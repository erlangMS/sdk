package br.erlangms.samples.pojo;

import java.util.Date;

public class Pessoa {
	private int id;
	private String nome;
	private int idade;
	private String cpf;
	private String cidade;
	private Date dataNascimento;
 
	public Pessoa(int id, String nome, int idade, String cpf, String cidade, Date dataNascimento){
		this.id = id;
		this.nome = nome;
		this.idade = idade;
		this.cpf = cidade;
		this.dataNascimento = dataNascimento;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public int getIdade() {
		return idade;
	}
	public void setIdade(int idade) {
		this.idade = idade;
	}
	public String getCpf() {
		return cpf;
	}
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
	public String getCidade() {
		return cidade;
	}
	public void setCidade(String cidade) {
		this.cidade = cidade;
	}
	public Date getDataNascimento() {
		return dataNascimento;
	}
	public void setDataNascimento(Date dataNascimento) {
		this.dataNascimento = dataNascimento;
	}
}

