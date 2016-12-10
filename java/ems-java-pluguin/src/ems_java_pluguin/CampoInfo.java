package ems_java_pluguin;

public class CampoInfo {
	public String nome = "";
	public String tipo = "";
	public String tipoJava = "";
	public String caption = "";
	public String nomeVar = "";
	public Integer maxlength;
	public Boolean obrigatorio = false;
	public Boolean permiteNulo = true;
	public String regra = "InputText";
	public String valorDefault = "";
	public Integer max;
	public Integer min;
	public Boolean filtravel = false;
	public Boolean mapear = true;
	public String widget = "InputText";
	public Boolean primarykey = false;
	public Boolean unique = false;
	
	public CampoInfo(String nome, String tipo, String nomeTabela, String prefix){
		this.nome = nome;
		this.tipo = tipo;
		if (this.tipo.equals("smallint")){
			this.tipoJava = "Integer";
		} else if (this.tipo.equals("int")){
			this.tipoJava = "Integer";
		} else if (this.tipo.equals("int identity")){
			this.tipoJava = "@Id Integer";
			this.primarykey = true;
			this.unique = true;
		} else if (this.tipo.equals("varchar")){
			this.tipoJava = "String";
		} else if (this.tipo.equals("datetime")){
			this.tipoJava = "java.util.Date";
		} else if (this.tipo.equals("bit")){
			this.tipoJava = "Boolean";
		} else if (this.tipo.equals("char")){
			this.tipoJava = "String";
		} else if (this.tipo.equals("money")){
			this.tipoJava = "Double";
		} else if (this.tipo.equals("ntext")){
			this.tipoJava = "String";
		} else{
			this.tipoJava = "String";
		}
		this.caption = this.nome;
		this.nomeVar = this.nome;
		if (this.nomeVar.substring(0, 3).equals(prefix)){
			this.nomeVar = this.nomeVar.substring(3);
		}
		
        String nomeTabelaSemPrefixo = nomeTabela.substring(3);
		if (this.nomeVar.endsWith(nomeTabelaSemPrefixo)){
			this.nomeVar = this.nomeVar.substring(0, this.nomeVar.length()-nomeTabelaSemPrefixo.length());
		}
		
		this.nomeVar = this.nomeVar.substring(0, 1).toLowerCase() + this.nomeVar.substring(1);  
		this.mapear = true;
		this.filtravel = false;
	}
}