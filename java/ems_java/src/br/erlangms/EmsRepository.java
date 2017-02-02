/*********************************************************************
 * @title EmsDao
 * @version 1.0.0
 * @doc Classe de dao simples
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/

package br.erlangms;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Query;

import org.jinq.jpa.JPAJinqStream;
import org.jinq.jpa.JinqJPAStreamProvider;

public abstract class EmsRepository<Model> {
	public abstract Class<Model> getClassOfModel();
	public abstract EntityManager getEntityManager();
	private String SQL_DELETE;

	public EmsRepository(){
		doCreateCacheSQL();
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
		Query query = null;
		StringBuilder field_smnt = null;
		StringBuilder where = null;
		StringBuilder sort_smnt = null;
		Map<String, Object> filtro_obj = null;		
		Field idField = null;

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
						throw new EmsValidationException("Campo de pesquisa "+ field + " inválido");
					}
					if (fieldName.equals("pk")){
						idField = EmsUtil.findFieldByAnnotation(getClassOfModel(), Id.class);
						fieldName = idField.getName();
					}else{
						try{
							// Verifica se o campo existe. Uma excessão ocorre se não existir
							getClassOfModel().getDeclaredField(fieldName);
						}catch (Exception ex){
							throw new EmsValidationException("Campo de pesquisa " + fieldName + " não existe");
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
		
		
		try{
			// formata o sql
			StringBuilder sql = new StringBuilder("select ")
				.append(field_smnt == null ? " this " : field_smnt.toString())
				.append(" from ").append(getClassOfModel().getSimpleName()).append(" this ");
			if (where != null){
				sql.append(where.toString());
			}	
			if (sort_smnt != null){
				sql.append(sort_smnt.toString());
			}	
			query = getEntityManager().createQuery(sql.toString());
		}catch (Exception e){
			throw new EmsValidationException("Não foi possível criar a query da pesquisa. Erro interno: "+ e.getMessage());
		}

		// Seta os parâmetros da query para cada campo do filtro
		if (where != null){
			EmsUtil.setQueryParameterFromMap(query, filtro_obj);
		}

		if (!(limit > 0 && limit <= 999999999)){
			throw new EmsValidationException("Parâmetro limit da pesquisa fora do intervalo permitido. Deve ser maior que zero e menor ou igual que 999999999");
		}

		if (!(offset >= 0 && offset < 999999999)){
			throw new EmsValidationException("Parâmetro offset da pesquisa fora do intervalo permitido. Deve ser maior que zero e menor que 999999999");
		}

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
	 * @param filter_map HashMap com os campos do filtro. Ex:/ {idAluno = 12345678, idDisciplina = 321654}
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
	 * @return objeto ou EmsNotFoundException se não existe o id
	 * @author Everton de Vargas Agilar
	 */
	public Model findById(final Integer id){
		if (id != null && id >= 0){
			Class<Model> classOfModel = getClassOfModel();
			Model obj = getEntityManager().find(classOfModel, id);
			if (obj == null){
				throw new EmsNotFoundException(classOfModel.getSimpleName() + " não encontrado: "+ id.toString());
			}
			return obj;
		}else{
			throw new EmsValidationException("Parâmetro id não pode ser null para EmsRepository.findById.");
		}
	}

	/**
	 * Recuperar um objeto pelo seu id
	 * @param classOfModel classe do objeto
	 * @param id identificador do objeto
	 * @return objeto ou EmsNotFoundException se não existe o id
	 * @author Everton de Vargas Agilar
	 */
	public <T> T findById(final Class<T> classOfModel, final Integer id){
		if (classOfModel != null && id != null && id >= 0){
			T obj = getEntityManager().find(classOfModel, id);
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
			EntityManager em = getEntityManager();
			Integer idValue = EmsUtil.getIdFromObject(obj);
			if (idValue != null && idValue >= 0){
				em.merge(obj);
			}else{
				throw new EmsValidationException("Não é possível atualizar objeto sem id em EmsRepository.update.");
			}
			em.flush();
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
			EntityManager em = getEntityManager();
			Integer idValue = EmsUtil.getIdFromObject(obj);
			if (idValue != null && idValue >= 0){
				throw new EmsValidationException("Não é possível incluir objeto que já possui id em EmsRepository.insert.");
			}else{
				em.persist(obj);
			}
			em.flush();
			return obj;
		}else{
			throw new EmsValidationException("Parâmetro obj não pode ser null para EmsRepository.insert.");
		}
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
			EntityManager em = getEntityManager();
			Integer idValue = EmsUtil.getIdFromObject(obj);
			if (idValue != null && idValue >= 0){
				em.merge(obj);
			}else{
				em.persist(obj);
			}
			em.flush();
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
			return getEntityManager()
				.createQuery(SQL_DELETE)
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
			String idFieldName = EmsUtil.findFieldByAnnotation(classOfModel, Id.class).getName();
			String sql = new StringBuilder("delete from ")
								.append(classOfModel.getSimpleName())
								.append(" where ")
								.append(idFieldName).append("=:pId").toString();
			return getEntityManager()
					.createQuery(sql)
					.setParameter("pId", id)
					.executeUpdate() > 0;
		}else{
			throw new EmsValidationException("Parâmetros classOfModel e id do método EmsRepository.delete não podem ser null");
		}
	}

	/**
	 * Um método para criar as contantes de sql internas do repositóro
	 * @author Everton de Vargas Agilar
	 */
	private void doCreateCacheSQL(){
		Class<Model> classOfModel = getClassOfModel();
		if (classOfModel != null){
			Field IdField = EmsUtil.findFieldByAnnotation(classOfModel, Id.class);
			if (IdField != null){
				String idFieldName = IdField.getName();
				SQL_DELETE = new StringBuilder("delete from ")
										.append(getClassOfModel().getSimpleName())
										.append(" where ")
										.append(idFieldName).append("=:pId").toString();
			}else{
				throw new EmsValidationException("O modelo "+ classOfModel.getSimpleName() + " não possui nenhum campo com a anotação @Id.");
			}
		}else{
			throw new EmsValidationException("Não foi implementado getClassOfModel() para a classe "+ getClass().getSimpleName());
		}
		createCacheSQL();
	}
	
	/**
	 * Um método para criar as contantes de sql
	 * @author Everton de Vargas Agilar
	 */
	protected void createCacheSQL() {
	}
	
}

