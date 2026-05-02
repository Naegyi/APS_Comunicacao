package comunicaçãoTcpIP;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

	public static void main(String[] args) throws IOException {
        //1 - Definir o serverSocket (abrir porta de conexão)
        ServerSocket serverSocket = new ServerSocket(54321);
        System.out.println("A porta 54321 foi aberta!");
        System.out.println("Servidor esperando receber mensagem de cliente...");
        
        while (true) {
        	
        	// 1. O Servidor para aqui e espera alguém conectar
            Socket socket = serverSocket.accept();
            
            // 2. Assim que conecta, pegamos o IP para avisar no console do servidor
            String ipCliente = socket.getInetAddress().getHostAddress();
            System.out.println("Novo usuário conectado! IP: " + ipCliente);
            
            // 3. Criamos o "atendente" (TratarCliente)
            TratarCliente cuidador = new TratarCliente(socket);
            
            // 4. USAMOS .start() para que ele rode em paralelo!
            cuidador.start();
        	
        }
        
    }
}