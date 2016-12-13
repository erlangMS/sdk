package br.unb.unb_aula.model;

import java.io.Serializable;
import java.util.NoSuchElementException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import br.erlangms.EmsValidationException;
import br.unb.unb_aula.infra.CreditoRepository;
import br.unb.unb_aula.infra.ModInfra;

@Entity
@Table(name="TBPessoa")
public class Pessoa implements Serializable {

	private static final long serialVersionUID = -8332441069417753271L;

	@Id
    @Column(name = "PesCodPesssoa", nullable = false, insertable = true, updatable = true)
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

    @Column(name = "PesDenominacao", nullable = false, insertable = true, updatable = true, length = 100, unique = true)
    private String denominacao;
    
     
	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public String getDenominacao() {
		return denominacao;
	}


	public void setDenominacao(String denominacao) {
		this.denominacao = denominacao;
	}


	public void validar() {
		EmsValidationException erro = new EmsValidationException();


		// outras validações aqui...
		
		if(erro.getErrors().size() > 0) {
			throw erro;
		}
		
	}
	
	public Double getCredito(){
		int pessoa_id = getId();
		try{
			return ModInfra.getInstance()
					.getCreditoRepository()
					.getStreams()
					.where(a -> a.getIdPessoa() == pessoa_id)
					.getOnlyValue().
					getSaldo();
		}catch (NoSuchElementException e){
			return 0.00;
		}
	}
	
	public Double inserirCredito(final Double value){
		CreditoRepository creditoRepository = 
				ModInfra.getInstance()
				.getCreditoRepository();
		int id_pessoa = getId();
		Credito credito = null;
		try{
			// Localiza o registro do crédito e aumenta o saldo
			credito = creditoRepository
						.getStreams()
						.where(a -> a.getIdPessoa() == id_pessoa)
						.getOnlyValue();
			credito.aumentaSaldo(value);
			creditoRepository.update(credito);
		}catch (NoSuchElementException e){
			// Não tem registro de crédito ainda, insere novo
			credito = new Credito();
			credito.aumentaSaldo(value);
			credito.setIdPessoa(id_pessoa);
			creditoRepository.insert(credito);
		}
		return credito.getSaldo();
	}
    
}
