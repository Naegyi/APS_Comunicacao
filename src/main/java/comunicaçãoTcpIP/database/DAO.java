package comunicaçãoTcpIP.database;

import java.sql.*;

public class DAO {

    public boolean registrarUsuario(String nome, String senha) {
        String sql = "INSERT INTO usuarios(nome, senha) VALUES(?, ?)";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nome);
            pstmt.setString(2, senha);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao registrar: " + e.getMessage());
            return false; // Retorna false se o usuário já existir (erro de UNIQUE)
        }
    }

    public boolean autenticar(String nome, String senha) {
        String sql = "SELECT * FROM usuarios WHERE nome = ? AND senha = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nome);
            pstmt.setString(2, senha);
            ResultSet rs = pstmt.executeQuery();
            
            return rs.next(); // Se houver um resultado, o login é válido
        } catch (SQLException e) {
            System.err.println("Erro ao autenticar: " + e.getMessage());
            return false;
        }
    }
}