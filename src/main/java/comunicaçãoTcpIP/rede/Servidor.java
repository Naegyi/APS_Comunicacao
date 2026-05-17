package comunicaçãoTcpIP.rede;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import comunicaçãoTcpIP.database.DataBaseConfig;

/**
 * Servidor TCP do chat.
 * Escuta conexões na porta definida, aceita clientes e cria uma thread
 * (TratarCliente) para cuidar da comunicação com cada um.
 * Mantém um mapa de clientes conectados para envio de mensagens direcionadas.
 */
public class Servidor {

    // Constantes de configuração
    private static final int PORTA_PADRAO = 54321;
    private static final String MSG_SERVIDOR_INICIADO = "Servidor rodando na porta %d...";
    private static final String MSG_NOVA_CONEXAO = "Nova conexão de: %s";

    /**
     * Mapa global com todos os clientes ativos.
     * A chave é o nome do usuário (String) e o valor é a thread que gerencia a conexão.
     * Usamos ConcurrentHashMap para acesso seguro entre múltiplas threads.
     */
    public static final ConcurrentHashMap<String, TratarCliente> clientesConectados = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // Inicializa o banco de dados (cria tabelas se não existirem)
        DataBaseConfig.inicializarBanco();

        // Inicia o socket do servidor e aceita conexões infinitamente
        try (ServerSocket serverSocket = new ServerSocket(PORTA_PADRAO)) {
            System.out.printf(MSG_SERVIDOR_INICIADO, PORTA_PADRAO);

            // Loop principal: aceita novos clientes para sempre
            while (true) {
                Socket socket = serverSocket.accept();  // bloqueia até nova conexão
                String ipCliente = socket.getInetAddress().getHostAddress();
                System.out.printf(MSG_NOVA_CONEXAO, ipCliente);

                // Cria uma nova thread para atender este cliente
                TratarCliente clienteThread = new TratarCliente(socket);
                clienteThread.start();   // inicia a execução paralela
            }
        } catch (IOException e) {
            System.err.println("Erro crítico no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}