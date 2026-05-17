package comunicaçãoTcpIP.rede;

import java.io.*;
import java.net.Socket;

import comunicaçãoTcpIP.modelos.Mensagem;

/**
 * Cliente do chat – responsável por manter a conexão com o servidor,
 * enviar mensagens e notificar a interface sobre mensagens recebidas.
 * 
 * Implementação singleton estática (apenas uma conexão ativa por aplicação).
 * Toda a comunicação é feita via objetos serializados (ObjectOutputStream).
 */
public class Cliente {

    // Constantes
    private static final String LOGON_MENSAGEM = "Logon";
    private static final String LOGOFF_MENSAGEM = "Logoff";
    private static final String DESTINO_SERVIDOR = "Servidor";

    // Conexão
    private static Socket socket;
    private static ObjectOutputStream saida;
    private static ObjectInputStream entrada;
    private static Thread escutaThread;

    // Estado
    private static String usuarioLogado;
    private static boolean conectado = false;

    // Callback para notificar a UI sobre mensagens recebidas
    public interface MensagemRecebidaListener {
        void onMensagemRecebida(Mensagem msg);
    }
    private static MensagemRecebidaListener listener;

    // ---------------------------------------------------------
    // Configuração do listener (chamado pela TelaChat)
    // ---------------------------------------------------------
    public static void setMensagemListener(MensagemRecebidaListener l) {
        listener = l;
    }

    // ---------------------------------------------------------
    // Conexão e inicialização
    // ---------------------------------------------------------
    /**
     * Conecta ao servidor de chat.
     * @param ipServer endereço IP do servidor
     * @param porta porta do servidor (normalmente 54321)
     * @param usuario nome do usuário que está logando
     */
    public static void conectar(String ipServer, int porta, String usuario) {
        // Evita reconectar se já estiver conectado
        if (conectado && socket != null && !socket.isClosed()) {
            System.out.println("Cliente já conectado, ignorando nova tentativa.");
            return;
        }

        usuarioLogado = usuario;

        try {
            // Cria o socket e os streams de comunicação
            socket = new Socket(ipServer, porta);
            saida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
            conectado = true;
            System.out.println("Conectado ao servidor!");

            // Envia uma mensagem de LOGON para o servidor se identificar
            Mensagem msgLogon = new Mensagem(usuarioLogado, DESTINO_SERVIDOR, LOGON_MENSAGEM);
            msgLogon.setTipo(Mensagem.TipoMensagem.LOGON);
            enviar(msgLogon);

            // Inicia thread que ficará ouvindo mensagens do servidor
            iniciarEscuta();

        } catch (Exception e) {
            System.err.println("Erro ao conectar: " + e.getMessage());
            conectado = false;
        }
    }

    /**
     * Inicia uma thread em background que fica aguardando mensagens do servidor.
     * Cada mensagem recebida é repassada ao listener (se registrado).
     */
    private static void iniciarEscuta() {
        escutaThread = new Thread(() -> {
            try {
                // Enquanto estiver conectado e o socket estiver aberto, lê objetos
                while (conectado && socket != null && !socket.isClosed()) {
                    Mensagem msg = (Mensagem) entrada.readObject();

                    // Log de depuração (útil para diagnóstico)
                    System.out.println("Mensagem recebida do servidor: " + msg.getTipo()
                                     + " de " + msg.getUsuario());

                    // Notifica a UI (ex: TelaChat) que uma nova mensagem chegou
                    if (listener != null) {
                        listener.onMensagemRecebida(msg);
                    }
                }
            } catch (IOException e) {
                System.err.println("Conexão perdida: " + e.getMessage());
                conectado = false;
            } catch (ClassNotFoundException e) {
                System.err.println("Erro ao ler mensagem: classe não encontrada - " + e.getMessage());
            }
        });
        escutaThread.start();
    }

    // ---------------------------------------------------------
    // Envio de mensagens
    // ---------------------------------------------------------
    /**
     * Envia uma mensagem ao servidor.
     * @param msg mensagem a ser enviada (já preenchida com remetente, destinatário e conteúdo)
     */
    public static void enviar(Mensagem msg) {
        try {
            if (saida != null && conectado) {
                saida.writeObject(msg);
                saida.flush();          // garante que os dados sejam enviados imediatamente
                System.out.println("Mensagem enviada: " + msg.getTipo());
            } else {
                System.err.println("Tentativa de enviar mensagem sem conexão ativa.");
            }
        } catch (Exception e) {
            System.err.println("Falha ao enviar mensagem: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // Desconexão
    // ---------------------------------------------------------
    /**
     * Desconecta do servidor de forma ordenada:
     * - Envia mensagem de LOGOFF
     * - Fecha os streams e o socket
     */
    public static void desconectar() {
        try {
            // Avisa o servidor que está saindo
            if (conectado && usuarioLogado != null) {
                Mensagem msgLogoff = new Mensagem(usuarioLogado, DESTINO_SERVIDOR, LOGOFF_MENSAGEM);
                msgLogoff.setTipo(Mensagem.TipoMensagem.LOGOFF);
                enviar(msgLogoff);
            }

            // Fecha os recursos na ordem inversa da criação
            if (entrada != null) entrada.close();
            if (saida != null) saida.close();
            if (socket != null) socket.close();

            conectado = false;
            System.out.println("Cliente desconectado.");
        } catch (IOException e) {
            System.err.println("Erro ao desconectar: " + e.getMessage());
        }
    }
}