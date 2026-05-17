package comunicaçãoTcpIP.view;

import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import comunicaçãoTcpIP.database.DAO;
import comunicaçãoTcpIP.rede.Cliente;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;

/**
 * Tela de login do sistema de chat.
 * Responsável por autenticar o usuário, conectar ao servidor e abrir a tela principal.
 */
public class Login extends JFrame {

    private static final long serialVersionUID = 1L;

    // Constantes para evitar números mágicos no layout
    private static final int LARGURA_JANELA = 475;
    private static final int ALTURA_JANELA = 400;
    private static final int PORTA_SERVIDOR = 54321;

    // Componentes da UI
    private JTextField textFUsuario;
    private JPasswordField passwordField;
    private JTextField textFServerIP;

    // -------------------------------------------------------------
    // Main (ponto de entrada)
    // -------------------------------------------------------------
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Login frame = new Login();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // -------------------------------------------------------------
    // Construtor: cria e organiza a interface
    // -------------------------------------------------------------
    public Login() {
        inicializarUI();
    }

    /**
     * Configura todos os componentes visuais da tela.
     * Layout absoluto (null) mantido para preservar a aparência original,
     * mas organizado em métodos menores.
     */
    private void inicializarUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setBounds(100, 100, LARGURA_JANELA, ALTURA_JANELA);
        setTitle("Login - Chat APS");

        // Painel principal (fundo preto)
        JPanel contentPane = new JPanel();
        contentPane.setBackground(Color.BLACK);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // Painel interno com fundo cinza escuro (área do formulário)
        JPanel jpLogin = new JPanel();
        jpLogin.setBackground(new Color(32, 32, 32));
        jpLogin.setBounds(10, 10, 441, 340);
        jpLogin.setLayout(null);
        contentPane.add(jpLogin);

        // Título "LOGIN"
        JLabel lblLogin = new JLabel("LOGIN");
        lblLogin.setBounds(207, 20, 86, 20);
        lblLogin.setForeground(Color.GREEN);
        lblLogin.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        jpLogin.add(lblLogin);

        // Campo: Usuário
        JLabel lblUsuario = new JLabel("Usuário:");
        lblUsuario.setBounds(80, 70, 70, 20);
        lblUsuario.setForeground(Color.GREEN);
        lblUsuario.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        jpLogin.add(lblUsuario);

        textFUsuario = new JTextField();
        textFUsuario.setBounds(160, 68, 220, 25);
        textFUsuario.setColumns(10);
        jpLogin.add(textFUsuario);

        // Campo: Senha
        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setBounds(80, 110, 70, 20);
        lblSenha.setForeground(Color.GREEN);
        lblSenha.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        jpLogin.add(lblSenha);

        passwordField = new JPasswordField();
        passwordField.setBounds(160, 108, 220, 25);
        jpLogin.add(passwordField);

        // Campo: IP do servidor (permite conectar a servidores remotos)
        JLabel lblServerIP = new JLabel("Servidor IP:");
        lblServerIP.setBounds(80, 150, 80, 20);
        lblServerIP.setForeground(Color.GREEN);
        lblServerIP.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        jpLogin.add(lblServerIP);

        textFServerIP = new JTextField();
        textFServerIP.setBounds(160, 148, 220, 25);
        textFServerIP.setText("127.0.0.1"); // valor padrão (localhost)
        textFServerIP.setToolTipText("Digite o IP do servidor (ex: 192.168.1.10 ou 127.0.0.1)");
        jpLogin.add(textFServerIP);

        // Botões serão adicionados no método configurarEventos()
        JButton btnLogar = new JButton("LOGAR");
        btnLogar.setBounds(230, 220, 150, 30);
        btnLogar.setBackground(Color.GREEN);
        btnLogar.setForeground(Color.BLACK);
        btnLogar.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        jpLogin.add(btnLogar);

        JButton btnRegistrar = new JButton("REGISTRAR");
        btnRegistrar.setBounds(60, 220, 150, 30);
        btnRegistrar.setBackground(Color.GREEN);
        btnRegistrar.setForeground(Color.BLACK);
        btnRegistrar.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        jpLogin.add(btnRegistrar);

        // Armazena referências dos botões para uso nos listeners
        // (não precisamos de campos separados, pois os listeners são anônimos)
        // Mas precisamos de acesso a eles, então vamos manter as variáveis locais
        // e configurar os eventos a seguir.
        configurarBotaoLogar(btnLogar);
        configurarBotaoRegistrar(btnRegistrar);
    }

    /**
     * Configura a ação do botão "LOGAR".
     * - Valida os campos
     * - Autentica no banco local (DAO)
     * - Conecta ao servidor via Cliente
     * - Abre a tela principal
     */
    private void configurarBotaoLogar(JButton btnLogar) {
        btnLogar.addActionListener(e -> {
            String usuario = textFUsuario.getText();
            String senha = new String(passwordField.getPassword()); // senha como String
            String serverIP = textFServerIP.getText().trim();

            // Validações de entrada
            if (usuario == null || usuario.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Digite o nome de usuário!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (senha == null || senha.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Digite a senha!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (serverIP.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Digite o IP do servidor!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Autenticação no banco de dados local
            DAO dao = new DAO();
            boolean autenticado = dao.autenticar(usuario, senha);

            if (!autenticado) {
                JOptionPane.showMessageDialog(this,
                        "Usuário ou senha incorretos. Tente novamente ou registre-se.",
                        "Erro de autenticação",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Se autenticado, conecta ao servidor de chat
            JOptionPane.showMessageDialog(this, "Bem-vindo, " + usuario + "!");

            // A classe Cliente é responsável pela comunicação com o servidor.
            // Passamos o IP informado, a porta padrão e o nome do usuário.
            Cliente.conectar(serverIP, PORTA_SERVIDOR, usuario);

            // Pequeno delay para garantir que a conexão foi estabelecida
            // (evita possíveis condições de corrida ao enviar mensagens logo após abrir a tela)
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt(); // restaura o sinal de interrupção
                ex.printStackTrace();
            }

            // Abre a tela principal do chat
            TelaPrincipal framePrincipal = new TelaPrincipal(usuario);
            framePrincipal.setLocationRelativeTo(null);
            framePrincipal.setVisible(true);

            // Fecha a tela de login
            dispose();
        });
    }

    /**
     * Configura a ação do botão "REGISTRAR".
     * Abre a tela de registro e fecha a tela de login.
     */
    private void configurarBotaoRegistrar(JButton btnRegistrar) {
        btnRegistrar.addActionListener(e -> {
            Registro frameRegistro = new Registro();
            frameRegistro.setLocationRelativeTo(null);
            frameRegistro.setVisible(true);
            dispose(); // fecha a janela de login
        });
    }
}