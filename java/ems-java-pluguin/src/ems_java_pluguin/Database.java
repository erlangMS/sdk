package ems_java_pluguin;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {

	public Database(){
		
	}
	
    private String url = "jdbc:jtds:sqlserver://desenvbd.ad-fub.unb.br;databaseName=%s";  
    private String driver = "net.sourceforge.jtds.jdbc.Driver";  
    private Connection con = null;
      
    public void conecta (String databaseName, String user, String pwd) throws Exception{  
        try {  
            url = String.format(url, databaseName);
        	Class.forName(driver);  
            con = DriverManager.getConnection(url, user, pwd);  
            con.setAutoCommit(false);  
            getListaDatabases();
        } catch (Exception e) {  
            e.printStackTrace();
        	throw new Exception("Erro interno: " + e.getMessage());
        }
    }
    
    
    public List<String> getListaDatabases() throws SQLException{
    	ArrayList<String> result = new ArrayList<String>();
        ResultSet rs = con.getMetaData().getCatalogs();
        while (rs.next()) {
            String databaseName = rs.getString("TABLE_CAT");
        	result.add(databaseName);
        }
    	return result;
    }
    
    
    public List<String> getListaNomeTabela(String nomeDatabase) throws SQLException{
    	ArrayList<String> result = new ArrayList<String>();
        DatabaseMetaData md = con.getMetaData();
        ResultSet rs = null;
        
        try {
        	rs = md.getTables(nomeDatabase, "dbo", "TB_%", null);
        } catch (Exception e){
        	return result;
        }
        		
        while (rs.next()) {
          String nomeTabela = rs.getString(3);
          result.add(nomeTabela);
        }
        return result;
    }
    
    
    public List<CampoInfo> getListaCamposTabela(String nomeTabela) throws SQLException{
    	ArrayList<CampoInfo> result = new ArrayList<CampoInfo>();
    	Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM " + nomeTabela + " WHERE 1 = 2");
        ResultSetMetaData md = rs.getMetaData();
        int col = md.getColumnCount();

        // Tenta encontrar o prefixo da tabela
        int prefixCount = 0;
        String prefix = "", ultPrefix = "";
        for (int i = 1; i <= col; i++){
        	String col_name = md.getColumnName(i);
        	prefix = col_name.substring(0, 3);
        	if (i > 1){
        		if (prefix.equals(ultPrefix)){
        			prefixCount++;
        		}
        	}else{
        		ultPrefix = prefix;
        	}
        }
        
        for (int i = 1; i <= col; i++){
        	String col_name = md.getColumnName(i);
        	String col_tipo = md.getColumnTypeName(i);
        	result.add(new CampoInfo(col_name, col_tipo, nomeTabela, prefix));
        }
    	return result;
    }
    
    
        
}  