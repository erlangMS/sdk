/*********************************************************************
 * @title EmsNegocio
 * @version 1.0.0
 * @doc Classe de negócio simples
 * @author Everton de Vargas Agilar <evertonagilar@gmail.com>
 * @copyright ErlangMS Team
 *********************************************************************/

package br.erlangms;

import java.io.Serializable;
import java.util.List;

public abstract class EmsNegocio<T extends Serializable> {
	
	public EmsNegocio() {
		super();
	}
	
	public abstract EmsDao<T> getDao();

	public List<T> pesquisar(String filtro, String fields, int limit_ini, int limit_fim, String sort){
		return getDao().pesquisar(filtro, fields, limit_ini, limit_fim, sort);
	}

	public T findById(Serializable id){
		return getDao().findById(id);
	}
	
	public List<String> update(T obj){
		List<String> validations = valida_objeto(obj);
		if (validations == null){
			getDao().update(obj);
		}
		return validations;
	}

	/*
	 * Valida o objeto
	 * @return retorna a lista de validações ou null se estiver tudo ok 
	 */
	public List<String> valida_objeto(T obj) {
		return null;
	}
}
