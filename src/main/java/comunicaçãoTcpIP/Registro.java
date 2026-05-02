package comunicaçãoTcpIP;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
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
	private JTextField textFConfSenha;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Registro frame = new Registro();
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
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(0, 0, 0));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel jpRegistro = new JPanel();
		jpRegistro.setBackground(new Color(192, 192, 192));
		jpRegistro.setBounds(10, 10, 416, 243);
		contentPane.add(jpRegistro);
		jpRegistro.setLayout(null);
		
		JLabel lblRegistro = new JLabel("Registrar Usuario");
		lblRegistro.setBounds(162, 27, 103, 12);
		jpRegistro.add(lblRegistro);
		
		JLabel lblUsuario = new JLabel("Usuario:");
		lblUsuario.setBounds(78, 48, 96, 12);
		jpRegistro.add(lblUsuario);
		
		JLabel lblNewLabel_1_1 = new JLabel("Senha:");
		lblNewLabel_1_1.setBounds(78, 90, 44, 12);
		jpRegistro.add(lblNewLabel_1_1);
		
		JLabel lblNewLabel_1_1_1 = new JLabel("Confirmar Senha:");
		lblNewLabel_1_1_1.setBounds(78, 131, 161, 12);
		jpRegistro.add(lblNewLabel_1_1_1);
		
		textFUsuario = new JTextField();
		textFUsuario.setBounds(78, 62, 234, 18);
		jpRegistro.add(textFUsuario);
		textFUsuario.setColumns(10);
		
		textFSenha = new JTextField();
		textFSenha.setColumns(10);
		textFSenha.setBounds(78, 103, 234, 18);
		jpRegistro.add(textFSenha);
		
		textFConfSenha = new JTextField();
		textFConfSenha.setColumns(10);
		textFConfSenha.setBounds(78, 143, 234, 18);
		jpRegistro.add(textFConfSenha);
		
		JButton btnRegistrar = new JButton("Registrar");
		btnRegistrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (textFUsuario.getText()!=null &&
						!textFUsuario.getText().isEmpty() &&
						textFSenha.getText()!=null &&
						!textFSenha.getText().isEmpty() &&
						textFConfSenha.getText()!=null &&
						!textFConfSenha.getText().isEmpty()) {
					JOptionPane.showMessageDialog(btnRegistrar, "Contato Registrado");
					
					Login frameLogin = new Login();
					frameLogin.setVisible(true);
					
					dispose();
					
					
				} else {
					JOptionPane.showMessageDialog(btnRegistrar, "Informações Invalidas", "Aviso", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		btnRegistrar.setBounds(170, 185, 84, 20);
		jpRegistro.add(btnRegistrar);

	}

}
