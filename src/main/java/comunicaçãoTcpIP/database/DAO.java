package comunicaçãoTcpIP.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import comunicaçãoTcpIP.modelos.Mensagem.TipoMensagem;

public class DAO {

    public boolean registrarUsuario(String nome, String senha) {
        String sql = "INSERT INTO usuarios(nome, senha) VALUES(?, ?)";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nome);
            pstmt.setString(2, senha);
            pstmt.executeUpdate();
            System.out.println("Usuário registrado: " + nome);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao registrar: " + e.getMessage());
            return false;
        }
    }

    public boolean autenticar(String nome, String senha) {
        String sql = "SELECT * FROM usuarios WHERE nome = ? AND senha = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nome);
            pstmt.setString(2, senha);
            ResultSet rs = pstmt.executeQuery();
            
            boolean autenticado = rs.next();
            if (autenticado) {
                System.out.println("Usuário autenticado: " + nome);
            } else {
                System.out.println("Falha na autenticação: " + nome);
            }
            return autenticado;
        } catch (SQLException e) {
            System.err.println("Erro ao autenticar: " + e.getMessage());
            return false;
        }
    }
    
    public void salvarMensagem(String remetente, String destinatario, String conteudo, TipoMensagem tipo, String caminhoArquivo) {
        String sql = "INSERT INTO mensagens(remetente, destinatario, conteudo, caminho_arquivo, tipo) VALUES(?, ?, ?, ?, ?)";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, remetente);
            pstmt.setString(2, destinatario);
            pstmt.setString(3, conteudo);
            pstmt.setString(4, caminhoArquivo);
            pstmt.setString(5, tipo.name());
            
            int linhasAfetadas = pstmt.executeUpdate();
            System.out.println("Mensagem salva no BD! Linhas: " + linhasAfetadas);
            System.out.println("   Remetente: " + remetente + ", Destinatário: " + destinatario);
            System.out.println("   Tipo: " + tipo.name());
            
        } catch (SQLException e) {
            System.err.println("Erro ao salvar mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Sobrecarga para texto (mantém compatibilidade)
    public void salvarMensagem(String remetente, String destinatario, String conteudo, TipoMensagem tipo) {
        salvarMensagem(remetente, destinatario, conteudo, tipo, null);
    }
    
    // BUSCAR HISTÓRICO COMPLETO (com caminho do arquivo) - VERSÃO CORRIGIDA
    public List<Object[]> buscarHistoricoCompleto(String usuarioLogado, String nomeContato) {
        List<Object[]> historico = new ArrayList<>();
        
        // VERIFICA SE É GRUPO
        if (nomeContato != null && nomeContato.endsWith(" (Grupo)")) {
            String nomeGrupo = nomeContato.replace(" (Grupo)", "");
            String sqlGrupo = "SELECT remetente, conteudo, caminho_arquivo, tipo, data_envio " +
                             "FROM mensagens WHERE destinatario = ? ORDER BY id ASC";
            
            try (Connection conn = Conexao.conectar();
                 PreparedStatement pstmt = conn.prepareStatement(sqlGrupo)) {
                
                pstmt.setString(1, "GRUPO_" + nomeGrupo);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Object[] msg = new Object[5];
                    msg[0] = rs.getString("remetente");
                    msg[1] = rs.getString("conteudo");
                    msg[2] = rs.getString("caminho_arquivo");
                    msg[3] = rs.getString("tipo");
                    msg[4] = rs.getString("data_envio");
                    historico.add(msg);
                }
                
                System.out.println("Histórico do grupo '" + nomeGrupo + "': " + historico.size() + " mensagens");
                
            } catch (SQLException e) {
                System.err.println("Erro ao buscar histórico do grupo: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // MENSAGEM NORMAL (entre dois usuários)
            String sql = "SELECT remetente, conteudo, caminho_arquivo, tipo, data_envio FROM mensagens WHERE " +
                        "(remetente = ? AND destinatario = ?) OR (remetente = ? AND destinatario = ?) " +
                        "ORDER BY id ASC";
            
            try (Connection conn = Conexao.conectar();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, usuarioLogado);
                pstmt.setString(2, nomeContato);
                pstmt.setString(3, nomeContato);
                pstmt.setString(4, usuarioLogado);
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Object[] msg = new Object[5];
                    msg[0] = rs.getString("remetente");
                    msg[1] = rs.getString("conteudo");
                    msg[2] = rs.getString("caminho_arquivo");
                    msg[3] = rs.getString("tipo");
                    msg[4] = rs.getString("data_envio");
                    historico.add(msg);
                }
                
                System.out.println("Histórico entre " + usuarioLogado + " e " + nomeContato + ": " + historico.size() + " mensagens");
                
            } catch (SQLException e) {
                System.err.println("Erro ao buscar histórico: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return historico;
    }
    
    // Método antigo para compatibilidade
    public List<String[]> buscarHistorico(String usuarioLogado, String nomeContato) {
        List<String[]> historico = new ArrayList<>();
        
        String sql = "SELECT remetente, conteudo FROM mensagens WHERE " +
                     "(remetente = ? AND destinatario = ?) OR (remetente = ? AND destinatario = ?) " +
                     "ORDER BY id ASC";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, usuarioLogado);
            pstmt.setString(2, nomeContato);
            pstmt.setString(3, nomeContato);
            pstmt.setString(4, usuarioLogado);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                historico.add(new String[]{rs.getString("remetente"), rs.getString("conteudo")});
            }
            
            System.out.println("Histórico carregado: " + historico.size() + " mensagens");
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar histórico: " + e.getMessage());
        }
        
        return historico;
    }
    
    public List<String> listarUsuarios(String usuarioLogado) {
        List<String> usuarios = new ArrayList<>();
        String sql = "SELECT nome FROM usuarios WHERE nome != ? ORDER BY nome ASC";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
             pstmt.setString(1, usuarioLogado);
             ResultSet rs = pstmt.executeQuery();
             
             while (rs.next()) {
                 String nomeUsuario = rs.getString("nome");
                 usuarios.add(nomeUsuario);
             }
             
             System.out.println("Usuários listados (excluindo " + usuarioLogado + "): " + usuarios.size());
             
        } catch (SQLException e) {
            System.err.println("Erro ao listar usuários: " + e.getMessage());
        }
        
        return usuarios;
    }
    
    public List<String> conversas(String usuarioLogado) {
        List<String> usuarios = new ArrayList<>();
        
        // Busca apenas conversas entre usuários (não grupos)
        String sql = "SELECT DISTINCT CASE " +
                     "  WHEN remetente = ? THEN destinatario " +
                     "  ELSE remetente " +
                     "END AS contato " +
                     "FROM mensagens " +
                     "WHERE (remetente = ? OR destinatario = ?) " +
                     "AND destinatario NOT LIKE 'GRUPO_%' " +
                     "AND remetente NOT LIKE 'GRUPO_%' " +
                     "ORDER BY contato ASC";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, usuarioLogado);
            pstmt.setString(2, usuarioLogado);
            pstmt.setString(3, usuarioLogado);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String nomeUsuario = rs.getString("contato");
                if (nomeUsuario != null && !nomeUsuario.equals(usuarioLogado)) {
                    usuarios.add(nomeUsuario);
                }
            }
            
            // Também busca grupos
            String sqlGrupos = "SELECT DISTINCT REPLACE(destinatario, 'GRUPO_', '') || ' (Grupo)' AS grupo " +
                              "FROM mensagens " +
                              "WHERE (remetente = ? OR destinatario LIKE 'GRUPO_%') " +
                              "AND destinatario LIKE 'GRUPO_%'";
            
            try (PreparedStatement pstmtGrupo = conn.prepareStatement(sqlGrupos)) {
                pstmtGrupo.setString(1, usuarioLogado);
                ResultSet rsGrupo = pstmtGrupo.executeQuery();
                
                while (rsGrupo.next()) {
                    String nomeGrupo = rsGrupo.getString("grupo");
                    if (nomeGrupo != null && !usuarios.contains(nomeGrupo)) {
                        usuarios.add(nomeGrupo);
                    }
                }
            }
             
             
        } catch (SQLException e) {
            System.err.println(" Erro ao listar conversas: " + e.getMessage());
        }
        
        return usuarios;
    }

    public List<String> buscarMembrosDoGrupo(String nomeGrupo) {
        List<String> membros = new ArrayList<>();
        String sql = "SELECT u.nome FROM usuarios u " +
                     "INNER JOIN membros_grupo mg ON mg.usuario_id = u.id " +
                     "INNER JOIN grupos g ON g.id = mg.grupo_id " +
                     "WHERE g.nome = ?";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nomeGrupo);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                membros.add(rs.getString("nome"));
            }
            
            System.out.println("Membros do grupo '" + nomeGrupo + "': " + membros.size());
            
        } catch (SQLException e) {
            System.err.println("Erro ao buscar membros do grupo: " + e.getMessage());
        }
        return membros;
    }

    public void salvarMensagemGrupo(String nomeGrupo, String remetente, String conteudo, TipoMensagem tipo) {
        String sql = "INSERT INTO mensagens(remetente, destinatario, conteudo, tipo, caminho_arquivo) VALUES(?, ?, ?, ?, ?)";
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, remetente);
            pstmt.setString(2, "GRUPO_" + nomeGrupo);
            pstmt.setString(3, conteudo);
            pstmt.setString(4, tipo.name());
            pstmt.setString(5, null);
            
            int linhas = pstmt.executeUpdate();
            System.out.println("Mensagem de grupo salva no BD! Grupo: " + nomeGrupo + ", Remetente: " + remetente);
            
        } catch (SQLException e) {
            System.err.println("Erro ao salvar mensagem de grupo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean criarGrupo(String nomeGrupo, List<String> membros) {
        String sqlGrupo = "INSERT INTO grupos(nome) VALUES(?)";
        String sqlMembro = "INSERT INTO membros_grupo(grupo_id, usuario_id) VALUES(?, ?)";
        String sqlBuscarUsuario = "SELECT id FROM usuarios WHERE nome = ?";
        
        try (Connection conn = Conexao.conectar()) {
            conn.setAutoCommit(false);
            
            // Verifica se o grupo já existe
            String sqlCheck = "SELECT id FROM grupos WHERE nome = ?";
            try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck)) {
                pstmtCheck.setString(1, nomeGrupo);
                ResultSet rsCheck = pstmtCheck.executeQuery();
                if (rsCheck.next()) {
                    System.err.println("Grupo já existe: " + nomeGrupo);
                    conn.rollback();
                    return false;
                }
            }
            
            // Insere o grupo
            int grupoId;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlGrupo, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, nomeGrupo);
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    grupoId = rs.getInt(1);
                } else {
                    conn.rollback();
                    return false;
                }
            }
            
            // Adiciona os membros
            try (PreparedStatement pstmtMembro = conn.prepareStatement(sqlMembro);
                 PreparedStatement pstmtUsuario = conn.prepareStatement(sqlBuscarUsuario)) {
                
                for (String membro : membros) {
                    pstmtUsuario.setString(1, membro);
                    ResultSet rs = pstmtUsuario.executeQuery();
                    if (rs.next()) {
                        int usuarioId = rs.getInt("id");
                        pstmtMembro.setInt(1, grupoId);
                        pstmtMembro.setInt(2, usuarioId);
                        pstmtMembro.addBatch();
                    } else {
                        System.err.println("Usuário não encontrado: " + membro);
                    }
                }
                pstmtMembro.executeBatch();
            }
            
            conn.commit();
            System.out.println("Grupo criado com sucesso: " + nomeGrupo + " com " + membros.size() + " membros");
            return true;
            
        } catch (SQLException e) {
            System.err.println("Erro ao criar grupo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean salvarGrupoComMembros(String nomeGrupo, List<String> membros) {
        return criarGrupo(nomeGrupo, membros);
    }
    
    // Método auxiliar para debug - conta mensagens no banco
    public int contarMensagens() {
        String sql = "SELECT COUNT(*) FROM mensagens";
        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int total = rs.next() ? rs.getInt(1) : 0;
            System.out.println("Total de mensagens no banco: " + total);
            return total;
            
        } catch (SQLException e) {
            System.err.println("Erro ao contar mensagens: " + e.getMessage());
            return 0;
        }
    }
    
    // Método auxiliar para debug - lista todas as mensagens
    public void listarTodasMensagens() {
        String sql = "SELECT id, remetente, destinatario, conteudo, tipo, data_envio FROM mensagens ORDER BY id";
        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("=== TODAS AS MENSAGENS NO BANCO ===");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " + 
                                 rs.getString("remetente") + " -> " + 
                                 rs.getString("destinatario") + " | " +
                                 rs.getString("tipo") + " | " +
                                 rs.getString("data_envio"));
                System.out.println("    Conteúdo: " + rs.getString("conteudo"));
            }
            System.out.println("==================================");
            
        } catch (SQLException e) {
            System.err.println("Erro ao listar mensagens: " + e.getMessage());
        }
    }
}	