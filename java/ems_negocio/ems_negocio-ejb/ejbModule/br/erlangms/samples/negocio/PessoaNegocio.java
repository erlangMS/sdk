package br.erlangms.samples.negocio;

import java.util.List;

import br.erlangms.samples.dao.PessoaDao;
import br.erlangms.samples.pojo.Pessoa;

public class PessoaNegocio {
	public List<Pessoa> getLista(){
		PessoaDao dao = new PessoaDao();
		return dao.getLista();
	}

	public Pessoa findById(int id) {
		PessoaDao dao = new PessoaDao();
		return dao.findById(id);
	}

	public void update(Pessoa pessoa) {
		PessoaDao dao = new PessoaDao();
		dao.update(pessoa);
	}

	public void insert(Pessoa pessoa) {
		PessoaDao dao = new PessoaDao();
		dao.insert(pessoa);
	}
}
