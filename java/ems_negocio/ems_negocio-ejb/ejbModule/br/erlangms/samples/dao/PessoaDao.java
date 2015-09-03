package br.erlangms.samples.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.erlangms.samples.pojo.Pessoa;

public class PessoaDao {
	private static List<Pessoa> list = new ArrayList<Pessoa>();
	private static int sequence = 0;
	
	static{
		list.add(new Pessoa(++sequence, "Everton de Vargas Agilar", 32, "00167743023", "Brasília", Calendar.getInstance().getTime()));
		list.add(new Pessoa(++sequence, "Rafael Stefanello Agilar", 2, "", "Brasília", Calendar.getInstance().getTime()));
	}
	
	public List<Pessoa> getLista(){
		return list;
	}

	public Pessoa findById(int id) {
		return getLista().get(id);
	}

	public void update(Pessoa pessoa) {
		
				
	}

	public void insert(Pessoa pessoa) {
		pessoa.setId(++sequence);
		list.add(pessoa);
	}
}
