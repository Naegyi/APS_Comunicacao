package comunicaçãoTcpIP.database;

import java.sql.*;


public class DataBaseConfig {

    public static void inicializarBanco() {
        // SQL para criar cada uma das tabelas necessárias
        String[] comandosSQL = {
            // Tabela de Usuários
            "CREATE TABLE IF NOT EXISTS usuarios (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "nome TEXT NOT NULL UNIQUE, " +
            "senha TEXT NOT NULL);",

            // Tabela de Mensagens (Geral)
            "CREATE TABLE IF NOT EXISTS mensagens (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "remetente_id INTEGER, " +
            "conteudo TEXT, " +
            "tipo TEXT, " + // TEXTO, IMAGEM, ARQUIVO[cite: 4]
            "data_envio DATETIME DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY(remetente_id) REFERENCES usuarios(id));",

            // Tabela de Grupos
            "CREATE TABLE IF NOT EXISTS grupos (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "nome TEXT NOT NULL);",

            // Tabela Relacional: Quem está em qual grupo
            "CREATE TABLE IF NOT EXISTS membros_grupo (" +
            "grupo_id INTEGER, " +
            "usuario_id INTEGER, " +
            "PRIMARY KEY(grupo_id, usuario_id), " +
            "FOREIGN KEY(grupo_id) REFERENCES grupos(id), " +
            "FOREIGN KEY(usuario_id) REFERENCES usuarios(id));"
        };

        try (Connection conn = Conexao.conectar(); 
             Statement stmt = conn.createStatement()) {
            
            for (String sql : comandosSQL) {
                stmt.execute(sql);
            }
            System.out.println("Banco de dados pronto e tabelas verificadas.");
            
        } catch (SQLException e) {
            System.err.println("Erro ao configurar tabelas: " + e.getMessage());
        }
    }
}
