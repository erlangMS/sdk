package br.unb.unb_aula.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import br.erlangms.EmsValidationException;
import br.unb.unb_aula.infra.ModInfra;



@Entity
@Table(name="TBCredito")
public class Credito implements Serializable {

	private static final long serialVersionUID = -4314658678284348133L;

	@Id
    @Column(name = "CreCodCredito", nullable = false, insertable = true, updatable = true)
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

    @Column(name = "CreCodPessoa")
    private Integer idPessoa;
	
	@Column(name = "CreSaldo")
    private Double saldo = 0.00;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIdPessoa() {
		return idPessoa;
	}

	public void setIdPessoa(Integer idPessoa) {
		this.idPessoa = idPessoa;
	}

	public Double getSaldo() {
		return saldo;
	}

	public void aumentaSaldo(Double value) {
		if (value == null || value == 0) 
			throw new EmsValidationException("Valor do crédito não informado.");
		this.saldo = this.saldo + value;
		if (this.saldo > 100){ 
			ModInfra.getInstance().getCreditoRepository().getEntityManager().flush();
			ModInfra.getInstance().getPessoaRepository().getEntityManager().flush();
			throw new EmsValidationException("Não é possível colocar mais que 100 reais de crédito.");
		}
	}
	
}
