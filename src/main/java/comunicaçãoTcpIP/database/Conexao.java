package comunicaçãoTcpIP.database;

import java.sql.*;


public class Conexao {
	
	private static final String url = "jdbc:sqlite:skynet.db";
	
	
	public static Connection conectar() {
		try{
			Connection conexao = DriverManager.getConnection(url);
			return conexao;
		}catch(SQLException e){
			System.err.println("Erro ao Conectar ao Banco de Dados: " + e.getMessage());
			return null;
		}
		
	}
}
