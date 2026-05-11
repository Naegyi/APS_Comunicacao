package comunicaçãoTcpIP.view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import comunicaçãoTcpIP.database.DAO;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Login extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textFUsuario;
	private JPasswordField passwordField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Login() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 475, 327);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(0, 0, 0));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel jpLogin = new JPanel();
		jpLogin.setBackground(new Color(192, 192, 192));
		jpLogin.setBounds(10, 10, 441, 270);
		contentPane.add(jpLogin);
		jpLogin.setLayout(null);
		
		JLabel lblLogin = new JLabel("Login");
		lblLogin.setBounds(207, 29, 44, 12);
		jpLogin.add(lblLogin);
		
		JLabel lblUsuario = new JLabel("Usuario:");
		lblUsuario.setBounds(114, 68, 60, 12);
		jpLogin.add(lblUsuario);
		
		JLabel lblSenha = new JLabel("Senha:");
		lblSenha.setBounds(114, 105, 44, 12);
		jpLogin.add(lblSenha);
		
		textFUsuario = new JTextField();
		textFUsuario.setBounds(169, 65, 157, 18);
		jpLogin.add(textFUsuario);
		textFUsuario.setColumns(10);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(169, 102, 157, 18);
		jpLogin.add(passwordField);
		
		JButton btnLogar = new JButton("Logar");
		btnLogar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				DAO dao = new DAO();
				String usuario = textFUsuario.getText();
		        String senha = passwordField.getText();
				
				if (textFUsuario.getText()!=null &&
						!textFUsuario.getText().isEmpty() &&
						passwordField.getText()!=null &&
						!passwordField.getText().isEmpty()) {
					
					boolean sucesso = dao.autenticar(usuario, senha);
					
					if (sucesso) {
						JOptionPane.showMessageDialog(btnLogar, "Bem Vindo " + usuario);
						TelaPrincipal framePrincipal = new TelaPrincipal();
						framePrincipal.setLocationRelativeTo(null);
						framePrincipal.setVisible(true);
						dispose();
					} else{
						JOptionPane.showMessageDialog(btnLogar, "Erro ao Logar. O usuário/senha esta incorreto ou não existe.", "Erro", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(btnLogar, "Informações Invalidas", "Aviso", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		btnLogar.setBounds(242, 147, 84, 20);
		jpLogin.add(btnLogar);
		
		JButton btnRegistrar = new JButton("Registrar");
		btnRegistrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Cria a instância da tela de registro
			    Registro frameRegistro = new Registro();
			    // Faz ela aparecer
			    frameRegistro.setVisible(true);
			    // Fecha a tela de login atual
			    dispose();
			}
		});
		btnRegistrar.setBounds(114, 147, 84, 20);
		jpLogin.add(btnRegistrar);

	}
}
