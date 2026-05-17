package comunicaçãoTcpIP.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Configuração inicial do banco de dados.
 * Responsável por criar as tabelas necessárias para o funcionamento do chat
 * caso elas ainda não existam.
 * 
 * As tabelas criadas são:
 * - usuarios: armazena os usuários cadastrados (nome e senha)
 * - mensagens: armazena todas as mensagens enviadas (texto, arquivo, etc.)
 * - grupos: lista os grupos de conversa
 * - membros_grupo: relaciona usuários com grupos (muitos-para-muitos)
 */
public class DataBaseConfig {

    // Comandos SQL para criação das tabelas (strings simples, sem text blocks)
    private static final String[] COMANDOS_SQL = {
        "CREATE TABLE IF NOT EXISTS usuarios (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "nome TEXT NOT NULL UNIQUE, " +
        "senha TEXT NOT NULL);",

        "CREATE TABLE IF NOT EXISTS mensagens (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "remetente TEXT NOT NULL, " +
        "destinatario TEXT NOT NULL, " +
        "conteudo TEXT, " +
        "caminho_arquivo TEXT, " +
        "tipo TEXT, " +
        "data_envio DATETIME DEFAULT CURRENT_TIMESTAMP);",

        "CREATE TABLE IF NOT EXISTS grupos (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "nome TEXT NOT NULL);",

        "CREATE TABLE IF NOT EXISTS membros_grupo (" +
        "grupo_id INTEGER, " +
        "usuario_id INTEGER, " +
        "PRIMARY KEY(grupo_id, usuario_id), " +
        "FOREIGN KEY(grupo_id) REFERENCES grupos(id), " +
        "FOREIGN KEY(usuario_id) REFERENCES usuarios(id));"
    };

    /**
     * Inicializa o banco de dados, criando todas as tabelas necessárias.
     * Se as tabelas já existirem, o comando CREATE TABLE IF NOT EXISTS não faz nada.
     */
    public static void inicializarBanco() {
        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement()) {

            for (String sql : COMANDOS_SQL) {
                stmt.execute(sql);
            }
            System.out.println("[DB] Banco de dados pronto e tabelas verificadas.");

        } catch (SQLException e) {
            System.err.println("[DB] Erro ao configurar tabelas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}