/*********************************************************************
 * @title EmsDao
 * @version 1.0.0
 * @doc Classe de dao simples
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/

package br.erlangms;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Query;

public abstract class EmsDao<T> {
	public abstract Class<T> getClassOfPojo();
	protected abstract EntityManager getEntityManager();
	
	/**
	 * Pesquisa um objeto/recurso a partir de um filtro
	 * Obs: Desenvolvido para suporte ao ErlangMS
	 * @param filtro objeto json com os campos do filtro. Ex:/ {"nome":"Everton de Vargas Agilar", "ativo":true}
	 * @param fields lista de campos que devem retornar ou o objeto inteiro se vazio. Ex: "nome, cpf, rg"
	 * @param limit_ini Paginador inicial dos registros 
	 * @param limit_ini Paginador final dos registros
	 * @param sort trazer ordenado por quais campos o conjunto de dados
	 * @return lista dos objetos
	 * @author Everton de Vargas Agilar
	 */
	@SuppressWarnings("unchecked")
	public List<T> find(String filtro, String fields, int limit_ini, int limit_fim, String sort){
		Query query = null;
		StringBuilder field_smnt = null;
		StringBuilder where = null;
		StringBuilder sort_smnt = null;
		Map<String, Object> filtro_obj = null;		
		Field idField = EmsUtil.findFieldByAnnotation(getClassOfPojo(), Id.class);

		// Define o filtro da query se foi informado
		if (filtro != null && !filtro.isEmpty() && !filtro.equals("{}")){
			try{
				boolean useAnd = false; 
				filtro_obj = (Map<String, Object>) EmsUtil.fromJson(filtro, HashMap.class);
				where = new StringBuilder("where ");
				for (String field : filtro_obj.keySet()){
					if (useAnd){
						where.append(" and ");
					}
					if (field.equals("pk")){
						where.append("this.").append(idField.getName()).append("=?");
					}else{
						where.append("this.").append(field).append("=?");
					}
					useAnd = true;
				}
			}catch (Exception e){
				throw new IllegalArgumentException("Filtro da pesquisa inválido. Erro interno: "+ e.getMessage());
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
				throw new IllegalArgumentException("Lista de campos da pesquisa inválido. Erro interno: "+ e.getMessage());
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
				throw new IllegalArgumentException("Definição sort inválido. Erro interno: "+ e.getMessage());
			}
		}
		
		
		try{
			// formata o sql
			StringBuilder sql = new StringBuilder("select ")
				.append(field_smnt == null ? " this " : field_smnt.toString())
				.append(" from ").append(getClassOfPojo().getSimpleName()).append(" this ");
			if (where != null){
				sql.append(where.toString());
			}	
			if (sort_smnt != null){
				sql.append(sort_smnt.toString());
			}	
			query = getEntityManager().createQuery(sql.toString());
		}catch (Exception e){
			throw new IllegalArgumentException("Não foi possível criar a query da pesquisa. Erro interno: "+ e.getMessage());
		}

		// Seta os parâmetros da query para cada campo do filtro
		if (where != null){
			EmsUtil.setQueryParameterFromMap(query, filtro_obj);
		}

		// Define o limite inicial e final do resultado
		if (limit_ini >= 0 && limit_fim >= limit_ini && limit_fim <= 999999999){
			query.setFirstResult(limit_ini);
			query.setMaxResults(limit_fim-limit_ini+1);
		}else{
			throw new IllegalArgumentException("Limite final para pesquisa deve ser maior que limite inicial.");
		}
		
		List<T> result = query.getResultList();
		return result;
	}

	/**
	 * Recuperar um objeto/recurso pelo seu id
	 * Obs: Desenvolvido para suporte ao ErlangMS
	 * @param id identificador do objeto/recurso
	 * @return objeto/recurso ou EmsNotFoundException se não existe o id
	 * @author Everton de Vargas Agilar
	 */
	public T findById(Serializable id){
		if (id != null){
			Class<T> classOfPojo = getClassOfPojo();
			T obj = getEntityManager().find(getClassOfPojo(), id);
			if (obj == null){
				throw new EmsNotFoundException("Id "+ id.toString() + " para "+ classOfPojo.getName() + " não encontrado.");
			}
			return obj;
		}else{
			throw new IllegalArgumentException("Argumento obj inválido para findById.");
		}
	}

	/**
	 * Persiste as modificações de um objeto/recurso no banco
	 * Obs: Desenvolvido para suporte ao ErlangMS
	 * @param obj objeto/recurso
	 * @param update_values Map com os dados modificados
	 * @return objeto/recurso
	 * @author Everton de Vargas Agilar
	 */
	public T update(T obj){
		if (obj != null){
			getEntityManager().merge(obj);
			getEntityManager().flush();
			return obj;
		}else{
			throw new IllegalArgumentException("Argumento obj inválido para update.");
		}
	}

	/**
	 * Insere um novo objeto/recurso no banco
	 * @param obj objeto/recurso
	 * @param update_values Map com os dados modificados
	 * @return objeto/recurso
	 * @author Everton de Vargas Agilar
	 */
	public T insert(T obj){
		if (obj != null){
			getEntityManager().persist(obj);
			getEntityManager().flush();
			return obj;
		}else{
			throw new IllegalArgumentException("Argumento obj inválido para insert.");
		}
	}

		
}

