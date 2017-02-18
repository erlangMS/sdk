/*********************************************************************
 * @title EmsRepository
 * @version 1.0.0
 * @doc Classe de repositório para persistência de objetos
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/

package br.erlangms;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import org.jinq.jpa.JPAJinqStream;
import org.jinq.jpa.JinqJPAStreamProvider;

public abstract class EmsRepository<Model> {

	public abstract Class<Model> getClassOfModel();
	public abstract EntityManager getEntityManager();
	private Class<Model> classOfModel = null;
	private EntityManager entityManager = null;
	private EntityManagerFactory entityManagerFactory = null;
	private Field IdField = null;
	private String idFieldName = null;
	private String NAMED_QUERY_DELETE = null;
	private String NAMED_QUERY_EXISTS = null;
	private String NAMED_QUERY_CHECK_CONTRAINTS_ON_INSERT = null;
	private String NAMED_QUERY_CHECK_CONTRAINTS_ON_UPDATE = null;
	private Logger logger = Logger.getLogger("erlangms");
	private List<String> cachedNamedQuery = new ArrayList<String>();
	private List<String> cachedNativeNamedQuery = new ArrayList<String>();
	private boolean hasContraints = false;

	public EmsRepository(){

	}
	
	@PostConstruct
	private void postConstruct(){
		classOfModel = getClassOfModel();
		if (classOfModel != null){
			entityManager = getEntityManager();
			if (entityManager != null){
				entityManagerFactory = entityManager.getEntityManagerFactory();
				IdField = EmsUtil.findFieldByAnnotation(classOfModel, Id.class);
				if (IdField != null){
					idFieldName = IdField.getName();
					doCreateCachedNamedQueries();
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
	 * Retorna um stream para realizar pesquisas 
	 * @return  stream para pesquisa
	 * @author Everton de Vargas Agilar
	 */
	public JPAJinqStream<Model> getStreams(){
		EntityManager em = getEntityManager();
		JinqJPAStreamProvider streams = new JinqJPAStreamProvider(em.getMetamodel());
		return (JPAJinqStream<Model>) streams.streamAll(em, getClassOfModel());
	}

	/**
	 * Retorna um stream para realizar pesquisas
	 * @param classOfModel classe do modelo 
	 * @return  stream para pesquisa
	 * @author Everton de Vargas Agilar
	 */
	public <T> JPAJinqStream<T> getStreams(final Class<T> classOfModel){
		EntityManager em = getEntityManager();
		JinqJPAStreamProvider streams = new JinqJPAStreamProvider(em.getMetamodel());
		return streams.streamAll(em, classOfModel);
	}
	
	/**
	 * Pesquisa um objeto a partir de um filtro
	 * @param filter objeto json com os campos do filtro. Ex:/ {"nome":"Everton de Vargas Agilar", "ativo":true}
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
	 * Recupera uma lista de objeto a partir de um objeto pai, utilizando os filtros. Variação do método find, acrescentando o objeto pai. 
	 * @param filter objeto json com os campos do filtro. Ex:/ {"nome":"Everton de Vargas Agilar", "ativo":true}
	 * @param fields lista de campos que devem retornar ou o objeto inteiro se vazio. Ex: "nome, cpf, rg"
	 * @param limit Quantidade objetos trazer na pesquisa
	 * @param offset A partir de que posição. Iniciando em 1
	 * @param sort trazer ordenado por quais campos o conjunto de dados
	 * @param owner Objeto pai
	 * @return lista dos objetos
	 * @author André Luciano Claret
	 */
	@SuppressWarnings("unchecked")
	public List<Model> find(final String filter, final String fields, int limit, int offset, final String sort, final Object owner){
		String new_filter = null;
		String fieldName = null;
		Integer idOwner;
		Map<String,Object> filtro_obj = new HashMap<String, Object>();
		if (owner != null){
			idOwner = EmsUtil.getIdFromObject(owner);
			fieldName = "id" + owner.getClass().getSimpleName();			
		} else {
			throw new EmsValidationException("Objeto pai não pode ser nulo!");
		}	
		filtro_obj = EmsUtil.fromJson(filter, HashMap.class);
		filtro_obj.put(fieldName, idOwner);
		new_filter = EmsUtil.toJson(filtro_obj);
		return find(new_filter, fields, limit, offset, sort);
	}
	
	/**
	 * Recupera uma lista de objeto a partir de um filtro por HashMap. Variação do método find, para consultas internas. 
	 * @param filter_map HashMap com os campos do filtro. Ex:/ {"idAluno" = 12345678, "idDisciplina" = 321654}
	 * @param fields lista de campos que devem retornar ou o objeto inteiro se vazio. Ex: "nome, cpf, rg"
	 * @param limit Quantidade objetos trazer na pesquisa
	 * @param offset A partir de que posição. Iniciando em 1
	 * @param sort trazer ordenado por quais campos o conjunto de dados
	 * @return lista dos objetos
	 * @author André Luciano Claret
	 */
	public List<Model> find(final Map<String,Object> filter_map, final String fields, int limit, int offset, final String sort){
		String filter = null;
		if (!filter_map.isEmpty()){
			filter = EmsUtil.toJson(filter_map);
		}
		return find(filter, fields, limit, offset, sort);
	}
	
	/**
	 * Recuperar um objeto pelo seu id
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
	
	@SuppressWarnings("unchecked")
	public Model findFirstOwner(final Integer idOwner, final String foreignKeyFieldName){
		if (idOwner != null && idOwner >= 0){
			String sqlFindByOwner =  new StringBuilder("select this from ")
												.append(classOfModel.getSimpleName())
												.append(" where this.")
												.append(foreignKeyFieldName).append("=:pIdOwner").toString();
			return (Model) createNamedQuery(sqlFindByOwner, sqlFindByOwner)
				.setParameter("pIdOwner", idOwner)
				.setMaxResults(1)
				.getSingleResult();
		}else{
			throw new EmsValidationException("Parâmetro owner não pode ser null para EmsRepository.findByOwner.");
		}
	}

	@SuppressWarnings("unchecked")
	public List<Model> findByOwner(final Integer idOwner, final String foreignKeyFieldName){
		if (idOwner != null && idOwner >= 0){
			String sqlFindByOwner =  new StringBuilder("select this from ")
												.append(classOfModel.getSimpleName())
												.append(" where ")
												.append(foreignKeyFieldName).append("=:pIdOwner").toString();
			return createNamedQuery(sqlFindByOwner, sqlFindByOwner)
				.setParameter("pIdOwner", idOwner)
				.getResultList();
		}else{
			throw new EmsValidationException("Parâmetro owner não pode ser null para EmsRepository.findByOwner.");
		}
	}

	/**
	 * Verifica se o objeto existe, passando um mapa com os nomes dos atributos e seus valores 
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
				throw new EmsValidationException("É necessário informar parâmetros para a pesquisa.");
			}
			long result = (long) query.getSingleResult();
			if (result >= 1){
				anyMatch = true;
			}			
			return anyMatch;
		}else {
			throw new EmsValidationException("filer_map não pode ser null para EmsRepository.exists.");
		}
	}
	
	/**
	 * Verifica se o objeto existe, passando o id do objeto 
	 * @param id Id do objeto
	 * @return true se o objeto foi encontrado 
	 * @author André Luciano Claret
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
			throw new EmsValidationException("É necessário informar o id do objeto!");
		}
	}

	/**
	 * Recuperar um objeto pelo seu id
	 * @param classOfModel classe do objeto
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
	 * Persiste as modificações de um objeto
	 * @param obj objeto que será persistido
	 * @return objeto persistido
	 * @author Everton de Vargas Agilar
	 */
	public <T> T update(final T obj){
		if (obj != null){
			Integer idValue = EmsUtil.getIdFromObject(obj);
			if (idValue != null && idValue >= 0){
				entityManager.merge(obj);
			}else{
				throw new EmsValidationException("Não é possível atualizar objeto sem id em EmsRepository.update.");
			}
			entityManager.flush();
			return obj;
		}else{
			throw new EmsValidationException("Parâmetro obj não pode ser null para EmsRepository.update.");
		}
	}
	
	/**
	 * Insere um novo objeto
	 * @param obj objeto que será inserido
	 * @return objeto inserido
	 * @author Everton de Vargas Agilar
	 */
	public <T> T insert(final T obj){
		if (obj != null){
			Integer idValue = EmsUtil.getIdFromObject(obj);
			if (idValue != null && idValue >= 0){
				throw new EmsValidationException("Não é possível incluir objeto que já possui identificador.");
			}else{
				if (hasContraints) checkInsertConstraints(obj);
				entityManager.persist(obj);
			}
			entityManager.flush();
			return obj;
		}else{
			throw new EmsValidationException("Parâmetro obj não pode ser null para EmsRepository.insert.");
		}
	}

	/**
	 * Verifica as constrains do objeto e levanta uma exception case houve violação
	 * @param obj objeto que será inserido
	 * @return objeto inserido
	 * @author Everton de Vargas Agilar
	 */
	private <T> void checkInsertConstraints(final T obj) {
		Query query = getNamedQuery(NAMED_QUERY_CHECK_CONTRAINTS_ON_INSERT);
		Class<?> classObj = obj.getClass();
		Field[] fields = classObj.getDeclaredFields();
		for (Parameter<?> p : query.getParameters()){
			String paramName = p.getName();
			Field field = null;
			Object value;
			try {
				for (int i = 0; i < fields.length; i++){
					field = fields[i];
					if (field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).name().equals(paramName)){
						break;
					}
				}
				field.setAccessible(true);
				value = field.get(obj);
				query.setParameter(paramName, value);
			} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
				throw new EmsValidationException("Não é possível checar as constraints na inserção do objeto. Erro interno: "+ e.getMessage());
			}
		}
		
		try{
			query.getSingleResult();
		}catch (NoResultException e){
			return; // ok, registro não está duplicado
		}catch (Exception e){
			throw new EmsValidationException("Não é possível checar as constraints na inserção do objeto. Erro interno: "+ e.getMessage());
		}
		throw new EmsNotFoundException("Registro duplicado, verifique.");
	}
	
	/**
	 * Insere ou atualiza um objeto
	 * @param obj objeto que será inserido ou atualizado
	 * @param update_values Map com os dados modificados
	 * @return objeto inserido ou atualizado
	 * @author Everton de Vargas Agilar
	 */
	public <T> T insertOrUpdate(final T obj){
		if (obj != null){
			Integer idValue = EmsUtil.getIdFromObject(obj);
			if (idValue != null && idValue >= 0){
				entityManager.merge(obj);
			}else{
				entityManager.persist(obj);
			}
			entityManager.flush();
			return obj;
		}else{
			throw new EmsValidationException("Parâmetro obj não pode ser null para EmsRepository.insertOrUpdate.");
		}
	}
	
	/**
	 * Exclui um objeto
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
	 * Exclui um objeto
	 * @param classOfModel classe do objeto
	 * @param id identificador do objeto
	 * @return true se o objeto foi excluído
	 * @author Everton de Vargas Agilar
	 * @param <T>
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
	 * Um método protected para as classes herdadas criarem as queries
	 * @author Everton de Vargas Agilar
	 */
	protected void createCachedNamedQueries() {
	}	

	public Query parseQuery(final String filter, 
							 final String fields, 
							 int limit, 
							 int offset, 
							 final String sort, 
							 final List<String> listFunction){
		return parseQuery(filter, fields, limit, offset, sort, listFunction, this.classOfModel);
	}
	
	/**
	 * Cria uma query a partir de um filtro e a partir de uma função sql
	 * @param filter objeto json com os campos do filtro. Ex:/ {"nome":"Everton de Vargas Agilar", "ativo":true}
	 * @param fields lista de campos que devem retornar ou o objeto inteiro se vazio. Ex: "nome, cpf, rg"
	 * @param limit Quantidade objetos trazer na pesquisa
	 * @param offset A partir de que posição. Iniciando em 1
	 * @param sort Trazer ordenado por quais campos o conjunto de dados
	 * @param listFunction Lista com a funçao sql e o nome do atributo, nesta ordem. Ex:/ {"count", "idObjeto"}
	 * @return query com o filtro e a função sql
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public Query parseQuery(final String filter, 
							 final String fields, 
							 int limit, 
							 int offset, 
							 final String sort, 
							 final List<String> listFunction,
							 final Class<Model> classOfModel){
		Query query = null;
		StringBuilder field_smnt = null;
		StringBuilder where = null;
		StringBuilder sort_smnt = null;
		String sqlFunction = null;
		Map<String, Object> filtro_obj = null;		
		Field idField = null;

		if (!(limit > 0 && limit <= 999999999)){
			throw new EmsValidationException("Parâmetro limit da pesquisa fora do intervalo permitido. Deve ser maior que zero e menor ou igual que 999999999");
		}

		if (!(offset >= 0 && offset < 999999999)){
			throw new EmsValidationException("Parâmetro offset da pesquisa fora do intervalo permitido. Deve ser maior que zero e menor que 999999999");
		}
		
		// Define o filtro da query se foi informado
		if (filter != null && filter.length() > 5){
			try{
				boolean useAnd = false; 
				filtro_obj = (Map<String, Object>) EmsUtil.fromJson(filter, HashMap.class);
				where = new StringBuilder("where ");
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
						if (classOfModel != this.classOfModel){
							idField = EmsUtil.findFieldByAnnotation(classOfModel, Id.class);
							if (idField == null) {
								throw new EmsValidationException("Classe " + classOfModel.getSimpleName() + " não tem id.");
							}
						}else{
							idField = this.IdField;
						}
						fieldName = idField.getName();
					}else{
						try{
							// Verifica se o campo existe. Uma excessão ocorre se não existir
							classOfModel.getDeclaredField(fieldName);
						}catch (Exception ex){
							throw new EmsValidationException("Campo de pesquisa " + fieldName + " não existe.");
						}
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
							where.append(fieldName).append(sqlOperator).append("?");
						}else{
							fieldName = String.format("this.%s", fieldName);
							where.append(fieldName).append(sqlOperator).append("?");
						}
					}else{
						fieldName = String.format("this.%s", fieldName);
						where.append(fieldName).append(sqlOperator).append("?");
					}
					useAnd = true;
				}
			}catch (Exception e){
				throw new EmsValidationException("Filtro da pesquisa inválido. Erro interno: "+ e.getMessage());
			}
		}

		// Formata a lista de campos 
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
				.append(" from ").append(classOfModel.getSimpleName()).append(" this ");
			} else {
				 sqlBuilder = new StringBuilder("select ")
				.append(sqlFunction)
				.append(" from ").append(classOfModel.getSimpleName()).append(" this ");
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
	
	
	protected Query createNamedQuery(final String namedQuery, final String sql) {
		Query query = null;
		if (cachedNamedQuery.contains(namedQuery)){
			query = entityManager.createNamedQuery(namedQuery);	
		}else{
			cachedNamedQuery.add(namedQuery);
			query = entityManager.createQuery(sql); 
			entityManagerFactory.addNamedQuery(namedQuery, query);
			logger.info("Build named query: "+ namedQuery);
			logger.info("\tSQL: "+ sql);
		}
		return query;
	}
	
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
		Table tableAnnotation = classOfModel.getAnnotation(Table.class);
		UniqueConstraint[] tableContrains = tableAnnotation.uniqueConstraints();
		List<Field> fieldsConstraints = EmsUtil.getFieldsWithUniqueConstraint(classOfModel);
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
				sql.append(idFieldName).append("=:").append(idFieldName).append(" and (");
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
			if (tableConstraintsCount > 0){
				sql.append(" or ");
			}
			sql.append("(");
			for (int i = 0; i < fieldConstraintsCount; i++){
				Field field = fieldsConstraints.get(i);
				String f = field.getAnnotation(Column.class).name();
				sql.append("this.").append(f).append("=:").append(f);
				if (i+1 < fieldConstraintsCount){
					sql.append(" or ");
				}
			}
			sql.append(")");
			
			if (!isInsert){
				sql.append(")");
			}
			
			return sql.toString();
		}
		
		// return null porque não há constraint na declaração do modelo
		return null;
	}
	
}





