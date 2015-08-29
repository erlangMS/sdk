package br.erlangms.negocio;

import java.util.List;

import br.erlangms.dao.PessoaDao;
import br.erlangms.pojo.Pessoa;

public class PessoaNegocio {
	public List<Pessoa> getLista(){
		PessoaDao dao = new PessoaDao();
		return dao.getLista();
	}

	public Pessoa findById(int id) {
		PessoaDao dao = new PessoaDao();
		return dao.findById(id);
	}
}
