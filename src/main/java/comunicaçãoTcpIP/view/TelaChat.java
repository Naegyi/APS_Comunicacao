package comunicaçãoTcpIP.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import comunicaçãoTcpIP.modelos.Mensagem;

public class TelaChat extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTextArea textAreaMensagens;
	private JTextField textFieldMensagem;
	private String tituloChat;

	public TelaChat(String tituloChat) {
		this.tituloChat = tituloChat;
		setTitle("Conversa: " + this.tituloChat);
		setBounds(150, 150, 500, 450);
		// Usa DISPOSE_ON_CLOSE para que fechar o chat não feche o app inteiro
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
		getContentPane().setLayout(new BorderLayout());

		// Área onde as mensagens vão aparecer
		textAreaMensagens = new JTextArea();
		textAreaMensagens.setEditable(false);
		textAreaMensagens.setBackground(new Color(30, 30, 30));
		textAreaMensagens.setForeground(Color.GREEN); // Mantendo o estilo visual verde/preto
		
		JScrollPane scrollPane = new JScrollPane(textAreaMensagens);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		// Painel inferior para digitar a mensagem e o botão de enviar
		JPanel painelInferior = new JPanel();
		painelInferior.setLayout(new BorderLayout());
		getContentPane().add(painelInferior, BorderLayout.SOUTH);

		textFieldMensagem = new JTextField();
		textFieldMensagem.setBackground(new Color(50, 50, 50));
		textFieldMensagem.setForeground(Color.WHITE);
		painelInferior.add(textFieldMensagem, BorderLayout.CENTER);

		JButton btnEnviar = new JButton("Enviar");
		btnEnviar.setBackground(new Color(10, 90, 40));
		btnEnviar.setForeground(Color.WHITE);
		painelInferior.add(btnEnviar, BorderLayout.EAST);

		// Ação ao clicar em "Enviar"
		btnEnviar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enviarMensagem();
			}
		});

		// Ação ao apertar "Enter" no campo de texto
		textFieldMensagem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enviarMensagem();
			}
		});
	}

	private void enviarMensagem() {
		String texto = textFieldMensagem.getText();
		if (texto != null && !texto.trim().isEmpty()) {
			
			// Usa o seu modelo Mensagem para estruturar o dado
			Mensagem msg = new Mensagem("Você", texto);
			
			// Formata a hora para exibir no chat (ex: Você [14:30]: Olá!)
			String horaFormatada = String.format("%02d:%02d", msg.getDataHora().getHour(), msg.getDataHora().getMinute());
			
			// Adiciona a mensagem na tela
			textAreaMensagens.append(msg.getUsuario() + " [" + horaFormatada + "]: " + msg.getTexto() + "\n");
			
			// Limpa o campo de texto
			textFieldMensagem.setText("");
			
			
		}
	}
}