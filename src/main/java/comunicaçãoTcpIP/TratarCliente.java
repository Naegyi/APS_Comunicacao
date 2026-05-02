package comunicaçãoTcpIP;

import java.net.Socket;
import java.io.ObjectInputStream;

public class TratarCliente extends Thread {
	
	private Socket socket;
	
	public TratarCliente(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream()); 
			
			while (true) {
				
				Mensagem msg = (Mensagem) entrada.readObject();
				
				 switch (msg.getTipo()) {
				 	case TEXTO:
				 		System.out.println(msg.getUsuario() + ": " + msg.getTexto());
				 		break;
				 	case IMAGEM:
				 		System.out.println(msg.getUsuario() + " acabou de enviar uma imagem!");
				 		break;
				 	case ARQUIVO:
				 		System.out.println(msg.getUsuario() + " acabou de enviar um arquivo!");
				 		break;
				 	case LOGON:
				 		System.out.println("LOG: " + msg.getUsuario() + " entrou.");
				 		break;
				 	case LOGOFF:
				 		System.out.println("LOG: " + msg.getUsuario() + " saiu.");
				 		break;
				 } 
			}
			
		} catch (Exception e) {
			System.out.println("Cliente desconectou.");
		}
	}

}
