package comunicaçãoTcpIP.modelos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilitário para manipulação de arquivos no servidor.
 * Responsável por salvar arquivos recebidos em disco e carregá-los quando solicitados.
 * 
 * Os arquivos são armazenados na pasta "uploads/" (criada automaticamente).
 * Cada arquivo recebe um nome único baseado em timestamp + remetente + nome original
 * para evitar conflitos.
 */
public class Arquivo {

    // Constantes de configuração
    private static final String PASTA_UPLOADS = "uploads/";
    private static final String FORMATO_TIMESTAMP = "yyyyMMdd_HHmmss";

    // Bloco estático: garante que a pasta de uploads exista ao iniciar a aplicação
    static {
        try {
            Files.createDirectories(Paths.get(PASTA_UPLOADS));
            System.out.println("Pasta de uploads verificada/criada: " + PASTA_UPLOADS);
        } catch (IOException e) {
            System.err.println("ERRO CRÍTICO: Não foi possível criar a pasta de uploads: " + e.getMessage());
            // Não lançamos exceção porque o servidor pode tentar continuar,
            // mas qualquer operação de arquivo falhará posteriormente.
        }
    }

    /**
     * Salva um arquivo no disco do servidor.
     * 
     * @param dados       conteúdo binário do arquivo
     * @param nomeOriginal nome original do arquivo (enviado pelo cliente)
     * @param remetente   nome do usuário que enviou o arquivo
     * @param destinatario nome do destinatário (usado apenas para contexto, não no nome do arquivo)
     * @return o caminho relativo do arquivo salvo (ex: "uploads/20250320_143000_joao_foto.jpg")
     *         ou null se ocorrer erro
     */
    public static String salvarArquivo(byte[] dados, String nomeOriginal, 
                                       String remetente, String destinatario) {
        // Validação básica dos parâmetros
        if (dados == null || dados.length == 0) {
            System.err.println("Erro ao salvar arquivo: dados vazios ou nulos.");
            return null;
        }
        if (nomeOriginal == null || nomeOriginal.trim().isEmpty()) {
            System.err.println("Erro ao salvar arquivo: nome original inválido.");
            return null;
        }

        try {
            // Gera um nome único para evitar sobrescrita
            String nomeUnico = gerarNomeUnico(remetente, nomeOriginal);
            Path caminhoCompleto = Paths.get(PASTA_UPLOADS + nomeUnico);

            // Escreve os bytes no arquivo
            Files.write(caminhoCompleto, dados);

            System.out.println("Arquivo salvo com sucesso: " + caminhoCompleto);
            return caminhoCompleto.toString();

        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo '" + nomeOriginal + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Carrega um arquivo do disco do servidor a partir do caminho.
     * 
     * @param caminho caminho relativo ou absoluto do arquivo (ex: "uploads/20250320_143000_joao_foto.jpg")
     * @return array de bytes com o conteúdo do arquivo, ou null se não encontrado/erro
     */
    public static byte[] carregarArquivo(String caminho) {
        if (caminho == null || caminho.trim().isEmpty()) {
            System.err.println("Erro ao carregar arquivo: caminho inválido.");
            return null;
        }

        try {
            Path path = Paths.get(caminho);
            if (!Files.exists(path)) {
                System.err.println("Arquivo não encontrado: " + caminho);
                return null;
            }
            return Files.readAllBytes(path);

        } catch (IOException e) {
            System.err.println("Erro ao carregar arquivo '" + caminho + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Gera um nome único para o arquivo baseado no timestamp atual e no remetente.
     * Formato: yyyyMMdd_HHmmss_remetente_nomeOriginal
     * Exemplo: 20250320_143000_joao_foto.jpg
     * 
     * @param remetente    nome do usuário que enviou
     * @param nomeOriginal nome original do arquivo
     * @return string no formato descrito
     */
    private static String gerarNomeUnico(String remetente, String nomeOriginal) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(FORMATO_TIMESTAMP));
        // Remove caracteres problemáticos do nome do remetente (espaços, barras, etc.)
        String remetenteLimpo = remetente.replaceAll("[^a-zA-Z0-9_-]", "");
        String nomeOriginalLimpo = nomeOriginal.replaceAll("[^a-zA-Z0-9._-]", "");
        return timestamp + "_" + remetenteLimpo + "_" + nomeOriginalLimpo;
    }
}