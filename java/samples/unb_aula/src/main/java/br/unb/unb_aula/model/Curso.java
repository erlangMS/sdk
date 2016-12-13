package br.unb.unb_aula.model;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import br.erlangms.EmsUtil;
import br.erlangms.EmsValidationException;

@Entity
@Table(name="TBCurso")
public class Curso implements Serializable {

	private static final long serialVersionUID = -4121261049751377228L;

	@Id
    @Column(name = "id", nullable = false, insertable = true, updatable = true)
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

    @Column(name = "CurDenominacao", nullable = false, insertable = true, updatable = true, length = 100, unique = true)
    private String denominacao;
    
    @Column(name = "CurDataInicio", nullable = true)
    private Date dataInicio;

    @Column(name = "CurDataFim", nullable = true)
    private Date dataFim;    
    
    @Column(name = "CurAtivo", nullable = false, insertable = true, updatable = true)
    private boolean ativo = true;

    @Column(name = "CurValor", nullable = false, insertable = true, updatable = true, precision = 10)
    private Double valor;
    
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
	
	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public Double getValor() {
		return valor;
	}

	public void setValor(Double valor) {
		this.valor = valor;
	}

	public void validar() {
		EmsValidationException erro = new EmsValidationException();

		if (!EmsUtil.isFieldStrValid(denominacao)){
			erro.addError("Informe a denominação do curso.");
		}
		
		if (!EmsUtil.isDateValid(getDataInicio())){
			erro.addError("Informe a data de início do curso.");
		}

		if(!EmsUtil.isDateFinalAfterDateInitial(getDataInicio(), getDataFim()))  {
			erro.addError("A data fim do curso deve ser maior que a data de início.");
		}

		if(getValor() == null) {
			setValor(0.0);
		}else{
			if (getValor() < 0.00 || getValor() > 9999.00) {
				erro.addError("O valor do curso está fora do intervalo permitido.");
			}
		}

		// outras validações aqui...
		
		if(erro.getErrors().size() > 0) {
			throw erro;
		}
		
	}
    
}
