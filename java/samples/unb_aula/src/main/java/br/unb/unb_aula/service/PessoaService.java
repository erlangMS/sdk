package br.unb.unb_aula.service;

import java.util.List;

import javax.ejb.Stateless;

import br.unb.unb_aula.infra.ModInfra;
import br.unb.unb_aula.model.Pessoa;

@Stateless
public class PessoaService {

	public Pessoa findById(Integer id) {
		return ModInfra.getInstance()
			.getPessoaRepository()
			.findById(id);
	}

	public List<Pessoa> find(String filtro, String fields, int limit_ini, int limit_fim, String sort) {
		return ModInfra.getInstance()
			.getPessoaRepository()
			.find(filtro, fields, limit_ini, limit_fim, sort);
	}

	public Pessoa update(Pessoa Pessoa){
		Pessoa.validar();
		return ModInfra.getInstance()
			.getPessoaRepository()
			.update(Pessoa);
	}

	public Pessoa insert(Pessoa Pessoa) {
		Pessoa.validar();
		return ModInfra.getInstance()
			.getPessoaRepository()
			.insert(Pessoa);
	}
	
	public boolean delete(Integer id) {
		return ModInfra.getInstance()
			.getPessoaRepository()
			.delete(id);
	}

	public Double getCredito(Integer id) {
		return ModInfra.getInstance()
			.getPessoaRepository()
			.findById(id)
			.getCredito();
	}
	
	public Double inserirCredito(final Integer id, final Double value) {
		return ModInfra.getInstance()
				.getPessoaRepository()
				.findById(id)
				.inserirCredito(value);
	}
	
}
