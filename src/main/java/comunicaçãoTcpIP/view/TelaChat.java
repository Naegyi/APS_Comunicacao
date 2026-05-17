package comunicaçãoTcpIP.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.swing.*;
import javax.swing.border.LineBorder;

import comunicaçãoTcpIP.database.DAO;
import comunicaçãoTcpIP.modelos.Arquivo;
import comunicaçãoTcpIP.modelos.Mensagem;
import comunicaçãoTcpIP.rede.Cliente;
import comunicaçãoTcpIP.rede.Criptografia;

/**
 * Tela de chat para conversas privadas ou em grupo.
 * Gerencia exibição de mensagens, envio de texto/arquivo, recepção em tempo real
 * e persistência local (histórico) usando o DAO.
 */
public class TelaChat extends JFrame {
    private static final long serialVersionUID = 1L;

    // Componentes da UI
    private JPanel painelMensagensConteudo; // painel que recebe os balões de mensagem (rolável)
    private JTextField textFieldMensagem;

    // Dados da conversa
    private final String usuarioLogado;
    private final String destinatario;   // pode ser um nome de usuário ou "nomeGrupo (Grupo)"
    private final DAO dao = new DAO();

    // Controle para evitar recarregar o histórico múltiplas vezes ao abrir a tela
    private boolean historicoCarregado = false;

    // -------------------------------------------------------------
    // Construtor
    // -------------------------------------------------------------
    public TelaChat(String usuarioLogado, String destinatario) {
        this.usuarioLogado = usuarioLogado;
        this.destinatario = destinatario;

        inicializarUI();
        configurarListenerRecepcaoMensagens();
        carregarHistorico(); // carrega mensagens anteriores do banco
    }

    // -------------------------------------------------------------
    // Inicialização da interface gráfica
    // -------------------------------------------------------------
    private void inicializarUI() {
        setTitle("Conversa com: " + this.destinatario);
        setBounds(150, 150, 600, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        criarPainelCentral();   // área rolável com os balões
        criarPainelInferior();  // campo de texto + botões
    }

    /**
     * Cria o painel rolável que contém todos os balões de mensagens.
     */
    private void criarPainelCentral() {
        painelMensagensConteudo = new JPanel();
        painelMensagensConteudo.setLayout(new BoxLayout(painelMensagensConteudo, BoxLayout.Y_AXIS));
        painelMensagensConteudo.setBackground(new Color(20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(painelMensagensConteudo);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Cria o painel inferior com campo de texto, botão de enviar arquivo e botão enviar.
     * O campo de texto também responde à tecla Enter.
     */
    private void criarPainelInferior() {
        JPanel painelInferior = new JPanel(new BorderLayout());
        getContentPane().add(painelInferior, BorderLayout.SOUTH);

        textFieldMensagem = new JTextField();
        textFieldMensagem.setBackground(new Color(50, 50, 50));
        textFieldMensagem.setForeground(Color.WHITE);
        textFieldMensagem.addActionListener(e -> enviarMensagemTexto()); // Enter envia
        painelInferior.add(textFieldMensagem, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        painelBotoes.setBackground(new Color(50, 50, 50));

        JButton btnArquivo = new JButton("📁 Enviar Arquivo");
        btnArquivo.setBackground(new Color(60, 60, 60));
        btnArquivo.setForeground(Color.WHITE);
        btnArquivo.addActionListener(e -> selecionarEEnviarArquivo());
        painelBotoes.add(btnArquivo);

        JButton btnEnviar = new JButton("Enviar");
        btnEnviar.setBackground(new Color(10, 90, 40));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.addActionListener(e -> enviarMensagemTexto());
        painelBotoes.add(btnEnviar);

        painelInferior.add(painelBotoes, BorderLayout.EAST);
    }

    // -------------------------------------------------------------
    // Lógica de recebimento de mensagens via rede (Cliente)
    // -------------------------------------------------------------
    /**
     * Configura o listener do Cliente para receber mensagens em tempo real.
     * As mensagens recebidas são:
     * - Salvas no banco local (se forem de outro usuário).
     * - Exibidas visualmente na tela.
     * - O listener verifica se a mensagem pertence a esta conversa (privada ou grupo).
     */
    private void configurarListenerRecepcaoMensagens() {
        Cliente.setMensagemListener(msg -> {
            SwingUtilities.invokeLater(() -> {
                if (!mensagemPertenceAConversa(msg)) return;

                // Salva no banco apenas se a mensagem NÃO foi enviada pelo próprio usuário
                // (pois quando enviamos, já salvamos localmente no momento do envio)
                if (!msg.getUsuario().equals(usuarioLogado)) {
                    salvarMensagemRecebidaNoBanco(msg);
                }

                // Exibe visualmente
                exibirMensagemNaTela(msg);

                // Rola automaticamente para a mensagem mais recente
                rolarParaFim();
            });
        });
    }

    /**
     * Verifica se a mensagem recebida pertence à conversa atual.
     * Casos:
     * - Conversa privada: destinatário == usuário logado e remetente == contato da conversa.
     * - Conversa em grupo: o destinatário termina com " (Grupo)" e a mensagem é enviada para o grupo
     *   (o remetente pode ser qualquer membro, inclusive o próprio logado - mas este filtro já cai fora).
     */
    private boolean mensagemPertenceAConversa(Mensagem msg) {
        boolean isPrivadaParaMim = msg.getDestinatario().equals(usuarioLogado) &&
                                   msg.getUsuario().equals(destinatario);
        boolean isPrivadaDoMeuContato = msg.getUsuario().equals(destinatario) &&
                                        msg.getDestinatario().equals(usuarioLogado);

        boolean isGrupo = destinatario != null && destinatario.endsWith(" (Grupo)");
        String nomeGrupo = isGrupo ? destinatario.replace(" (Grupo)", "") : null;
        boolean isMsgParaOGrupo = isGrupo && msg.getDestinatario().equals(nomeGrupo);

        return isPrivadaParaMim || isPrivadaDoMeuContato || isMsgParaOGrupo;
    }

    /**
     * Salva a mensagem recebida no banco de dados local, já descriptografando se for texto.
     * Para arquivos, salva a referência.
     */
    private void salvarMensagemRecebidaNoBanco(Mensagem msg) {
        if (msg.getTipo() == Mensagem.TipoMensagem.TEXTO) {
            String textoDescriptografado = Criptografia.descriptografar(msg.getTexto());
            String textoCriptografado = Criptografia.criptografar(textoDescriptografado);
            dao.salvarMensagem(msg.getUsuario(), usuarioLogado, textoCriptografado, Mensagem.TipoMensagem.TEXTO);
        } else if (msg.getTipo() == Mensagem.TipoMensagem.ARQUIVO) {
            String identificador = Criptografia.criptografar("[ARQUIVO]:" + msg.getNomeArquivo());
            dao.salvarMensagem(msg.getUsuario(), usuarioLogado, identificador, Mensagem.TipoMensagem.ARQUIVO, msg.getCaminhoArquivo());
        }
    }

    /**
     * Exibe a mensagem no painel de balões, chamando o método adequado para texto ou arquivo.
     */
    private void exibirMensagemNaTela(Mensagem msg) {
        if (msg.getTipo() == Mensagem.TipoMensagem.TEXTO) {
            String textoLimpo = Criptografia.descriptografar(msg.getTexto());
            adicionarBalaoVisual(msg.getUsuario(), textoLimpo);
        } else if (msg.getTipo() == Mensagem.TipoMensagem.ARQUIVO) {
            adicionarBalaoArquivo(msg.getUsuario(), msg.getNomeArquivo(), msg.getCaminhoArquivo());
        }
        revalidarERolar();
    }

    // -------------------------------------------------------------
    // Histórico (carregamento do banco)
    // -------------------------------------------------------------
    /**
     * Carrega todas as mensagens anteriores do banco de dados para esta conversa.
     * Executado apenas uma vez ao abrir a tela.
     */
    private void carregarHistorico() {
        if (historicoCarregado) return;
        historicoCarregado = true;

        List<Object[]> historico = dao.buscarHistoricoCompleto(usuarioLogado, destinatario);
        System.out.println("Carregando histórico: " + historico.size() + " mensagens");

        painelMensagensConteudo.removeAll(); // limpa para não duplicar

        for (Object[] registro : historico) {
            String remetente = (String) registro[0];
            String conteudoCripto = (String) registro[1];
            String caminhoArquivo = (String) registro[2];
            String tipo = (String) registro[3];

            if (conteudoCripto == null) continue;
            String conteudo = Criptografia.descriptografar(conteudoCripto);

            if ("ARQUIVO".equals(tipo)) {
                String nomeArquivo = extrairNomeArquivoDoIdentificador(conteudo);
                adicionarBalaoArquivo(remetente, nomeArquivo, caminhoArquivo);
            } else {
                if (conteudo.isEmpty()) conteudo = "[Mensagem vazia]";
                adicionarBalaoVisual(remetente, conteudo);
            }
        }

        revalidarERolar();
    }

    /**
     * Extrai o nome do arquivo a partir do identificador salvo no banco.
     * Exemplo: "[ARQUIVO]:documento.pdf" -> "documento.pdf"
     */
    private String extrairNomeArquivoDoIdentificador(String identificador) {
        if (identificador != null && identificador.startsWith("[ARQUIVO]:")) {
            return identificador.substring(10);
        }
        return "arquivo_desconhecido";
    }

    // -------------------------------------------------------------
    // Métodos auxiliares de UI
    // -------------------------------------------------------------
    private void rolarParaFim() {
        SwingUtilities.invokeLater(() -> {
            JScrollPane scroll = (JScrollPane) painelMensagensConteudo.getParent().getParent();
            JScrollBar vertical = scroll.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void revalidarERolar() {
        painelMensagensConteudo.revalidate();
        painelMensagensConteudo.repaint();
        rolarParaFim();
    }

    // -------------------------------------------------------------
    // Construção dos balões (visual)
    // -------------------------------------------------------------
    /**
     * Adiciona um balão de texto à conversa.
     * @param remetente quem enviou a mensagem
     * @param texto conteúdo da mensagem já descriptografado
     */
    private void adicionarBalaoVisual(String remetente, String texto) {
        JPanel painelLinha = new JPanel();
        painelLinha.setBackground(new Color(20, 20, 20));
        boolean souEu = remetente.equals(usuarioLogado);

        if (souEu) {
            painelLinha.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
            JPanel balao = criarBalaoTexto(texto, new Color(10, 80, 35), null);
            painelLinha.add(balao);
        } else {
            painelLinha.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
            JPanel balao = criarBalaoTexto(texto, new Color(40, 40, 40), remetente);
            painelLinha.add(balao);
        }

        painelLinha.setMaximumSize(new Dimension(Integer.MAX_VALUE, painelLinha.getPreferredSize().height));
        painelMensagensConteudo.add(painelLinha);
    }

    /**
     * Cria um balão de texto, com ou sem nome do remetente.
     */
    private JPanel criarBalaoTexto(String texto, Color corFundo, String remetente) {
        JPanel balao = new JPanel();
        balao.setBackground(corFundo);
        balao.setBorder(new LineBorder(new Color(60, 60, 60), 1, true));
        balao.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 5));

        if (remetente != null) {
            JLabel nomeLabel = new JLabel(remetente + ": ");
            nomeLabel.setForeground(new Color(100, 200, 100));
            nomeLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
            balao.add(nomeLabel);
        }

        JLabel textoLabel = new JLabel(texto);
        textoLabel.setForeground(Color.WHITE);
        balao.add(textoLabel);
        return balao;
    }

    /**
     * Adiciona um balão representando um arquivo compartilhado.
     * Inclui um botão "Baixar" que salva o arquivo localmente.
     */
    private void adicionarBalaoArquivo(String remetente, String nomeArquivo, String caminhoArquivo) {
        JPanel painelLinha = new JPanel();
        painelLinha.setBackground(new Color(20, 20, 20));
        boolean souEu = remetente.equals(usuarioLogado);
        painelLinha.setLayout(souEu ? new FlowLayout(FlowLayout.RIGHT, 10, 5) : new FlowLayout(FlowLayout.LEFT, 10, 5));

        JPanel balao = new JPanel();
        balao.setBackground(souEu ? new Color(10, 80, 35) : new Color(40, 40, 40));
        balao.setBorder(new LineBorder(new Color(60, 60, 60), 1, true));
        balao.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        if (!souEu) {
            JLabel nomeLabel = new JLabel(remetente + ": ");
            nomeLabel.setForeground(new Color(100, 200, 100));
            nomeLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
            balao.add(nomeLabel);
        }

        JLabel iconeArquivo = new JLabel("📁 " + nomeArquivo);
        iconeArquivo.setForeground(Color.WHITE);
        balao.add(iconeArquivo);

        JButton btnDownload = new JButton("📥 Baixar");
        btnDownload.setBackground(souEu ? new Color(10, 90, 40) : new Color(60, 60, 60));
        btnDownload.setForeground(Color.WHITE);
        btnDownload.addActionListener(e -> baixarArquivo(caminhoArquivo, nomeArquivo));
        balao.add(btnDownload);

        painelLinha.add(balao);
        painelLinha.setMaximumSize(new Dimension(Integer.MAX_VALUE, painelLinha.getPreferredSize().height));
        painelMensagensConteudo.add(painelLinha);
    }

    // -------------------------------------------------------------
    // Envio de mensagens (texto e arquivo)
    // -------------------------------------------------------------
    /**
     * Envia uma mensagem de texto (acionado pelo botão ou Enter).
     * - Salva no banco local (privado ou grupo)
     * - Exibe imediatamente na tela
     * - Envia pela rede via Cliente
     */
    private void enviarMensagemTexto() {
        String texto = textFieldMensagem.getText();
        if (texto == null || texto.trim().isEmpty()) return;

        String textoCriptografado = Criptografia.criptografar(texto);
        boolean isGrupo = destinatario != null && destinatario.endsWith(" (Grupo)");

        // 1. Persistência local
        if (isGrupo) {
            String nomeGrupo = destinatario.replace(" (Grupo)", "");
            dao.salvarMensagemGrupo(nomeGrupo, usuarioLogado, textoCriptografado, Mensagem.TipoMensagem.TEXTO);
        } else {
            dao.salvarMensagem(usuarioLogado, destinatario, textoCriptografado, Mensagem.TipoMensagem.TEXTO);
        }

        // 2. Exibição visual imediata
        adicionarBalaoVisual(usuarioLogado, texto);
        revalidarERolar();

        // 3. Envio pela rede
        Mensagem msg = new Mensagem(usuarioLogado, destinatario, textoCriptografado);
        msg.setTipo(Mensagem.TipoMensagem.TEXTO);
        Cliente.enviar(msg);

        textFieldMensagem.setText("");
    }

    /**
     * Abre um seletor de arquivos, lê o conteúdo, salva referência no banco e envia pela rede.
     * Limite de 10 MB.
     */
    private void selecionarEEnviarArquivo() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File arquivo = chooser.getSelectedFile();
        if (arquivo.length() > 10 * 1024 * 1024) {
            JOptionPane.showMessageDialog(this, "Arquivo muito grande! Máximo 10MB.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            byte[] dados = Files.readAllBytes(arquivo.toPath());
            String identificador = Criptografia.criptografar("[ARQUIVO]:" + arquivo.getName());
            boolean isGrupo = destinatario != null && destinatario.endsWith(" (Grupo)");

            // Salva no banco local
            if (isGrupo) {
                String nomeGrupo = destinatario.replace(" (Grupo)", "");
                dao.salvarMensagemGrupo(nomeGrupo, usuarioLogado, identificador, Mensagem.TipoMensagem.ARQUIVO);
            } else {
                dao.salvarMensagem(usuarioLogado, destinatario, identificador, Mensagem.TipoMensagem.ARQUIVO, null);
            }

            // Envia pela rede
            Mensagem msg = new Mensagem(usuarioLogado, destinatario, dados, arquivo.getName(), Mensagem.TipoMensagem.ARQUIVO);
            Cliente.enviar(msg);

            // Exibe visualmente (o caminho no servidor pode ser nulo por enquanto)
            adicionarBalaoArquivo(usuarioLogado, arquivo.getName(), null);
            revalidarERolar();

            JOptionPane.showMessageDialog(this, "Arquivo enviado!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao ler arquivo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------
    // Download de arquivo
    // -------------------------------------------------------------
    /**
     * Baixa um arquivo a partir do caminho armazenado no servidor (ou local).
     * Solicita ao usuário onde salvar e escreve os bytes.
     */
    private void baixarArquivo(String caminhoArquivo, String nomeArquivo) {
        if (caminhoArquivo == null || caminhoArquivo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Caminho do arquivo não disponível.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        byte[] dadosArquivo = Arquivo.carregarArquivo(caminhoArquivo);
        if (dadosArquivo == null) {
            JOptionPane.showMessageDialog(this, "Arquivo não encontrado no servidor.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(nomeArquivo));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileOutputStream fos = new FileOutputStream(chooser.getSelectedFile())) {
                fos.write(dadosArquivo);
                JOptionPane.showMessageDialog(this, "Arquivo salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}