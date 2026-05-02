package comunicaçãoTcpIP;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Mensagem implements Serializable{

	// Este ID é como uma versão do seu contrato. 
    // Se o Cliente e o Servidor tiverem versões diferentes, eles não se entendem.
    private static final long serialVersionUID = 1L;
    
    private String usuario;
    private String texto;
    private byte[] arquivo; // Para fotos da webcam ou arquivos pequenos
    private String nomeArquivo;
    private LocalDateTime dataHora;
    private TipoMensagem tipo;
    
    
    // Enum para organizar o que estamos enviando
    public enum TipoMensagem {
        TEXTO, IMAGEM, ARQUIVO, LOGON, LOGOFF
    }
    
    public Mensagem (String usuario, String texto) {
    	this.usuario = usuario;
    	this.texto = texto;
    	this.dataHora = LocalDateTime.now();
    	this.tipo = TipoMensagem.TEXTO;;
    }
    
    public Mensagem(String usuario, byte[] arquivo, String nomeArquivo, TipoMensagem tipo) {
    	this.usuario = usuario;
    	this.arquivo = arquivo;
    	this.nomeArquivo = nomeArquivo;
    	this.dataHora = LocalDateTime.now();
    	this.tipo = tipo;
    }
    
    
    public String getUsuario() { return usuario; }
    public String getTexto() { return texto; }
    public byte[] getArquivo() { return arquivo; }
    public String getNomeArquivo() { return nomeArquivo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public TipoMensagem getTipo() { return tipo; }
    
}

