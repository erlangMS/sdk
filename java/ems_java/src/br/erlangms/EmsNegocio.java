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
import java.util.Map;

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
	
	public T update(T obj, Map<String, Object> update_values){
		valida_objeto(obj);
		return getDao().update(obj, update_values);
	}

	public void valida_objeto(T obj) {
	}
}
