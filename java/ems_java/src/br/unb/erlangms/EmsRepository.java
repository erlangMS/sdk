/*********************************************************************
 * @title EmsRepository
 * @version 1.0.0
 * @doc Classe de repositório para persistência de objetos
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/

package br.unb.erlangms;

import br.unb.erlangms.EmsUtil.EmsFilterStatement;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.jinq.jpa.JPAJinqStream;
import org.jinq.jpa.JinqJPAStreamProvider;

public abstract class EmsRepository<Model> implements Serializable {
	private static final long serialVersionUID = 4246028326643284073L;

	public abstract Class<Model> getClassOfModel();
	public abstract EntityManager getEntityManager();
	private static final Logger logger = EmsUtil.logger;
	private Class<Model> classOfModel = null;
	private EntityManager entityManager = null;
	private EntityManagerFactory entityManagerFactory = null;
	private Field idField = null;
	private String idFieldName = null;
	private Column idFieldColumn = null;
	private final List<String> cachedNamedQuery = new ArrayList<>();
	private final List<String> cachedNativeNamedQuery = new ArrayList<>();
	private boolean hasContraints = false;
	private Table tableAnnotation = null;
	private UniqueConstraint[] tableContrains = null;
	private List<Field> fieldsConstraints = null;
	private List<Field> fields = null;
	private String[] fieldNames = null;
	private String NAMED_QUERY_DELETE = null;
	private String NAMED_QUERY_EXISTS = null;
	private String NAMED_QUERY_CHECK_CONTRAINTS_ON_INSERT = null;
	private String NAMED_QUERY_CHECK_CONTRAINTS_ON_UPDATE = null;
	private String prefixFindNamedQuery = null;

	public EmsRepository(){

	}

	@PostConstruct
	private void postConstruct(){
		classOfModel = getClassOfModel();
		if (classOfModel != null){
			prefixFindNamedQuery = classOfModel.getSimpleName() + "_";
			entityManager = getEntityManager();
			if (entityManager != null){
				entityManagerFactory = entityManager.getEntityManagerFactory();
				// idField é obrigatório para que os recursos desta classe funcionem corretamente
				idField = EmsUtil.findFieldByAnnotation(classOfModel, Id.class);
				if (idField != null){
					idFieldName = idField.getName();
					tableAnnotation = classOfModel.getAnnotation(Table.class); // não é obrigatório o seu seu uso em VO
					idFieldColumn = idField.getAnnotation(Column.class); // não é obrigatório o seu seu uso em VO
					if (tableAnnotation != null){
						tableContrains = tableAnnotation.uniqueConstraints();
					}
					fieldsConstraints = EmsUtil.getFieldsWithUniqueConstraint(classOfModel);
					fields = EmsUtil.getFieldsFromModel(classOfModel);
					fieldNames = new String[fields.size()];
					for (int i = 0; i < fields.size(); i++){
						Field f = fields.get(i); // importante para conseguir acessar o valor do campo
						f.setAccessible(true);
						fieldNames[i] = f.getName();
					}
					// doCreateCachedNamedQueries é invocado somente para models que possuem a anotação @Table.
					if (tableAnnotation != null){
						doCreateCachedNamedQueries();
					}
				}else{
					throw new EmsValidationException("O modelo "+ classOfModel.getSimpleName() + " não possui nenhum campo com a anotação @Id.");
				}
			}else{
				throw new EmsValidationException("Não foi implementado getEntityManager() para a classe "+ getClass().getSimpleName());
			}
		}else{
			throw new EmsValidationException("Não foi implementado getClassOfModel() para a classe "+ getClass().getSimpleName());
		}
	}

	/**
	 * Retorna um stream para realizar pesquisas com lambda.
	 * @return  stream para pesquisa
	 * @author Everton de Vargas Agilar
	 */
	public JPAJinqStream<Model> getStreams(){
		JinqJPAStreamProvider streams = new JinqJPAStreamProvider(entityManager.getMetamodel());
		return (JPAJinqStream<Model>) streams.streamAll(entityManager, classOfModel);
	}

	/**
	 * Retorna um stream para realizar pesquisas com lambda.
	 * @param classOfModel classe do modelo
	 * @param <T> classe do modelo
	 * @return stream para pesquisa
	 * @author Everton de Vargas Agilar
	 */
	public <T> JPAJinqStream<T> getStreams(final Class<T> classOfModel){
		if (classOfModel != null){
			JinqJPAStreamProvider streams = new JinqJPAStreamProvider(entityManager.getMetamodel());
			return streams.streamAll(entityManager, classOfModel);
		}else{
			throw new EmsValidationException("Parâmetro classOfModel não pode ser null para EsRepository.getStreams.");
		}
	}


	/**
	 * Retorna uma lista de objetos a partir de um objeto request.
	 * O objeto request deve possuir os seguintes atributos padrão:
	 * @param request IEmsRequest - objeto da requisição
	 * @return List
	 * @author Everton de Vargas Agilar
	 */
	public List<Map<String, Object>> findAsMap(final IEmsRequest request){
		if (request != null){
			String filterRequest = request.getQuery("filter");
			String fieldsRequest = request.getQuery("fields");
			int limitRequest = request.getQueryAsInt("limit", 100);
			int offsetRequest = request.getQueryAsInt("offset", 0);
			String sortRequest = request.getQuery("sort");
			return findAsMap(filterRequest, fieldsRequest, limitRequest, offsetRequest, sortRequest);
		}else{
			throw new EmsValidationException("Parâmetro request não pode ser null para EsRepository.findAsMap.");
		}
	}

	/**
	 * Retorna uma lista de objetos.
	 * @param filter json com os campos do filtro. Ex:/ {"nome":"Everton de Vargas Agilar", "ativo":true}
	 * @param fields lista de campos ou o objeto inteiro se vazio. Ex: "nome, cpf, rg"
	 * @param limit Quantidade objetos trazer na pesquisa
	 * @param offset A partir de que posição. Iniciando em 1
	 * @param sort trazer ordenado por quais campos o conjunto de dados
	 * @return list of maps
	 * @author Everton de Vargas Agilar
	 */
	public List<Map<String, Object>> findAsMap(final String filter, final String fields, int limit, int offset, final String sort){
		Query query = parseQuery(filter, fields, limit, offset, sort, null);
		query.setFirstResult(offset);
		query.setMaxResults(limit);
		org.hibernate.Query q = (org.hibernate.Query) query.unwrap(org.hibernate.Query.class);
		q.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
		@SuppressWarnings("unchecked")
		List<Map<String,Object>> result = q.list();
		return result;
	}

	/**
	 * Retorna uma lista de objetos a partir de um sql nativo.
	 * Obs.: Somente sql nativo é suportado.
	 * @param sql - texto sql
	 * @return list of maps
	 * @author Everton de Vargas Agilar
	 */
	public List<Map<String, Object>> findAsMap(final String sql) {
		if (sql != null && !sql.isEmpty()){
			String namedQuery = prefixFindNamedQuery + EmsUtil.toBase64(EmsUtil.toSHA1(sql));
			Query query = createNativeNamedQuery(namedQuery, sql, null); // apenas retorna referência se já existe!
			org.hibernate.Query q = (org.hibernate.Query) query.unwrap(org.hibernate.Query.class);
			q.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
			@SuppressWarnings("unchecked")
			List<Map<String,Object>> result = q.list();
			return result;
		}else{
			throw new EmsValidationException("Parâmetro sql não pode ser null para EmsRepository.find.");
		}
	}


	/**
	 * Retorna uma lista de objetos a partir de um sql nativo.
	 * Obs.: Somente sql nativo é suportado.
	 * @param sql texto sql
	 * @param filter json com os campos do filtro. Ex:/ {"nome":"Everton de Vargas Agilar", "ativo":true}
	 * @param fields lista de campos ou o objeto inteiro se vazio. Ex: "nome, cpf, rg"
	 * @param limit Quantidade objetos trazer na pesquisa
	 * @param offset A partir de que posição. Iniciando em 1
	 * @param sort trazer ordenado por quais campos o conjunto de dados
	 * @return list of maps
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findAsMap(final String sql, String filter, String fields, int limit, int offset, String sort) {
		Query query = null;
		StringBuilder field_smnt = null;
		EmsFilterStatement where = null;
		StringBuilder sort_smnt = null;
		if (!(limit > 0 && limit <= 999999999)){
			throw new EmsValidationException("Parâmetro limit da pesquisa fora do intervalo permitido. Deve ser maior que zero e menor ou igual que 999999999");
		}
		if (!(offset >= 0 && offset < 999999999)){
			throw new EmsValidationException("Parâmetro offset da pesquisa fora do intervalo permitido. Deve ser maior que zero e menor que 999999999");
		}
		if (filter == null)	filter = "";
		if (fields == null)	fields = "";
		if (sort == null)	sort = "";
		String namedQuery = prefixFindNamedQuery + EmsUtil.toBase64(EmsUtil.toSHA1(sql + filter + fields + sort));
		if (!cachedNativeNamedQuery.contains(namedQuery)){
			where = EmsUtil.parseSqlNativeFilter(filter);

			// Inclui a lista de campos no sql
			if (fields != null && !fields.isEmpty()){
				try{
					field_smnt = new StringBuilder();
					String[] field_list = fields.split(",");
					boolean useVirgula = false;
					for (String field_name : field_list){
						if (useVirgula){
							field_smnt.append(",");
						}
						if (field_name.equals("pk")){
							field_smnt.append("this.").append(idField.getName());
						}else{
							field_smnt.append("this.").append(field_name);
						}
						useVirgula = true;
					}
				}catch (Exception e){
					throw new EmsValidationException("Lista de campos da pesquisa inválido. Erro interno: "+ e.getMessage());
				}
			}

			// Define o sort se foi informado
			if (sort != null && !sort.isEmpty()){
				try{
					boolean useVirgula = false;
					sort_smnt = new StringBuilder(" order by");
					String[] sort_list = sort.split(",");
					String sort_field = null;
					for (String s : sort_list){
						if (useVirgula){
							sort_smnt.append(",");
						}
						if (s.startsWith("-")){
							sort_field = s.substring(1);
							if (sort_field.equals("pk")){
								sort_field =  idField.getName();
							}
							sort_smnt.append(" this.").append(sort_field).append(" desc");
						}else{
							sort_field = s;
							if (sort_field.equals("pk")){
								sort_field =  idField.getName();
							}
							sort_smnt.append(" this.").append(sort_field);
						}
						useVirgula = true;
					}
				}catch (Exception e){
					throw new EmsValidationException("Sort da pesquisa inválido. Erro interno: "+ e.getMessage());
				}
			}

			try{
				// formata o sql
				StringBuilder sqlBuilder;
			    sqlBuilder = new StringBuilder("select ")
			    		.append(field_smnt == null ? "*" : field_smnt)
			    		.append(" from (").append(sql).append(") this ");
				if (where != null){
					sqlBuilder.append(where.where.toString());
				}
				if (sort_smnt != null){
					sqlBuilder.append(sort_smnt.toString());
				}
				String sqlCommand = sqlBuilder.toString();
				query = createNativeNamedQuery(namedQuery, sqlCommand, null);
			}catch (Exception e){
				throw new EmsValidationException("Não foi possível criar a query da pesquisa. Erro interno: "+ e.getMessage());
			}
		}else{
			query = getNamedQuery(namedQuery);
			where = EmsUtil.parseSqlNativeFilter(filter);
		}

		// Seta os parâmetros da query para cada campo do filtro
		if (where != null){
			EmsUtil.setQueryParameterFromMap(query, where.filtro_obj);
		}
		query.setFirstResult(offset);
		query.setMaxResults(limit);
		org.hibernate.Query q = (org.hibernate.Query) query.unwrap(org.hibernate.Query.class);
		q.setResultTransformer(EmsAliasToEntityMapResultTransformer.INSTANCE);
		List<Map<String,Object>> result = q.list();
		return result;
	}

	/**
	 * Retorna uma lista de objetos a partir de um sql nativo.
	 * Obs.: Somente sql nativo é suportado.
	 * @param sql texto sql
	 * @param filter json com os campos do filtro. Ex:/ {"nome":"Everton de Vargas Agilar", "ativo":true}
	 * @param fields lista de campos ou o objeto inteiro se vazio. Ex: "nome, cpf, rg"
	 * @param sort trazer ordenado por quais campos o conjunto de dados
	 * @return list of maps
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findAsMap(final String sql, String filter, String fields, String sort) {
		Query query = null;
		StringBuilder field_smnt = null;
		EmsFilterStatement where = null;
		StringBuilder sort_smnt = null;
		if (filter == null)	filter = "";
		if (fields == null)	fields = "";
		if (sort == null)	sort = "";
		String namedQuery = prefixFindNamedQuery + EmsUtil.toBase64(EmsUtil.toSHA1(sql + filter + fields + sort));
		if (!cachedNativeNamedQuery.contains(namedQuery)){
			where = EmsUtil.parseSqlNativeFilter(filter);

			// Inclui a lista de campos no sql
			if (fields != null && !fields.isEmpty()){
				try{
					field_smnt = new StringBuilder();
					String[] field_list = fields.split(",");
					boolean useVirgula = false;
					for (String field_name : field_list){
						if (useVirgula){
							field_smnt.append(",");
						}
						if (field_name.equals("pk")){
							field_smnt.append("this.").append(idField.getName());
						}else{
							field_smnt.append("this.").append(field_name);
						}
						useVirgula = true;
					}
				}catch (Exception e){
					throw new EmsValidationException("Lista de campos da pesquisa inválido. Erro interno: "+ e.getMessage());
				}
			}

			// Define o sort se foi informado
			if (sort != null && !sort.isEmpty()){
				try{
					boolean useVirgula = false;
					sort_smnt = new StringBuilder(" order by");
					String[] sort_list = sort.split(",");
					String sort_field = null;
					for (String s : sort_list){
						if (useVirgula){
							sort_smnt.append(",");
						}
						if (s.startsWith("-")){
							sort_field = s.substring(1);
							if (sort_field.equals("pk")){
								sort_field =  idField.getName();
							}
							sort_smnt.append(" this.").append(sort_field).append(" desc");
						}else{
							sort_field = s;
							if (sort_field.equals("pk")){
								sort_field =  idField.getName();
							}
							sort_smnt.append(" this.").append(sort_field);
						}
						useVirgula = true;
					}
				}catch (Exception e){
					throw new EmsValidationException("Sort da pesquisa inválido. Erro interno: "+ e.getMessage());
				}
			}

			try{
				// formata o sql
				StringBuilder sqlBuilder;
			    sqlBuilder = new StringBuilder("select ")
			    		.append(field_smnt == null ? "*" : field_smnt)
			    		.append(" from (").append(sql).append(") this ");
				if (where != null){
					sqlBuilder.append(where.where.toString());
				}
				if (sort_smnt != null){
					sqlBuilder.append(sort_smnt.toString());
				}
				String sqlCommand = sqlBuilder.toString();
				query = createNativeNamedQuery(namedQuery, sqlCommand, null);
			}catch (Exception e){
				throw new EmsValidationException("Não foi possível criar a query da pesquisa. Erro interno: "+ e.getMessage());
			}
		}else{
			query = getNamedQuery(namedQuery);
			where = EmsUtil.parseSqlNativeFilter(filter);
		}

		// Seta os parâmetros da query para cada campo do filtro
		if (where != null){
			EmsUtil.setQueryParameterFromMap(query, where.filtro_obj);
		}
		org.hibernate.Query q = (org.hibernate.Query) query.unwrap(org.hibernate.Query.class);
		q.setResultTransformer(EmsAliasToEntityMapResultTransformer.INSTANCE);
		List<Map<String,Object>> result = q.list();
		return result;
	}


	/**
	 * Retorna uma lista de objetos a partir de um filtro no formato json
	 * @param filter json com os campos do filtro. Ex:/ {"nome":"Everton de Vargas Agilar", "ativo":true}
	 * @param fields lista de campos que devem retornar ou o objeto inteiro se vazio. Ex: "nome, cpf, rg"
	 * @param limit Quantidade objetos trazer na pesquisa
	 * @param offset A partir de que posição. Iniciando em 1
	 * @param sort trazer ordenado por quais campos o conjunto de dados
	 * @return lista dos objetos
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public List<Model> find(final String filter, final String fields, int limit, int offset, final String sort){
		Query query = parseQuery(filter, fields, limit, offset, sort, null);
		query.setFirstResult(offset);
		query.setMaxResults(limit);
		List<Model> result = query.getResultList();
		return result;
	}


	/**
	 * Retorna uma lista de objetos a partir de um objeto request.
	 * O objeto request deve possuir os seguintes atributos padrão:
	 * @param request requisição
	 * @return lista dos objetos
	 * @author Everton de Vargas Agilar
	 */
	public List<Model> find(IEmsRequest request){
		if (request != null){
			String filter = request.getQuery("filter");
			String fields = request.getQuery("fields");
			int limit = request.getQueryAsInt("limit", 100);
			int offset = request.getQueryAsInt("offset", 0);
			String sort = request.getQuery("sort");
			return find(filter, fields, limit, offset, sort);
		}else{
			throw new EmsValidationException("Parâmetro request não pode ser null para EmsRepository.find.");
		}
	}


	/**
	 * Retorna uma lista de objeto a partir de um objeto pai, utilizando os filtros.Variação do método find, acrescentando o objeto pai.
	 * @param filter objeto json com os campos do filtro. Ex:/ {"nome":"Everton de Vargas Agilar", "ativo":true}
	 * @param fields lista de campos ou o objeto inteiro se vazio. Ex: "nome, cpf, rg"
	 * @param limit Quantidade objetos trazer na pesquisa
	 * @param offset A partir de que posição. Iniciando em 1
	 * @param sort trazer ordenado por quais campos o conjunto de dados
	 * @param owner Objeto pai
         * @throws Exception exception
	 * @return lista dos objetos
	 * @author André Luciano Claret
         */
	@SuppressWarnings("unchecked")
	public List<Model> find(final String filter, final String fields, int limit, int offset, final String sort, final Object owner) throws Exception{
		String new_filter = null;
		String fieldName = null;
		Integer idOwner;
		if (owner != null){
			idOwner = EmsUtil.getIdFromObject(owner);
			fieldName = "id" + owner.getClass().getSimpleName();
		} else {
			throw new EmsValidationException("Parâmetro owner não pode ser nulo para EmsRepository.find.");
		}
		if (filter != null){
			Map<String,Object> filtro_obj = new HashMap<String, Object>();
			filtro_obj = EmsUtil.fromJson(filter, HashMap.class);
			filtro_obj.put(fieldName, idOwner);
			new_filter = EmsUtil.toJson(filtro_obj);
			return find(new_filter, fields, limit, offset, sort);
		}else{
			throw new EmsValidationException("Parâmetro filter não pode ser nulo para EmsRepository.find.");
		}
	}

	/**
	 * Retorna uma lista de objeto a partir de um filtro por HashMap.
	 * @param filter HashMap com os campos do filtro.
	 * @param fields lista de campos ou o objeto inteiro se vazio. Ex: "nome, cpf, rg"
	 * @param limit Quantidade objetos trazer na pesquisa
	 * @param offset A partir de que posição. Iniciando em 1
	 * @param sort trazer ordenado por quais campos o conjunto de dados
	 * @return lista dos objetos
	 * @author André Luciano Claret
	 */
	public List<Model> find(final Map<String,Object> filter, final String fields, int limit, int offset, final String sort){
		if (filter != null && !filter.isEmpty()){
			String filterJson = EmsUtil.toJson(filter);
			return find(filterJson, fields, limit, offset, sort);
		}else{
			throw new EmsValidationException("Parâmetro filter não pode ser nulo para EmsRepository.find.");
		}
	}

	/**
	 * Retorna um objeto pelo seu id. O id é obtido direto do request.
	 * @param request requisição
	 * @return objeto ou EmsNotFoundException se não existe um objeto com o id
	 * @author Everton de Vargas Agilar
	 */
	public Model findById(IEmsRequest request){
		if (request != null){
			Integer id = request.getParamAsInt("id");
			if (id != null && id >= 0){
				Model obj = entityManager.find(classOfModel, id);
				if (obj == null){
					throw new EmsNotFoundException(classOfModel.getSimpleName() + " não encontrado: "+ id.toString());
				}
				return obj;
			}else{
				throw new EmsValidationException("Parâmetro id não pode ser null para EmsRepository.findById.");
			}
		}else{
			throw new EmsValidationException("Parâmetro request não pode ser null para EsRepository.find.");
		}
	}

	/**
	 * Retorna um objeto pelo seu id.
	 * @param id identificador do objeto
	 * @return objeto ou EmsNotFoundException se não existe um objeto com o id
	 * @author Everton de Vargas Agilar
	 */
	public Model findById(final Integer id){
		if (id != null && id >= 0){
			Model obj = entityManager.find(classOfModel, id);
			if (obj == null){
				throw new EmsNotFoundException(classOfModel.getSimpleName() + " não encontrado: "+ id.toString());
			}
			return obj;
		}else{
			throw new EmsValidationException("Parâmetro id não pode ser null para EmsRepository.findById.");
		}
	}


	/**
	 * Retorna um objeto pelo seu id.
	 * @param id identificador do objeto
	 * @return Model
	 * @author Everton de Vargas Agilar
	 */
	public Optional<Model> findByIdOptional(final Integer id){
		if (id != null && id >= 0){
			Model obj = entityManager.find(classOfModel, id);
			if (obj == null){
				return Optional.ofNullable(obj);
			}else {
				return Optional.of(obj);
			}
		}else{
			throw new EmsValidationException("Parâmetro id não pode ser null para EmsRepository.findById.");
		}
	}

	/**
	 * Retorna uma lista de objetos pesquisando por determinado campo.
	 * Obs.: O campo deve existir no model.
	 * @param field field do model que será pesquisado.
	 * @param value valor a ser pesquisado
	 * @return lista of model
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public List<Model> findByField(final Field field, final Object value){
		if (field != null){
			String fieldName = field.getName();
			String sqlFindByField =  new StringBuilder("select this from ")
												.append(classOfModel.getSimpleName()).append(" this")
												.append(" where this.")
												.append(field.getName()).append("=:pField").toString();
			return createNamedQuery(classOfModel.getSimpleName() + ".findByField" + fieldName, sqlFindByField)
				.setParameter("pField", value)
				.getResultList();
		}else{
			throw new EmsValidationException("Parâmetro field não pode ser null para EmsRepository.findByField.");
		}
	}

	/**
	 * Retorna uma lista de objetos pesquisando por determinado nome de campo.
	 * @param fieldName nome do campo do model.
	 * @param value valor a ser pesquisado
	 * @return lista of model
	 * @author Everton de Vargas Agilar
	 */
	public List<Model> findByField(final String fieldName, final Object value){
		if (fieldName != null){
			Field field = getField(fieldName);
			return findByField(field, value);
		}else{
			throw new EmsValidationException("Parâmetro fieldName não pode ser null para EmsRepository.findByField.");
		}
	}

	/**
	 * Retorna o primeiro objeto pesquisando por determinado campo.
	 * @param field field do model que será pesquisado.
	 * @param value valor a ser pesquisado.
	 * @return objeto ou EmsNotFoundException se não encontrar nenhum objeto.
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public Model findFirstByField(final Field field, final Object value){
		if (field != null){
			String fieldName = field.getName();
			String nameOfClass = classOfModel.getSimpleName();
			String sqlFindByField =  new StringBuilder("select this from ").append(nameOfClass).append(" this ")
											  .append(" where this.")
											  .append(fieldName).append("=:pField").toString();
			try{
				return (Model) createNamedQuery(nameOfClass + ".findFirstByField" + fieldName, sqlFindByField)
					.setParameter("pField", value)
					.setMaxResults(1)
					.getSingleResult();
			} catch (NoResultException e) {
				throw new EmsNotFoundException(nameOfClass + " não encontrado pelo campo "+ fieldName);
			}
		}else{
			throw new EmsValidationException("Parâmetro field não pode ser null para EmsRepository.findFirstByField.");
		}
	}

	/**
	 * Retorna o primeiro objeto pesquisando por determinado nome de campo.
	 * @param fieldName nome do campo do model.
	 * @param value valor a ser pesquisado.
	 * @return objeto ou EmsNotFoundException se não encontrar nenhum objeto ou se o campo não existe.
	 * @author Everton de Vargas Agilar
	 */
	public Model findFirstByField(final String fieldName, final Object value){
		if (fieldName != null){
			Field field;
			field = getField(fieldName);
			return findFirstByField(field, value);
		}else{
			throw new EmsValidationException("Parâmetro fieldName não pode ser null para EmsRepository.findFirstByField.");
		}
	}

	/**
	 * Verifica se o objeto existe, passando um mapa com os nomes dos atributos e seus valores.
	 * @param filter_map Mapa com os nomes dos atributos e seus valores
	 * @return true se o objeto foi encontrado
	 * @author André Luciano Claret
	 */
	public boolean exists(final Map<String, Object> filter_map){
		if (filter_map != null) {
			boolean anyMatch = false;
			Query query = null;
			String fieldName = null;
			String filter = null;
			List<String> listFunction = new ArrayList<String>();
			if (!filter_map.isEmpty()){
				for (String field: filter_map.keySet()){
					fieldName = field;
					break;
				}
				listFunction.add(0, "count");
				listFunction.add(1, fieldName);
				filter = EmsUtil.toJson(filter_map);
				query = parseQuery(filter, null, 1, 0, null, listFunction);
			} else{
				throw new EmsValidationException("Parâmetro filter_map não pode ser vazio para EmsRepository.exists.");
			}
			long result = (long) query.getSingleResult();
			if (result >= 1){
				anyMatch = true;
			}
			return anyMatch;
		}else {
			throw new EmsValidationException("Parâmetro filter_map não pode ser null para EmsRepository.exists.");
		}
	}

	/**
	 * Verifica se o objeto existe passando o id do objeto.
	 * @param id Id do objeto
	 * @return true se o objeto foi encontrado
	 * @author André Luciano Claret
	 * 		   Everton de Vargas Agilar
	 */
	public boolean exists(final Integer id){
		if (id != null && id >= 0){
			try{
				getNamedQuery(NAMED_QUERY_EXISTS)
					.setParameter("pId", id)
					.getSingleResult();
				return true;
			} catch (NoResultException e) {
				return false;
			} catch (NonUniqueResultException e){
				return true;
			}
		}else {
			throw new EmsValidationException("Parâmetro id do objeto não pode ser null para EmsRepository.exists.");
		}
	}

	/**
	 * Retorna um objeto pelo seu id a partir de um classOfModel específico.
	 * @param classOfModel classe do objeto
	 * @param <T> classe do modelo
	 * @param id identificador do objeto
	 * @return objeto ou EmsNotFoundException se não existe o objeto com o id
	 * @author Everton de Vargas Agilar
	 */
	public <T> T findById(final Class<T> classOfModel, final Integer id){
		if (classOfModel != null && id != null && id >= 0){
			T obj = entityManager.find(classOfModel, id);
			if (obj == null){
				throw new EmsNotFoundException(classOfModel.getSimpleName() + " não encontrado: "+ id.toString());
			}
			return obj;
		}else{
			throw new EmsValidationException("Parâmetros classOfModel e id não podem ser null para EmsRepository.findById.");
		}
	}

	/**
	 * Persiste as modificações de um objeto.
	 * @param obj objeto que será persistido
	 * @return objeto persistido
	 * @author Everton de Vargas Agilar
	 */
	public Model update(final Model obj){
		return update(obj, true);
	}

	/**
	 * Persiste as modificações de um objeto.
	 * @param obj objeto que será persistido
	 * @param flush indica se deve fazer flush no hibernate
	 * @return objeto persistido
	 * @author Everton de Vargas Agilar
	 */
	public Model update(final Model obj, boolean flush){
		if (obj != null){
			Integer idValue = EmsUtil.getIdFromObject(obj);
			if (idValue != null && idValue >= 0){
				if (hasContraints) checkConstraints(obj, false);
				entityManager.merge(obj);
			}else{
				throw new EmsValidationException("Não é possível atualizar objeto sem id em EmsRepository.update.");
			}
			if (flush)
				entityManager.flush();
			return obj;
		}else{
			throw new EmsValidationException("Parâmetro obj não pode ser null para EmsRepository.update.");
		}
	}

	/**
	 * Insere um novo objeto.
	 * @param obj objeto que será inserido
	 * @return objeto inserido
	 * @author Everton de Vargas Agilar
	 */
	public Model insert(final Model obj){
		return insert(obj, true);
	}

	/**
	 * Insere um novo objeto.
	 * @param obj objeto que será inserido
     * @param flush indica se deve fazer flush no hibernate
	 * @return objeto inserido
	 * @author Everton de Vargas Agilar
	 */
	public Model insert(final Model obj, boolean flush){
		if (obj != null){
			if (getIdFromObject(obj) != null){
				throw new EmsValidationException("Não é possível incluir objeto que já possui identificador.");
			}else{
				if (hasContraints) checkConstraints(obj, true);
				entityManager.persist(obj);
			}
			if (flush)
				entityManager.flush();
			return obj;
		}else{
			throw new EmsValidationException("Parâmetro obj não pode ser null para EmsRepository.insert.");
		}
	}

	/**
	 * Verifica as constrains do objeto e levanta uma exception se houver violação de alguma regra.
	 * Esta função é invocada antes de persistir o objeto no banco e somente após todas as validações realizadas no objeto.
	 * @param obj objeto que será verificado as constraints.
	 * @param isInsert se true é insert senão é update.
	 * @author Everton de Vargas Agilar
	 */
	private void checkConstraints(final Model obj, boolean isInsert) {
		Query query = null;
		if (isInsert){
			query = getNamedQuery(NAMED_QUERY_CHECK_CONTRAINTS_ON_INSERT);
		}else{
			query = getNamedQuery(NAMED_QUERY_CHECK_CONTRAINTS_ON_UPDATE);
		}
		for (Parameter<?> p : query.getParameters()){
			String paramName = p.getName();
			Field field = null;
			Object value;
			try {
				for (int i = 0; i < fields.size(); i++){
					field = fields.get(i);
					if (field.getAnnotation(Column.class).name().equals(paramName)){
						break;
					}
				}
				value = field.get(obj);
				query.setParameter(paramName, value);
			} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
				throw new EmsValidationException("Não é possível verificar as constraints do objeto "+ classOfModel.getSimpleName() + ". Erro interno: "+ e.getMessage());
			}
		}

		try{
			query.getSingleResult();
		}catch (NoResultException e){
			return; // ok, registro não está duplicado
		}catch (Exception e){
			throw new EmsValidationException("Não é possível verificar as constraints do objeto "+ classOfModel.getSimpleName() + ". Erro interno: "+ e.getMessage());
		}

		// Se chegou até aqui então o registro está duplicado. Não há registro quando ocorrer uma exception NoResultException.
		throw new EmsValidationException("Registro duplicado, verifique.");
	}

	/**
	 * Insere ou atualiza um objeto.
	 * @param obj objeto que será inserido ou atualizado.
	 * @return objeto inserido ou atualizado
	 * @author Everton de Vargas Agilar
	 */
	public Model insertOrUpdate(final Model obj){
		return insertOrUpdate(obj, true);
	}

	/**
	 * Insere ou atualiza um objeto.
	 * @param obj objeto que será inserido ou atualizado.
	 * @param flush indica se deve fazer flush no hibernate
	 * @return objeto inserido ou atualizado
	 * @author Everton de Vargas Agilar
	 */
	public Model insertOrUpdate(final Model obj, boolean flush){
		if (obj != null){
			Integer idValue = EmsUtil.getIdFromObject(obj);
			if (idValue != null && idValue >= 0){
				if (hasContraints) checkConstraints(obj, false);
				entityManager.merge(obj);
			}else{
				if (hasContraints) checkConstraints(obj, true);
				entityManager.persist(obj);
			}
			if (flush)
				entityManager.flush();
			return obj;
		}else{
			throw new EmsValidationException("Parâmetro obj não pode ser null para EmsRepository.insertOrUpdate.");
		}
	}

	/**
	 * Exclui um objeto.
	 * @param id identificador do objeto
	 * @return true se o objeto foi excluído
	 * @author Everton de Vargas Agilar
	 */
	public boolean delete(final Integer id) {
		if (id != null && id >= 0){
			return getNamedQuery(NAMED_QUERY_DELETE)
				.setParameter("pId", id)
				.executeUpdate() > 0;
		}else{
			throw new EmsValidationException("Parâmetro id deve ser maior que zero para EmsRepository.delete.");
		}
	}

	/**
	 * Exclui um objeto a partir de um model específico.
	 * @param classOfModel classe do objeto
	 * @param <T> classe do modelo
	 * @param id identificador do objeto
	 * @return true se o objeto foi excluído
	 * @author Everton de Vargas Agilar
	 */
	public <T> boolean delete(final Class<T> classOfModel, final Integer id) {
		if (classOfModel != null && id != null && id >= 0){
			Field field = EmsUtil.findFieldByAnnotation(classOfModel, Id.class);
			if (field == null){
				throw new EmsValidationException(classOfModel.getSimpleName() + " não possui campo id.");
			}
			String idFieldName = field.getName();
			String sql = new StringBuilder("delete from ")
								.append(classOfModel.getSimpleName())
								.append(" where ")
								.append(idFieldName).append("=:pId").toString();
			String namedQuery = classOfModel.getSimpleName() + ".EmsRepository.delete";
			return createNamedQuery(namedQuery, sql)
					.setParameter("pId", id)
					.executeUpdate() > 0;
		}else{
			throw new EmsValidationException("Parâmetros classOfModel e id do método EmsRepository.delete não podem ser null");
		}
	}


	/**
	 * Um método protected para as classes herdadas criarem as queries em cache para serem reutilizadas.
	 * @author Everton de Vargas Agilar
	 */
	protected void createCachedNamedQueries() {
	}


	/**
	 * Cria uma query a partir de um filtro e a partir de uma função sql.
	 * @param filter objeto json com os campos do filtro. Ex: {"nome":"Everton de Vargas Agilar", "ativo":true}
	 * @param fields lista de campos que devem retornar ou o objeto inteiro se vazio. Ex: "nome, cpf, rg"
	 * @param limit Quantidade objetos trazer na pesquisa
	 * @param offset A partir de que posição. Iniciando em 1
	 * @param sort Trazer ordenado por quais campos o conjunto de dados
	 * @param listFunction Lista com a funçao sql e o nome do atributo, nesta ordem. Ex: {"count", "idObjeto"}
	 * @return query com o filtro e a função sql
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public Query parseQuery(final String filter,
							 final String fields,
							 int limit,
							 int offset,
							 final String sort,
							 final List<String> listFunction){
		Query query = null;
		StringBuilder field_smnt = null;
		StringBuilder where = null;
		StringBuilder sort_smnt = null;
		String sqlFunction = null;
		Map<String, Object> filtro_obj = null;
		String simpleNameOfModel = classOfModel.getSimpleName();

		if (!(limit > 0 && limit <= 999999999)){
			throw new EmsValidationException("Parâmetro limit da pesquisa fora do intervalo permitido. Deve ser maior que zero e menor ou igual que 999999999");
		}

		if (!(offset >= 0 && offset < 999999999)){
			throw new EmsValidationException("Parâmetro offset da pesquisa fora do intervalo permitido. Deve ser maior que zero e menor que 999999999");
		}

		// tem filtro?
		if (filter != null && filter.length() > 5){
			try{
				boolean useAnd = false;
				filtro_obj = (Map<String, Object>) EmsUtil.fromJson(filter, HashMap.class);
				where = new StringBuilder("where ");
				int p = 1;
				for (String field : filtro_obj.keySet()){
					if (useAnd){
						where.append(" and ");
					}
					String[] field_defs = field.split("__");
					String fieldName;
					String fieldOperator;
					String sqlOperator;
					int field_len = field_defs.length;
					if (field_len == 1){
						fieldName = field;
						fieldOperator = "=";
						sqlOperator = "=";
					} else if (field_len == 2){
						fieldName = field_defs[0];
						fieldOperator = field_defs[1];
						sqlOperator = EmsUtil.fieldOperatorToSqlOperator(fieldOperator);
					}else{
						throw new EmsValidationException("Campo de pesquisa "+ field + " inválido.");
					}
					if (fieldName.equals("pk")){
						fieldName = idField.getName();
					}else{
						// Verifica se o campo existe. Uma exception EmsValidationException ocorre se não existir o campo
						getField(fieldName);
					}
					if (field_len == 2){
						if (fieldOperator.equals("isnull")){
							boolean fieldBoolean = EmsUtil.parseAsBoolean(filtro_obj.get(field));
							if (fieldBoolean){
								where.append(fieldName).append(" is null ");
							}else{
								where.append(fieldName).append(" is not null ");
							}
						} else if(fieldOperator.equals("icontains") || fieldOperator.equals("ilike")){
							fieldName = String.format("lower(this.%s)", fieldName);
							where.append(fieldName).append(sqlOperator).append("?").append(p++);
						}else if(fieldOperator.equals("in")){
							fieldName = String.format("this.%s", fieldName);
							where.append(fieldName).append(sqlOperator).append("?").append(p++);
						}else{
							fieldName = String.format("this.%s", fieldName);
							where.append(fieldName).append(sqlOperator).append("?").append(p++);
							System.out.println(where.toString());
						}
					}else{
						fieldName = String.format("this.%s", fieldName);
						where.append(fieldName).append(sqlOperator).append("?").append(p++);
					}
					useAnd = true;
				}
			}catch (Exception e){
				throw new EmsValidationException("Filtro da pesquisa inválido. Erro interno: "+ e.getMessage());
			}
		}

		// Inclui a lista de campos no sql
		if (fields != null && !fields.isEmpty()){
			try{
				field_smnt = new StringBuilder();
				String[] field_list = fields.split(",");
				boolean useVirgula = false;
				for (String field_name : field_list){
					if (useVirgula){
						field_smnt.append(",");
					}
					if (field_name.equals("pk")){
						field_smnt.append("this.").append(idField.getName());
					}else{
						field_smnt.append("this.").append(field_name);
					}
					useVirgula = true;
				}
			}catch (Exception e){
				throw new EmsValidationException("Lista de campos da pesquisa inválido. Erro interno: "+ e.getMessage());
			}
		}

		// Define o sort se foi informado
		if (sort != null && !sort.isEmpty()){
			try{
				boolean useVirgula = false;
				sort_smnt = new StringBuilder(" order by");
				String[] sort_list = sort.split(",");
				String sort_field = null;
				for (String s : sort_list){
					if (useVirgula){
						sort_smnt.append(",");
					}
					if (s.startsWith("-")){
						sort_field = s.substring(1);
						if (sort_field.equals("pk")){
							sort_field =  idField.getName();
						}
						sort_smnt.append(" this.").append(sort_field).append(" desc");
					}else{
						sort_field = s;
						if (sort_field.equals("pk")){
							sort_field =  idField.getName();
						}
						sort_smnt.append(" this.").append(sort_field);
					}
					useVirgula = true;
				}
			}catch (Exception e){
				throw new EmsValidationException("Sort da pesquisa inválido. Erro interno: "+ e.getMessage());
			}
		}

		//verifica se a função sql passada existe
		if (listFunction != null && !listFunction.isEmpty()){
			sqlFunction = EmsUtil.listFunctionToSqlFunction(listFunction);
		}


		try{
			// formata o sql
			StringBuilder sqlBuilder;
			if (listFunction == null){
				 sqlBuilder = new StringBuilder("select ")
				.append(field_smnt == null ? "this" : field_smnt.toString())
				.append(" from ").append(simpleNameOfModel).append(" this ");
			} else {
				 sqlBuilder = new StringBuilder("select ")
				.append(sqlFunction)
				.append(" from ").append(simpleNameOfModel).append(" this ");
			}
			if (where != null){
				sqlBuilder.append(where.toString());
			}
			if (sort_smnt != null){
				sqlBuilder.append(sort_smnt.toString());
			}
			String sql = sqlBuilder.toString();
			query = createNamedQuery(sql, sql);
		}catch (Exception e){
			throw new EmsValidationException("Não foi possível criar a query da pesquisa. Erro interno: "+ e.getMessage());
		}

		// Seta os parâmetros da query para cada campo do filtro
		if (where != null){
			EmsUtil.setQueryParameterFromMap(query, filtro_obj);
		}

		return query;
	}


	/**
	 * Permite criar uma named query JPA para posterior execução. Depois de criado pode ser obtido com getNamedQuery.
	 * NamedQuery são mais rápidas e economizam recursos. Use isto em vez de criar uma query a cada execução de código.
	 * Se a namedQuery já existe, apenas é retornado sua referência.
	 * @param namedQuery nome da query.
	 * @param sql sql JPA da query
	 * @return query ou exception
	 * @author Everton de Vargas Agilar
	 */
	protected Query createNamedQuery(final String namedQuery, final String sql) {
		Query query = null;
		if (cachedNamedQuery.contains(namedQuery)){
			query = entityManager.createNamedQuery(namedQuery);
		}else{
			cachedNamedQuery.add(namedQuery);
			try {
				query = entityManager.createQuery(sql);
			}catch (Exception e) {
				throw new EmsValidationException("Não foi possível criar namedQuery " + namedQuery + " para o sql \"" + sql + "\" no método EmsRepository.createNamedQuery. Erro interno: "+ e.getMessage());
			}
			entityManagerFactory.addNamedQuery(namedQuery, query);
			logger.info("Build named query: "+ namedQuery);
			logger.info("\tSQL: "+ sql);
		}
		return query;
	}

	/**
	 * Permite criar uma named query com sql nativo para posterior execução. Depois de criado pode ser obtido com getNamedQuery.
	 * NamedQuery são mais rápidas e economiza recursos. Use isto em vez de criar uma query a cada execução de código.
	 * @param namedQuery nome da query.
	 * @param sql sql nativo da query
	 * @param resultClass informe a classe do objeto se a query tem que mapear senão null.
	 * @param <T> objeto do modelo
	 * @return query
	 * @author Everton de Vargas Agilar
	 */
	protected <T> Query createNativeNamedQuery(final String namedQuery, final String sql, final Class<T> resultClass) {
		Query query = null;
		if (cachedNativeNamedQuery.contains(namedQuery)){
			query = entityManager.createNamedQuery(namedQuery);
		} else{
			cachedNativeNamedQuery.add(namedQuery);
			if (resultClass == null){
				query = entityManager.createNativeQuery(sql);
			}else{
				query = entityManager.createNativeQuery(sql, resultClass);
			}
			entityManagerFactory.addNamedQuery(namedQuery, query);
			logger.info("Build native named query: "+ namedQuery);
			logger.info("\tSQL: "+ sql);
		}
		return query;
	}

	/**
	 * Retorna a referẽncia para um query previamente criada para execução.
	 * NamedQuery são mais rápidas e economiza recursos. Use isto em vez de criar uma query a cada execução de código.
	 * @param namedQuery nome da query.
	 * @return query
	 * @author Everton de Vargas Agilar
	 */
	protected Query getNamedQuery(final String namedQuery) {
		return entityManager.createNamedQuery(namedQuery);
	}


	/**
	 * Um método interno para criar as named queries
	 * @author Everton de Vargas Agilar
	 */
	private void doCreateCachedNamedQueries(){
		String nameOfModel = classOfModel.getName();
		String simpleNameOfModel = classOfModel.getSimpleName();

		// ************* create query delete *****************

		NAMED_QUERY_DELETE = nameOfModel + ".delete";
		String sqlDelete = new StringBuilder("delete from ")
											.append(simpleNameOfModel)
											.append(" where ")
											.append(idFieldName).append("=:pId").toString();
		createNamedQuery(NAMED_QUERY_DELETE, sqlDelete);




		// // ************* create query exists *****************

		NAMED_QUERY_EXISTS = nameOfModel + ".exists";
		String sqlExists =  new StringBuilder("select 1 from ")
											.append(simpleNameOfModel)
											.append(" where ")
											.append(idFieldName).append("=:pId").toString();
		createNamedQuery(NAMED_QUERY_EXISTS, sqlExists);


		// ************* create query to check contraints on insert *****************

		String sqlQueryConstraintInsert = createSqlForConstraintCheck(true);
		hasContraints = (sqlQueryConstraintInsert != null);
		if (hasContraints){
			NAMED_QUERY_CHECK_CONTRAINTS_ON_INSERT = nameOfModel + ".checkConstraintInsert";
			createNativeNamedQuery(NAMED_QUERY_CHECK_CONTRAINTS_ON_INSERT, sqlQueryConstraintInsert, null);

			String sqlQueryConstraintUpdate = createSqlForConstraintCheck(false);
			NAMED_QUERY_CHECK_CONTRAINTS_ON_UPDATE = nameOfModel + ".checkConstraintUpdate";
			createNativeNamedQuery(NAMED_QUERY_CHECK_CONTRAINTS_ON_UPDATE, sqlQueryConstraintUpdate, null);
		}


		// ************* create cached named queries of inherited class *****************
		createCachedNamedQueries();
	}


	/**
	 * Cria e retorna um sql para validar constraints de um modelo.
	 * @param isInsert se true é um insert senão é um update
	 * @return sql ou null se  não houver nenhuma constraint no modelo
	 * @author Everton de Vargas Agilar
	 */
	private String createSqlForConstraintCheck(boolean isInsert){
		int tableConstraintsCount = tableContrains.length;
		int fieldConstraintsCount = fieldsConstraints.size();
		boolean hasContraints = tableContrains.length > 0 || fieldConstraintsCount > 0;
		if (hasContraints){
			StringBuilder sql = new StringBuilder();
			sql.append("select top(1) 1 ")
									 .append("from ").append(tableAnnotation.name())
									 .append(" this where ");

			// Se não é um insert então é um update :)
			if (!isInsert){
				String idFieldColumnName = idFieldColumn.name();
				sql.append(idFieldColumnName).append("!=:").append(idFieldColumnName).append(" and (");
			}

			// build table constraints conditions
			for (int i = 0; i < tableConstraintsCount; i++){
				UniqueConstraint c = tableContrains[i];
				String[] columnNames = c.columnNames();
				int columnNamesCount = columnNames.length;
				sql.append("(");
				for (int j = 0; j < columnNamesCount; j++){
					String f = columnNames[j];
					sql.append("this.").append(f).append("=:").append(f);
					if (j+1 < columnNamesCount){
						sql.append(" and ");
					}
				}
				sql.append(")");
				if (i+1 < tableConstraintsCount){
					sql.append(" or ");
				}
			}

			// build field constraints conditions
			if (fieldConstraintsCount > 0){
				if (tableConstraintsCount > 0){
					sql.append(" or (");
				}else{
					sql.append("(");
				}
				for (int i = 0; i < fieldConstraintsCount; i++){
					Field field = fieldsConstraints.get(i);
					String f = field.getAnnotation(Column.class).name();
					sql.append("this.").append(f).append("=:").append(f);
					if (i+1 < fieldConstraintsCount){
						sql.append(" or ");
					}
				}
				sql.append(")");
			}

			if (!isInsert){
				sql.append(")");
			}

			return sql.toString();
		}

		// return null porque não há constraint na declaração do modelo
		return null;
	}

	/**
	 * Retorna a referência para um field de um model.
	 * Semelhante a getClass().getField mas mais rápido pois não faz as checagens de segurança que a JVM realizada.
	 * @param fieldName nome do campo para a busca.
	 * @return field ou EmsValidationException.
	 * @author Everton de Vargas Agilar
	 */
	protected Field getField(final String fieldName){
		 // Certamente, para poucos elementos (menos de 20) uma pesquisa sequencial é mais rápido que um Hashtable
		for (Field field : fields){
			if (field.getName().equals(fieldName)){
				return field;
			}
		}
		throw new EmsValidationException(classOfModel.getSimpleName() + "." + fieldName + " não existe.");
	}

	/**
	 * Retorna o id de um model.
	 * Semelhante a EmsUtil.getIdFromObject mas mais rápido pois a classe EmsRepository possui uma referência direta para idField.
	 * @param obj objeto para obter o id
	 * @return id ou null se não tem valor
	 * @author Everton de Vargas Agilar
	 */
	protected Integer getIdFromObject(final Model obj){
		try {
			Object id = idField.get(obj);
			return (Integer) id;
		} catch (Exception e) {
			return null;
		}
	}


	/**
	 * Classe utilizada pelo método find(String sql) para mapear os dados
	 * @author Everton de Vargas Agilar
	 */
	public static class EmsAliasToEntityMapResultTransformer extends org.hibernate.transform.AliasedTupleSubsetResultTransformer {

		private static final long serialVersionUID = 1L;
		public static final EmsAliasToEntityMapResultTransformer INSTANCE = new EmsAliasToEntityMapResultTransformer();

		/**
		 * Disallow instantiation of AliasToEntityMapResultTransformer.
		 */
		private EmsAliasToEntityMapResultTransformer() {
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Object transformTuple(Object[] tuple, String[] aliases) {
			int tupleLengh = tuple.length;
			if (aliases[tupleLengh-1].startsWith("__")){
				--tupleLengh;
			}
			Map result = new HashMap(tupleLengh);
			for ( int i=0; i<tupleLengh; i++ ) {
				String alias = aliases[i];
				if ( alias!=null ) {
					result.put( alias, tuple[i] );
				}
			}
			return result;
		}

		@Override
		public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
			return false;
		}

		/**
		 * Serialization hook for ensuring singleton uniqueing.
		 *
		 * @return The singleton instance : {@link #INSTANCE}
		 */
		private Object readResolve() {
			return INSTANCE;
		}
	}

}





