package br.erlangms;

import java.util.Date;

public interface IOAuth2Token {
	Long getId();
	void setId(Long id);
	String getToken();
	void setToken(String token);
	Date getData();
	void setData(Date data);
	int hashCode();
	boolean equals(Object obj);
	IUsuario getUsuario();
}