#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import br.erlangms.EmsUtil;
import br.erlangms.EmsValidationException;

@Entity
@Table(name="TBDocumentacao")
public class Documentacao implements Serializable {

	private static final long serialVersionUID = -4121261049751377228L;

	@Id
    @Column(name = "id", nullable = false, insertable = true, updatable = true)
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

    @Column(name = "DocDenominacao", nullable = false, insertable = true, updatable = true, length = 100, unique = true)
    private String denominacao;
    
    @Column(name = "DocAtivo", nullable = false, insertable = true, updatable = true)
    private boolean ativo = true;

	public Integer getId() {
		return id;
	}

	public String getDenominacao() {
		return denominacao;
	}

	public void setDenominacao(String denominacao) {
		this.denominacao = denominacao;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public void validar() {
		EmsValidationException erro = new EmsValidationException();

		if (!EmsUtil.isFieldStrValid(denominacao)){
			erro.addError("Informe uma denominação.");
		}
		
		// outras validações aqui...
		
		if(erro.getErrors().size() > 0) {
			throw erro;
		}
		
	}


    
}
