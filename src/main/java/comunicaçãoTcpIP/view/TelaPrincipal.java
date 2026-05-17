package comunicaçãoTcpIP.view;

import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import comunicaçãoTcpIP.database.DAO;

/**
 * Tela principal do chat.
 * Exibe a lista de conversas ativas do usuário e permite abrir novas conversas
 * (privadas ou em grupo).
 */
public class TelaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    // Constantes para evitar "números mágicos"
    private static final int WINDOW_WIDTH = 807;
    private static final int WINDOW_HEIGHT = 615;
    private static final int LIST_REFRESH_DELAY_MS = 5000; // 5 segundos

    // Componentes da UI
    private JPanel contentPane;
    private DefaultListModel<String> modelConversas;
    private JList<String> listConversas;

    // Lógica e dados
    private final String usuarioLogado;
    private final DAO dao = new DAO();

    // Controle de janelas de chat abertas (evita múltiplas instâncias)
    private final Map<String, TelaChat> conversasAbertas = new HashMap<>();

    // -------------------------------------------------------------
    // Método principal (apenas para teste / execução isolada)
    // -------------------------------------------------------------
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                TelaPrincipal frame = new TelaPrincipal("Teste");
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // -------------------------------------------------------------
    // Construtor
    // -------------------------------------------------------------
    public TelaPrincipal(String usuario) {
        this.usuarioLogado = usuario;
        inicializarUI();
        iniciarTimerAtualizacaoLista();
        atualizarHistoricoConversas(); // carrega lista pela primeira vez
    }

    // -------------------------------------------------------------
    // Configuração da interface gráfica
    // -------------------------------------------------------------
    private void inicializarUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setBounds(100, 100, WINDOW_WIDTH, WINDOW_HEIGHT);
        setTitle("APS CHAT - Logado como: " + this.usuarioLogado);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(30, 30, 30));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null); // layout absoluto (preserva funcionalidade original)
        setContentPane(contentPane);

        criarListaConversas();
        criarPainelBotoes();
    }

    /**
     * Cria a lista rolável que exibe as conversas (privadas ou grupos).
     * Adiciona um listener de duplo clique para abrir a conversa selecionada.
     */
    private void criarListaConversas() {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 70, 773, 498);
        contentPane.add(scrollPane);

        modelConversas = new DefaultListModel<>();
        listConversas = new JList<>(modelConversas);
        listConversas.setBackground(new Color(40, 40, 40));
        listConversas.setForeground(Color.GREEN);
        scrollPane.setViewportView(listConversas);

        // Duplo clique: abre a conversa (privada ou grupo)
        listConversas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String conversaSelecionada = listConversas.getSelectedValue();
                    if (conversaSelecionada != null) {
                        abrirTelaChat(conversaSelecionada);
                    }
                }
            }
        });
    }

    /**
     * Cria o painel superior com os botões "Nova Conversa" e "Novo Grupo".
     */
    private void criarPainelBotoes() {
        JPanel panel = new JPanel();
        panel.setBounds(10, 10, 400, 50);
        panel.setLayout(null);
        panel.setBackground(new Color(30, 30, 30));
        contentPane.add(panel);

        // Botão para iniciar conversa privada
        JButton btnNovoChat = new JButton("Nova Conversa");
        btnNovoChat.setBounds(10, 10, 140, 30);
        btnNovoChat.setBackground(new Color(10, 90, 40));
        btnNovoChat.setForeground(Color.WHITE);
        btnNovoChat.addActionListener(e -> exibirJanelaSelecaoContatos());
        panel.add(btnNovoChat);

        // Botão para criar grupo
        JButton btnNovoGrupo = new JButton("Novo Grupo");
        btnNovoGrupo.setBounds(160, 10, 140, 30);
        btnNovoGrupo.setBackground(new Color(10, 90, 40));
        btnNovoGrupo.setForeground(Color.WHITE);
        btnNovoGrupo.addActionListener(e -> criarNovoGrupoComMembros());
        panel.add(btnNovoGrupo);
    }

    /**
     * Inicia um timer que atualiza automaticamente a lista de conversas
     * a cada LIST_REFRESH_DELAY_MS milissegundos.
     */
    private void iniciarTimerAtualizacaoLista() {
        Timer timerLista = new Timer(LIST_REFRESH_DELAY_MS, e -> atualizarHistoricoConversas());
        timerLista.start();
    }

    // -------------------------------------------------------------
    // Lógica de negócio e interação com o banco de dados (DAO)
    // -------------------------------------------------------------

    /**
     * Busca no banco todas as conversas (privadas ou grupos) em que o usuário logado participa
     * e atualiza o modelo da JList.
     * A atualização é feita na EDT (Event Dispatch Thread) para garantir segurança em Swing.
     */
    private void atualizarHistoricoConversas() {
        SwingUtilities.invokeLater(() -> {
            List<String> conversasAtivas = dao.conversas(this.usuarioLogado);
            modelConversas.clear();
            for (String conversa : conversasAtivas) {
                modelConversas.addElement(conversa);
            }
        });
    }

    /**
     * Exibe uma janela com todos os outros usuários do sistema.
     * O usuário escolhe um contato e inicia uma conversa privada com ele.
     */
    private void exibirJanelaSelecaoContatos() {
        List<String> todosUsuarios = dao.listarUsuarios(this.usuarioLogado);
        if (todosUsuarios.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum outro usuário cadastrado no sistema.");
            return;
        }

        DefaultListModel<String> modelContatos = new DefaultListModel<>();
        todosUsuarios.forEach(modelContatos::addElement);

        JList<String> listContatos = new JList<>(modelContatos);
        JScrollPane scroll = new JScrollPane(listContatos);

        int resultado = JOptionPane.showConfirmDialog(this, scroll,
                "Selecione um contato para conversar:",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            String selecionado = listContatos.getSelectedValue();
            if (selecionado != null) {
                abrirTelaChat(selecionado);
                atualizarHistoricoConversas(); // garante que a nova conversa apareça na lista
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum contato foi selecionado.");
            }
        }
    }

    /**
     * Fluxo completo para criação de um grupo:
     * 1. Solicita o nome do grupo.
     * 2. Exibe lista de usuários (múltipla escolha) para selecionar membros.
     * 3. Inclui automaticamente o criador como membro.
     * 4. Chama o DAO para salvar o grupo no banco.
     * 5. Se bem-sucedido, abre a tela de chat do grupo e atualiza a lista de conversas.
     */
    private void criarNovoGrupoComMembros() {
        String nomeGrupo = JOptionPane.showInputDialog(this, "Digite o nome do novo grupo:");
        if (nomeGrupo == null || nomeGrupo.trim().isEmpty()) return;

        List<String> todosUsuarios = dao.listarUsuarios(this.usuarioLogado);
        if (todosUsuarios.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Não há outros usuários para adicionar.");
            return;
        }

        // Painel com instrução e lista de seleção múltipla
        DefaultListModel<String> modelMembros = new DefaultListModel<>();
        todosUsuarios.forEach(modelMembros::addElement);
        JList<String> listMembros = new JList<>(modelMembros);
        listMembros.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scroll = new JScrollPane(listMembros);

        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.add(new JLabel("Segure CTRL para selecionar múltiplos contatos:"));
        painel.add(scroll);

        int resultado = JOptionPane.showConfirmDialog(this, painel,
                "Adicionar membros ao grupo: " + nomeGrupo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            List<String> membrosSelecionados = listMembros.getSelectedValuesList();
            if (!membrosSelecionados.isEmpty()) {
                // O criador do grupo também participa
                membrosSelecionados.add(this.usuarioLogado);
                boolean sucesso = dao.salvarGrupoComMembros(nomeGrupo, membrosSelecionados);
                if (sucesso) {
                    JOptionPane.showMessageDialog(this, "Grupo '" + nomeGrupo + "' criado com sucesso!");
                    abrirTelaChat(nomeGrupo + " (Grupo)"); // padrão adotado para identificar grupos
                    atualizarHistoricoConversas();
                } else {
                    JOptionPane.showMessageDialog(this, "Erro ao criar grupo.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum membro selecionado.");
            }
        }
    }

    /**
     * Abre uma janela de chat (TelaChat) para a conversa especificada.
     * Caso a janela já esteja aberta (controlada pelo mapa conversasAbertas),
     * apenas a traz para frente, evitando duplicatas.
     *
     * @param identificadorConversa Nome do contato (privado) ou nome do grupo com sufixo " (Grupo)"
     */
    private void abrirTelaChat(String identificadorConversa) {
        if (conversasAbertas.containsKey(identificadorConversa)) {
            conversasAbertas.get(identificadorConversa).toFront();
            return;
        }

        TelaChat chat = new TelaChat(usuarioLogado, identificadorConversa);
        // Quando a janela de chat for fechada, a removemos do mapa
        chat.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                conversasAbertas.remove(identificadorConversa);
            }
        });

        conversasAbertas.put(identificadorConversa, chat);
        chat.setLocationRelativeTo(this);
        chat.setVisible(true);
    }
}