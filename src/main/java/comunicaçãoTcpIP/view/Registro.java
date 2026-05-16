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
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Registro extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textFUsuario;
	private JTextField textFSenha;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Registro frame = new Registro();
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
	public Registro() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(0, 0, 0));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel jpRegistro = new JPanel();
		jpRegistro.setBackground(new Color(32, 32, 32));
		jpRegistro.setBounds(10, 10, 416, 243);
		contentPane.add(jpRegistro);
		jpRegistro.setLayout(null);
		
		JLabel lblRegistro = new JLabel("REGISTRAR USUÁRIO");
		lblRegistro.setBounds(122, 27, 203, 12);
		lblRegistro.setForeground(Color.GREEN);
		jpRegistro.add(lblRegistro);
		
		JLabel lblUsuario = new JLabel("Usuário:");
		lblUsuario.setBounds(79, 69, 96, 12);
		lblUsuario.setForeground(Color.GREEN);
		jpRegistro.add(lblUsuario);
		
		JLabel lblNewLabel_1_1 = new JLabel("Senha:");
		lblNewLabel_1_1.setBounds(79, 111, 44, 12);
		lblNewLabel_1_1.setForeground(Color.GREEN);
		jpRegistro.add(lblNewLabel_1_1);
		
		textFUsuario = new JTextField();
		textFUsuario.setBounds(79, 83, 234, 18);
		jpRegistro.add(textFUsuario);
		textFUsuario.setColumns(10);
		
		textFSenha = new JTextField();
		textFSenha.setColumns(10);
		textFSenha.setBounds(79, 124, 234, 18);
		jpRegistro.add(textFSenha);
		
		JButton btnRegistrar = new JButton("REGISTRAR");
		btnRegistrar.setBackground(Color.green);
		btnRegistrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DAO dao = new DAO();
				String usuario = textFUsuario.getText();
		        String senha = textFSenha.getText();
				
				if (textFUsuario.getText()!=null &&
						!textFUsuario.getText().isEmpty() &&
						textFSenha.getText()!=null &&
						!textFSenha.getText().isEmpty()) {
					
					boolean sucesso = dao.registrarUsuario(usuario, senha);
					
					if (sucesso) {
					JOptionPane.showMessageDialog(btnRegistrar, "Usuario registrado com sucesso.");
					Login frameLogin = new Login();
					frameLogin.setLocationRelativeTo(null);
					frameLogin.setVisible(true);
					
					dispose();
					} else {
						JOptionPane.showMessageDialog(btnRegistrar, "Erro ao registrar. O usuário pode já existir.", "Erro", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(btnRegistrar, "Informações Invalidas", "Aviso", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		btnRegistrar.setBounds(150, 185, 100, 20);
		jpRegistro.add(btnRegistrar);

	}

}