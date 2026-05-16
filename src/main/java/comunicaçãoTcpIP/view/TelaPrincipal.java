package comunicaçãoTcpIP.view;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TelaPrincipal extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TelaPrincipal frame = new TelaPrincipal();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public TelaPrincipal() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setBounds(100, 100, 807, 615);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(30, 30, 30));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 70, 773, 498);
		contentPane.add(scrollPane);
		scrollPane.setLayout(null);
		
		JPanel panel = new JPanel();
		setTitle("APS CHAT");
		panel.setBounds(10, 10, 350, 50);
		contentPane.add(panel);
		panel.setLayout(null);
		panel.setBackground(new Color(30, 30, 30));

		
		JButton btnNovoChat = new JButton("Nova Conversa");
		btnNovoChat.setBounds(10, 10, 140, 30);
		panel.add(btnNovoChat);
		btnNovoChat.setBackground(new Color(10, 90, 40));
		btnNovoChat.setForeground(Color.WHITE);
		
		// Funcionalidade: Abrir chat individual
		btnNovoChat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String nomeContato = JOptionPane.showInputDialog(TelaPrincipal.this, "Digite o nome do usuário que deseja conversar:");
				
				// Verifica se o usuário digitou algo e não cancelou
				if (nomeContato != null && !nomeContato.trim().isEmpty()) {
					TelaChat chat = new TelaChat(nomeContato);
					chat.setLocationRelativeTo(TelaPrincipal.this); // Centraliza a nova janela em relação a tela principal
					chat.setVisible(true);
				}
			}
		});
		
		
		JButton btnNovoGrupo = new JButton("Novo Grupo");
		btnNovoGrupo.setBackground(new Color(10 , 90, 40));
		btnNovoGrupo.setForeground(Color.WHITE);
		btnNovoGrupo.setBounds(160, 10, 140, 30);
		panel.add(btnNovoGrupo);
		
		
		btnNovoGrupo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String nomeGrupo = JOptionPane.showInputDialog(TelaPrincipal.this, "Digite o nome do novo grupo:");
				
				// Verifica se o usuário digitou algo e não cancelou
				if (nomeGrupo != null && !nomeGrupo.trim().isEmpty()) {
					// Adicionamos "(Grupo)" ao título para diferenciar
					TelaChat chat = new TelaChat(nomeGrupo + " (Grupo)");
					chat.setLocationRelativeTo(TelaPrincipal.this);
					chat.setVisible(true);
				}
			}
		});

	}
}