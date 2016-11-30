#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.infra;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import br.erlangms.EmsRepository;
import ${package}.model.Documentacao;

@Stateless
public class DocumentacaoRepository extends EmsRepository<Documentacao> {

	@PersistenceContext(unitName = "service_context")
	public EntityManager saeContext;

	@Override
	public EntityManager getEntityManager() {
		return saeContext;
	}

	@Override
	public Class<Documentacao> getClassOfModel() {
		return Documentacao.class;
	}

}
