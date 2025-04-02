package br.erlangms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserHolder {
	Integer id;
	Double codigo;
	String login;
	List<Map<Object, Object>> lista_perfil;
	List<Map<String, Object>> lista_permission;
	Map remap_user_id;
	
	public UserHolder(IOAuth2Token token) {
		super();
		IUsuario usuario = token.getUsuario();
		this.id = usuario.getId();
		this.codigo = Double.valueOf(usuario.getCodigoPessoa().toString()); 
		this.login = usuario.getLogin();
		this.lista_perfil = new ArrayList<>();
		this.lista_permission = new ArrayList<>();
		this.remap_user_id = null;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Double getCodigo() {
		return codigo;
	}

	public void setCodigo(Double codigo) {
		this.codigo = codigo;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public List<Map<Object, Object>> getLista_perfil() {
		return lista_perfil;
	}

	public void setLista_perfil(List<Map<Object, Object>> lista_perfil) {
		this.lista_perfil = lista_perfil;
	}

	public List<Map<String, Object>> getLista_permission() {
		return lista_permission;
	}

	public void setLista_permission(List<Map<String, Object>> lista_permission) {
		this.lista_permission = lista_permission;
	}

	public Map getRemap_user_id() {
		return remap_user_id;
	}

	public void setRemap_user_id(Map remap_user_id) {
		this.remap_user_id = remap_user_id;
	}
	
	
	
}
