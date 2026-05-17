package comunicaçãoTcpIP.rede;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * Classe responsável por "codificar" e "decodificar" textos usando Base64.
 * 
 * ATENÇÃO: Base64 NÃO é um método de criptografia seguro!
 * Ele apenas transforma bytes em texto legível (ex: para enviar binário como texto).
 * Qualquer pessoa que interceptar a mensagem consegue decodificar facilmente.
 * 
 * Para um sistema real, substitua por AES ou outra cifra de verdade.
 * Mas esta implementação mantém a funcionalidade original do sistema.
 */
public class Criptografia {

    // Usamos UTF-8 para garantir consistência entre caracteres especiais
    private static final String CHARSET = StandardCharsets.UTF_8.name();

    /**
     * Codifica um texto em Base64.
     * 
     * @param texto texto plano (pode ser nulo)
     * @return string codificada em Base64, ou string vazia se texto for nulo
     */
    public static String criptografar(String texto) {
        if (texto == null) {
            return "";
        }
        // Converte a string para bytes usando UTF-8 e depois codifica em Base64
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Decodifica um texto que foi codificado em Base64.
     * 
     * @param textoCriptografado string codificada em Base64 (pode ser nula)
     * @return texto original decodificado, ou string vazia se entrada for nula.
     *         Em caso de erro (Base64 inválido), retorna o próprio texto de entrada.
     */
    public static String descriptografar(String textoCriptografado) {
        if (textoCriptografado == null) {
            return "";
        }
        try {
            // Decodifica de Base64 e reconstrói a string usando UTF-8
            byte[] bytesDecodificados = Base64.getDecoder().decode(textoCriptografado);
            return new String(bytesDecodificados, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // Se o texto não for um Base64 válido, retorna o original (comportamento legado)
            // Isso evita que mensagens já em texto plano quebrem o sistema.
            return textoCriptografado;
        }
    }
}