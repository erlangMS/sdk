package br.erlangms.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.erlangms.pojo.Pessoa;

public class PessoaDao {
	public List<Pessoa> getLista(){
		List<Pessoa> list = new ArrayList<Pessoa>();
		list.add(new Pessoa("Everton de Vargas Agilar", 32, "00167743023", "Brasília", Calendar.getInstance().getTime()));
		list.add(new Pessoa("Rafael Stefanello Agilar", 2, "", "Brasília", Calendar.getInstance().getTime()));
		return list;
	}

	public Pessoa findById(int id) {
		return getLista().get(id);
	}
}
