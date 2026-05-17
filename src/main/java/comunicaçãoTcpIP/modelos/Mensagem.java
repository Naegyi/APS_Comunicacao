package comunicaçãoTcpIP.modelos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa uma mensagem trafegada entre cliente e servidor.
 * Implementa Serializable para ser enviada via ObjectOutputStream.
 * 
 * IMPORTANTE: O campo "texto" contém o conteúdo já criptografado/Base64
 * quando está em trânsito na rede. A descriptografia é feita no destino.
 */
public class Mensagem implements Serializable {

    private static final long serialVersionUID = 1L;

    // -------------------------------------------------------------
    // Enumerador de tipos de mensagem
    // -------------------------------------------------------------
    public enum TipoMensagem {
        TEXTO,      // mensagem de texto comum
        IMAGEM,     // (reservado para captura de webcam, não usado atualmente)
        ARQUIVO,    // envio de arquivo binário
        LOGON,      // anúncio de entrada do usuário
        LOGOFF      // anúncio de saída do usuário
    }

    // -------------------------------------------------------------
    // Campos da mensagem
    // -------------------------------------------------------------
    private String usuario;          // quem enviou
    private String destinatario;     // para quem (pode ser usuário ou grupo)
    private String texto;            // conteúdo da mensagem (já criptografado na rede)
    private byte[] arquivo;          // dados binários (arquivo ou imagem)
    private String nomeArquivo;      // nome original do arquivo
    private String caminhoArquivo;   // caminho onde o arquivo foi salvo no servidor
    private LocalDateTime dataHora;  // momento da criação da mensagem
    private TipoMensagem tipo;       // classificação da mensagem

    // -------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------

    /**
     * Construtor para mensagens de TEXTO.
     * @param usuario      remetente
     * @param destinatario destinatário (ou "Servidor" para LOGON/LOGOFF)
     * @param texto        conteúdo já criptografado (Base64)
     */
    public Mensagem(String usuario, String destinatario, String texto) {
        this.usuario = usuario;
        this.destinatario = destinatario;
        this.texto = texto;
        this.dataHora = LocalDateTime.now();
        this.tipo = TipoMensagem.TEXTO;
    }

    /**
     * Construtor para mensagens de ARQUIVO ou IMAGEM.
     * @param usuario      remetente
     * @param destinatario destinatário
     * @param arquivo      dados binários do arquivo
     * @param nomeArquivo  nome original do arquivo
     * @param tipo         deve ser ARQUIVO (ou IMAGEM)
     */
    public Mensagem(String usuario, String destinatario, byte[] arquivo,
                    String nomeArquivo, TipoMensagem tipo) {
        this.usuario = usuario;
        this.destinatario = destinatario;
        this.arquivo = arquivo;
        this.nomeArquivo = nomeArquivo;
        this.dataHora = LocalDateTime.now();
        this.tipo = tipo;
    }

    // -------------------------------------------------------------
    // Getters e Setters (acessos organizados por tipo de dado)
    // -------------------------------------------------------------

    // Dados básicos do remetente/destinatário
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    // Conteúdo textual (criptografado)
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    // Dados de arquivo
    public byte[] getArquivo() { return arquivo; }
    public void setArquivo(byte[] arquivo) { this.arquivo = arquivo; }

    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }

    public String getCaminhoArquivo() { return caminhoArquivo; }
    public void setCaminhoArquivo(String caminhoArquivo) { this.caminhoArquivo = caminhoArquivo; }

    // Metadados da mensagem
    public TipoMensagem getTipo() { return tipo; }
    public void setTipo(TipoMensagem tipo) { this.tipo = tipo; }

    // -------------------------------------------------------------
    // Métodos auxiliares
    // -------------------------------------------------------------

    /**
     * Retorna a hora da mensagem formatada como HH:mm (ex: "14:35").
     * Útil para exibição na interface.
     * @return hora no formato de dois dígitos
     */
    public String getHoraFormatada() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return dataHora.format(formatter);
    }
}