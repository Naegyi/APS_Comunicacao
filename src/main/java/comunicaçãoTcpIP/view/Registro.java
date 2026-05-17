package comunicaçãoTcpIP.view;

import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import comunicaçãoTcpIP.database.DAO;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;

/**
 * Tela de registro de novos usuários.
 * Permite criar uma conta no sistema (armazenada localmente via DAO).
 * Após o registro bem-sucedido, retorna à tela de login.
 */
public class Registro extends JFrame {

    private static final long serialVersionUID = 1L;

    // Constantes para layout (evitam números mágicos)
    private static final int LARGURA_JANELA = 450;
    private static final int ALTURA_JANELA = 300;

    // Componentes da interface
    private JTextField textFUsuario;
    private JTextField textFSenha;  // Nota: campo de texto comum (senha visível)
                                    // Mantido por compatibilidade com o código original

    // -------------------------------------------------------------
    // Main (ponto de entrada para teste isolado)
    // -------------------------------------------------------------
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Registro frame = new Registro();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // -------------------------------------------------------------
    // Construtor
    // -------------------------------------------------------------
    public Registro() {
        inicializarUI();
    }

    /**
     * Cria e organiza todos os componentes visuais da tela.
     * Mantém o layout absoluto (null) para preservar a aparência original,
     * mas com dimensões organizadas.
     */
    private void inicializarUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setBounds(100, 100, LARGURA_JANELA, ALTURA_JANELA);
        setTitle("Registro - Chat APS");

        // Painel principal (fundo preto)
        JPanel contentPane = new JPanel();
        contentPane.setBackground(Color.BLACK);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // Painel interno com fundo cinza escuro (área do formulário)
        JPanel jpRegistro = new JPanel();
        jpRegistro.setBackground(new Color(32, 32, 32));
        jpRegistro.setBounds(10, 10, 416, 243);
        jpRegistro.setLayout(null);
        contentPane.add(jpRegistro);

        // Título
        JLabel lblRegistro = new JLabel("REGISTRAR USUÁRIO");
        lblRegistro.setBounds(122, 27, 203, 12);
        lblRegistro.setForeground(Color.GREEN);
        jpRegistro.add(lblRegistro);

        // Rótulo Usuário
        JLabel lblUsuario = new JLabel("Usuário:");
        lblUsuario.setBounds(79, 69, 96, 12);
        lblUsuario.setForeground(Color.GREEN);
        jpRegistro.add(lblUsuario);

        // Campo Usuário
        textFUsuario = new JTextField();
        textFUsuario.setBounds(79, 83, 234, 18);
        textFUsuario.setColumns(10);
        jpRegistro.add(textFUsuario);

        // Rótulo Senha
        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setBounds(79, 111, 44, 12);
        lblSenha.setForeground(Color.GREEN);
        jpRegistro.add(lblSenha);

        // Campo Senha (texto simples, por compatibilidade)
        textFSenha = new JTextField();
        textFSenha.setBounds(79, 124, 234, 18);
        textFSenha.setColumns(10);
        jpRegistro.add(textFSenha);

        // Botão Registrar (a ação será configurada em configurarEventos)
        JButton btnRegistrar = new JButton("REGISTRAR");
        btnRegistrar.setBackground(Color.GREEN);
        btnRegistrar.setBounds(150, 185, 100, 20);
        jpRegistro.add(btnRegistrar);

        // Armazena a referência do botão para uso no listener
        configurarBotaoRegistrar(btnRegistrar);
    }

    /**
     * Configura a ação do botão "REGISTRAR".
     * - Valida os campos de usuário e senha
     * - Chama o DAO para tentar registrar
     * - Em caso de sucesso: mostra mensagem, volta para tela de login e fecha esta tela
     * - Em caso de falha: exibe erro (usuário já existe ou dados inválidos)
     */
    private void configurarBotaoRegistrar(JButton btnRegistrar) {
        btnRegistrar.addActionListener(e -> {
            String usuario = textFUsuario.getText();
            String senha = textFSenha.getText();

            // Validação básica
            if (usuario == null || usuario.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "O campo de usuário não pode estar vazio.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (senha == null || senha.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "O campo de senha não pode estar vazio.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Tenta registrar no banco de dados
            DAO dao = new DAO();
            boolean sucesso = dao.registrarUsuario(usuario, senha);

            if (sucesso) {
                JOptionPane.showMessageDialog(this,
                        "Usuário registrado com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);

                // Retorna para a tela de login
                Login frameLogin = new Login();
                frameLogin.setLocationRelativeTo(null);
                frameLogin.setVisible(true);

                // Fecha a tela de registro
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Erro ao registrar. O nome de usuário pode já existir ou ocorreu um problema interno.",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}