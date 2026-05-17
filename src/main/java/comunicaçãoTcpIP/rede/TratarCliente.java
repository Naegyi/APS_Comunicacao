package comunicaçãoTcpIP.rede;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import comunicaçãoTcpIP.database.DAO;
import comunicaçãoTcpIP.modelos.Arquivo;
import comunicaçãoTcpIP.modelos.Mensagem;

/**
 * Thread responsável por gerenciar a comunicação com um único cliente.
 * Cada cliente conectado tem sua própria instância dessa classe.
 * Ela lê mensagens enviadas pelo cliente, processa (salva no banco, repassa
 * para destinatários online) e envia respostas quando necessário.
 */
public class TratarCliente extends Thread {

    // Constantes para facilitar manutenção
    private static final String SUFIXO_GRUPO = " (Grupo)";
    private static final String PREFIXO_GRUPO_BD = "GRUPO_";

    // Conexões e streams
    private final Socket socket;
    private ObjectInputStream entrada;
    private ObjectOutputStream saida;

    // Dados do cliente
    private String nomeUsuario;
    private final DAO dao = new DAO();

    public TratarCliente(Socket socket) {
        this.socket = socket;
    }

    // ---------------------------------------------------------
    // Métodos públicos
    // ---------------------------------------------------------

    /**
     * Envia uma mensagem para este cliente (se ele estiver online).
     * Usado pelo servidor para repassar mensagens de outros usuários.
     * @param msg mensagem a ser enviada
     */
    public void enviarMensagem(Mensagem msg) {
        try {
            if (saida != null) {
                saida.writeObject(msg);
                saida.flush();
                System.out.println("Mensagem reenviada para: " + nomeUsuario);
            }
        } catch (IOException e) {
            System.err.println("Erro ao reenviar mensagem para " + nomeUsuario + ": " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // Loop principal da thread
    // ---------------------------------------------------------

    @Override
    public void run() {
        try {
            // Inicializa os streams de comunicação
            saida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());

            // Fica em loop eterno processando mensagens até que a conexão seja fechada
            while (true) {
                Mensagem msg = (Mensagem) entrada.readObject();
                System.out.println("Servidor recebeu: " + msg.getTipo() + " de " + msg.getUsuario());

                // Processa cada tipo de mensagem
                switch (msg.getTipo()) {
                    case LOGON:
                        processarLogon(msg);
                        break;
                    case TEXTO:
                        processarTexto(msg);
                        break;
                    case ARQUIVO:
                        processarArquivo(msg);
                        break;
                    case LOGOFF:
                        processarLogoff(msg);
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // Se algo der errado (cliente desconectou bruscamente, erro de rede, etc.)
            if (nomeUsuario != null) {
                Servidor.clientesConectados.remove(nomeUsuario);
                System.out.println(nomeUsuario + " desconectado (erro)");
            }
            System.err.println("Erro na thread do cliente " + nomeUsuario + ": " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // Processamento de cada tipo de mensagem
    // ---------------------------------------------------------

    /**
     * Processa o LOGON: registra o usuário no mapa de clientes conectados.
     */
    private void processarLogon(Mensagem msg) {
        this.nomeUsuario = msg.getUsuario();
        Servidor.clientesConectados.put(this.nomeUsuario, this);
        System.out.println(this.nomeUsuario + " conectou-se.");
    }

    /**
     * Processa mensagens de TEXTO.
     * - Se for mensagem para grupo, envia a todos os membros (exceto o remetente)
     *   e salva no banco com destino especial.
     * - Se for mensagem privada, salva no banco e repassa apenas ao destinatário
     *   (se estiver online).
     */
    private void processarTexto(Mensagem msg) {
        String destino = msg.getDestinatario();
        boolean isGrupo = destino != null && destino.endsWith(SUFIXO_GRUPO);

        System.out.println(msg.getUsuario() + " -> " + destino);

        if (isGrupo) {
            String nomeGrupo = destino.replace(SUFIXO_GRUPO, "");
            List<String> membros = dao.buscarMembrosDoGrupo(nomeGrupo);

            // Salva a mensagem no banco (usando um identificador especial para grupo)
            String destinoGrupoBD = PREFIXO_GRUPO_BD + nomeGrupo;
            dao.salvarMensagem(msg.getUsuario(), destinoGrupoBD, msg.getTexto(), msg.getTipo(), null);
            System.out.println("Mensagem de grupo salva no banco!");

            // Reenvia para todos os membros EXCETO o remetente
            reenviarParaGrupo(membros, msg, Mensagem.TipoMensagem.TEXTO, null, null);
        } else {
            // Mensagem privada
            dao.salvarMensagem(msg.getUsuario(), destino, msg.getTexto(), msg.getTipo(), null);
            System.out.println("Mensagem salva!");

            // Repassa se o destinatário estiver online
            reenviarParaDestinatario(destino, msg);
        }
    }

    /**
     * Processa mensagens de ARQUIVO.
     * - Salva o arquivo fisicamente no servidor (retorna o caminho).
     * - Registra a referência no banco.
     * - Se for grupo, envia para todos os membros (exceto remetente).
     * - Se for privado, envia apenas ao destinatário (se online).
     */
    private void processarArquivo(Mensagem msg) {
        String destino = msg.getDestinatario();
        boolean isGrupo = destino != null && destino.endsWith(SUFIXO_GRUPO);

        System.out.println("ARQUIVO: " + msg.getNomeArquivo() + " de " + msg.getUsuario() + " para " + destino);

        // Salva o arquivo no disco do servidor
        String caminhoArquivo = Arquivo.salvarArquivo(
            msg.getArquivo(), msg.getNomeArquivo(), msg.getUsuario(), destino
        );

        // Prepara o identificador criptografado (na verdade, apenas codificado em Base64)
        String identificador = Criptografia.criptografar("[ARQUIVO]:" + msg.getNomeArquivo());

        if (isGrupo) {
            String nomeGrupo = destino.replace(SUFIXO_GRUPO, "");
            dao.salvarMensagemGrupo(nomeGrupo, msg.getUsuario(), identificador, Mensagem.TipoMensagem.ARQUIVO);

            List<String> membros = dao.buscarMembrosDoGrupo(nomeGrupo);
            // Reenvia para todos os membros (exceto remetente) com os metadados do arquivo
            reenviarParaGrupo(membros, msg, Mensagem.TipoMensagem.ARQUIVO, msg.getNomeArquivo(), caminhoArquivo);
        } else {
            // Salva a mensagem de arquivo no banco (incluindo o caminho físico)
            dao.salvarMensagem(msg.getUsuario(), destino, identificador, Mensagem.TipoMensagem.ARQUIVO, caminhoArquivo);

            // Cria uma mensagem para repassar ao destinatário
            Mensagem msgRep = new Mensagem(msg.getUsuario(), destino, identificador);
            msgRep.setTipo(Mensagem.TipoMensagem.ARQUIVO);
            msgRep.setNomeArquivo(msg.getNomeArquivo());
            msgRep.setCaminhoArquivo(caminhoArquivo);
            reenviarParaDestinatario(destino, msgRep);
        }
    }

    /**
     * Processa LOGOFF: remove o cliente do mapa de conectados.
     */
    private void processarLogoff(Mensagem msg) {
        System.out.println(msg.getUsuario() + " desconectou-se.");
        Servidor.clientesConectados.remove(msg.getUsuario());
    }

    // ---------------------------------------------------------
    // Métodos auxiliares para reenvio
    // ---------------------------------------------------------

    /**
     * Reenvia uma mensagem para um único destinatário (se estiver online).
     * @param destinatario nome do usuário alvo
     * @param msg mensagem já pronta para ser enviada
     */
    private void reenviarParaDestinatario(String destinatario, Mensagem msg) {
        TratarCliente clienteDestino = Servidor.clientesConectados.get(destinatario);
        if (clienteDestino != null) {
            clienteDestino.enviarMensagem(msg);
            System.out.println("Mensagem reenviada para: " + destinatario);
        } else {
            System.out.println("⚠️ Destinatário offline: " + destinatario);
        }
    }

    /**
     * Reenvia uma mensagem para todos os membros de um grupo, exceto o remetente original.
     * @param membros lista de nomes dos membros do grupo
     * @param msgOriginal mensagem original recebida (para extrair remetente e conteúdo)
     * @param tipo tipo da mensagem (TEXTO ou ARQUIVO)
     * @param nomeArquivo (opcional) nome do arquivo, se for ARQUIVO
     * @param caminhoArquivo (opcional) caminho no servidor, se for ARQUIVO
     */
    private void reenviarParaGrupo(List<String> membros, Mensagem msgOriginal,
                                   Mensagem.TipoMensagem tipo, String nomeArquivo, String caminhoArquivo) {
        for (String membro : membros) {
            if (!membro.equals(msgOriginal.getUsuario())) {
                TratarCliente clienteMembro = Servidor.clientesConectados.get(membro);
                if (clienteMembro != null) {
                    // Cria uma nova mensagem com o destinatário específico (privada)
                    Mensagem msgGrupo;
                    if (tipo == Mensagem.TipoMensagem.TEXTO) {
                        msgGrupo = new Mensagem(msgOriginal.getUsuario(), membro, msgOriginal.getTexto());
                        msgGrupo.setTipo(Mensagem.TipoMensagem.TEXTO);
                    } else { // ARQUIVO
                        String identificador = Criptografia.criptografar("[ARQUIVO]:" + nomeArquivo);
                        msgGrupo = new Mensagem(msgOriginal.getUsuario(), membro, identificador);
                        msgGrupo.setTipo(Mensagem.TipoMensagem.ARQUIVO);
                        msgGrupo.setNomeArquivo(nomeArquivo);
                        msgGrupo.setCaminhoArquivo(caminhoArquivo);
                    }
                    clienteMembro.enviarMensagem(msgGrupo);
                    System.out.println("Mensagem de grupo reenviada para: " + membro);
                }
            }
        }
    }
}