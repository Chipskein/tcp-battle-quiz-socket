package com.client.apresentacao;

import com.client.negocio.PlayerDTO;
import com.client.negocio.AudioManager;
import com.client.negocio.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.negocio.Command;
import com.server.negocio.Question;
import com.server.negocio.ClientMessageUtil.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

import com.formdev.flatlaf.FlatLightLaf;

public class GUIClient {

    private JFrame frame;

    private final AudioManager audioManager;

    // Panels
    private JPanel mainPanel;
    private JPanel nicknamePanel;
    private JPanel questionPanel;
    private JPanel playAgainPanel;
    private JPanel statusPanel;

    // Components
    private JTextField nicknameField;
    private JButton nicknameButton;
    private JLabel questionLabel;
    private JPanel optionsPanel;
    private JButton playYesButton;
    private JButton playNoButton;
    private JTextArea statusArea;
    private JButton optionsButton;
    private JPanel volumePanel;
    private JSlider bgmSlider;
    private JSlider fxSlider;

    private BufferedWriter out;
    private final ObjectMapper mapper = new ObjectMapper();

    private String host;
    private int port;
    private static Image icon = null;

    private String nickname;

    public GUIClient() {
        audioManager = new AudioManager();
        audioManager.setBGMVolume(-30f);
        audioManager.setFXVolume(-15f);
        setupGUI();
        audioManager.playBGM("ost/theme.wav", true);
    }

    private void setupGUI() {
        frame = new JFrame("Battle Quiz Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLayout(new BorderLayout(10, 10));

        mainPanel = new JPanel(new CardLayout());

        // --- Nickname Panel ---
        nicknamePanel = new JPanel(new BorderLayout(5, 5));
        nicknameField = new JTextField();
        nicknameButton = new JButton("Enviar");
        JPanel nickInput = new JPanel(new BorderLayout());
        nickInput.add(nicknameField, BorderLayout.CENTER);
        nickInput.add(nicknameButton, BorderLayout.EAST);
        nicknamePanel.add(new JLabel("Digite seu nickname:"), BorderLayout.NORTH);
        nicknamePanel.add(nickInput, BorderLayout.CENTER);

        nicknameButton.addActionListener(e -> sendNickname());
        nicknameField.addActionListener(e ->  sendNickname());

        // --- Question Panel ---
        questionPanel = new JPanel(new BorderLayout(10, 10));
        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(0, 1, 5, 5));
        questionPanel.add(questionLabel, BorderLayout.NORTH);
        questionPanel.add(optionsPanel, BorderLayout.CENTER);

        // --- Play Again Panel ---
        playAgainPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        playYesButton = new JButton("Sim");
        playNoButton = new JButton("Não");
        playAgainPanel.add(playYesButton);
        playAgainPanel.add(playNoButton);
        playYesButton.addActionListener(e -> sendPlayAgain(true));
        playNoButton.addActionListener(e -> sendPlayAgain(false));

        // --- Status Panel ---
        statusPanel = new JPanel(new BorderLayout());
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        statusPanel.add(scrollPane, BorderLayout.CENTER);
        statusPanel.setPreferredSize(new Dimension(frame.getWidth(), 150));

        // --- Options Button ---
        optionsButton = new JButton("Opções");
        optionsButton.addActionListener(e -> toggleVolumePanel());
        statusPanel.add(optionsButton, BorderLayout.NORTH);

        // --- Volume Panel ---
        volumePanel = new JPanel();
        volumePanel.setLayout(new BoxLayout(volumePanel, BoxLayout.Y_AXIS));
        volumePanel.setBorder(BorderFactory.createTitledBorder("Volume"));

        // BGM
        JLabel bgmLabel = new JLabel("BGM", SwingConstants.CENTER);
        bgmLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bgmSlider = new JSlider(JSlider.VERTICAL, -80, 6, (int) audioManager.getBGMVolume());
        bgmSlider.setMajorTickSpacing(20);
        bgmSlider.setMinorTickSpacing(5);
        bgmSlider.setPaintTicks(true);
        bgmSlider.setPaintLabels(true);
        bgmSlider.addChangeListener(e -> audioManager.setBGMVolume(bgmSlider.getValue()));
        bgmSlider.setAlignmentX(Component.CENTER_ALIGNMENT);

        // FX
        JLabel fxLabel = new JLabel("FX", SwingConstants.CENTER);
        fxLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        fxSlider = new JSlider(JSlider.VERTICAL, -80, 6, (int) audioManager.getFXVolume());
        fxSlider.setMajorTickSpacing(20);
        fxSlider.setMinorTickSpacing(5);
        fxSlider.setPaintTicks(true);
        fxSlider.setPaintLabels(true);
        fxSlider.addChangeListener(e -> audioManager.setFXVolume(fxSlider.getValue()));
        fxSlider.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add sliders
        volumePanel.add(bgmLabel);
        volumePanel.add(bgmSlider);
        volumePanel.add(Box.createVerticalStrut(10));
        volumePanel.add(fxLabel);
        volumePanel.add(fxSlider);

        // hide initially
        volumePanel.setVisible(false);

        // --- Add panels to CardLayout ---
        mainPanel.add(nicknamePanel, "nickname");
        mainPanel.add(questionPanel, "question");
        mainPanel.add(playAgainPanel, "playagain");

        frame.add(volumePanel, BorderLayout.EAST);
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(statusPanel, BorderLayout.SOUTH);

        if (icon != null) frame.setIconImage(icon);

        frame.setVisible(true);
    }

    private void toggleVolumePanel() {
        volumePanel.setVisible(!volumePanel.isVisible());
        frame.revalidate();
        frame.repaint();
    }

    private void sendNickname() {
        try {
            nickname = nicknameField.getText().trim();
            if (nickname.isEmpty() || out == null) return;
            NicknameData data = new NicknameData();
            data.nickname = nickname;
            sendMessage(new Message(Command.SENT_YOUR_NICKNAME, mapper.writeValueAsString(data)));
            nicknameField.setText("");
            showPanel("status");
        } catch (IOException ex) {
            appendStatus("Erro ao enviar nickname: " + ex.getMessage());
        }
    }

    private void sendPlayAgain(boolean again) {
        try {
            PlayAgainData data = new PlayAgainData();
            data.again = again;
            sendMessage(new Message(Command.SENT_PLAY_AGAIN, mapper.writeValueAsString(data)));
            showPanel("status");
        } catch (IOException ex) {
            appendStatus("Erro ao enviar Play Again: " + ex.getMessage());
        }
    }

    private void sendMessage(Message msg) throws IOException {
        out.write(msg.toString() + "\n");
        out.flush();
    }

    private void showPanel(String name) {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, name);
    }

    public void startClient(String host, int port) {
        this.host = host;
        this.port = port;
        new Thread(this::connectAndListen).start();
    }

    private void connectAndListen() {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            appendStatus("Conectado ao servidor " + host + ":" + port);

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || !line.contains(",")) continue;

                String cmd = line.split(",")[0];
                String data = line.substring(line.indexOf(",") + 1);
                Message msg = new Message(Command.valueOf(cmd), data);

                SwingUtilities.invokeLater(() -> handleMessage(msg));
            }

            appendStatus("Servidor desconectou.");

        } catch (IOException e) {
            appendStatus("Erro de comunicação: " + e.getMessage());
        }
    }

    private void handleMessage(Message msg) {
        try {
            switch (msg.getCommand()) {
                case CONNECTED -> appendStatus("Conexão estabelecida. Aguardando outro jogador...");

                case SEND_YOUR_NICKNAME -> showPanel("nickname");

                case SHOW_QUESTION -> {
                    audioManager.playFXDelayed("ost/fx/new_question.wav",800);

                    audioManager.playFXDelayed(getGagAudio(), 3500);

                    Question question = mapper.readValue(msg.getData(), Question.class);
                    questionLabel.setText("<html><center>" + question.question + "</center></html>");
                    optionsPanel.removeAll();
                    for (String key : question.options.keySet().stream().sorted().toList()) {
                        JButton btn = new JButton(key + ": " + question.options.get(key));
                        btn.setFont(new Font("SansSerif", Font.PLAIN, 16));
                        btn.addActionListener(e -> {
                            try {
                                AnswerData answerData = new AnswerData();
                                answerData.answer = key;
                                sendMessage(new Message(Command.SENT_ANSWER, mapper.writeValueAsString(answerData)));
                                if( key.equals(question.correctAnswer.toString()) ) {
                                    audioManager.playFX("ost/fx/right.wav");
                                } else {
                                    audioManager.playFX("ost/fx/wrong.wav");
                                }
                                for (Component c : optionsPanel.getComponents()) c.setEnabled(false);
                            } catch (IOException ex) {
                                if(ex.getMessage().equalsIgnoreCase("socket closed")){
                                    appendStatus("Conexão fechada. Não é possível enviar a resposta. Closing in 3 seconds...");
                                    Timer timer = new Timer(3000, evt -> {
                                        frame.dispose();
                                        System.exit(0);
                                    });
                                    timer.setRepeats(false);
                                    timer.start();
                                    return;
                                }
                                appendStatus("Erro ao enviar resposta: " + ex.getMessage());
                            }
                        });
                        optionsPanel.add(btn);
                    }
                    optionsPanel.revalidate();
                    optionsPanel.repaint();
                    showPanel("question");
                }

                case ASK_PLAY_AGAIN -> showPanel("playagain");

                case GAME_START -> {
                    audioManager.playFX("ost/fx/game_start.wav");
                    audioManager.playFX("ost/fx/good_luck.wav");
                    appendStatus("O jogo começou!");
                }

                case GAME_END -> appendStatus("O jogo terminou!");

                case SHOW_SCORE -> {
                    PlayerDTO[] players = mapper.readValue(msg.getData(), PlayerDTO[].class);
                    appendStatus("Pontuação Atual:");
                    for (PlayerDTO p : players) appendStatus("- " + p.getNickname() + ": " + p.getScore());
                }

                case ERROR -> appendStatus("Erro do servidor: " + msg.getData());

                case NEW_GAME -> {
                    statusArea.setText("");
                    appendStatus("Novo jogo iniciado!");
                }

                case DISCONNECTED -> {
                    appendStatus("Servidor desconectou!");
                    SwingUtilities.invokeLater(() -> {
                        frame.dispose();
                        System.exit(0);
                    });
                }

                default -> appendStatus("Comando desconhecido: " + msg.getCommand());
            }
        } catch (IOException e) {
            appendStatus("Erro ao processar mensagem: " + e.getMessage());
        }
    }

    private void appendStatus(String text) {
        statusArea.append(text + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    private String getGagAudio(){
        String[] gags = {
            "ost/fx/20__to_you_go_away.wav",
            "ost/fx/are_you_sure-2.wav",
            "ost/fx/are_you_sure.wav",
            "ost/fx/get_at_least_a_three.wav",
            "ost/fx/this_one_is_hard.wav",
            "ost/fx/what_the_right_one.wav",
            "ost/fx/who_you_gonna_ask_for_help.wav",
            "ost/fx/you_dont_know_this_one.wav",
            "ost/fx/you_undestrand_the_question.wav",
        };
        int idx = (int) (Math.random() * gags.length);
        return gags[idx];
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {

            ImageIcon tmpImg = null;
            InputStream is = GUIClient.class.getClassLoader().getResourceAsStream("logo.png");
            if (is != null) {
                try {
                    Image image = ImageIO.read(is);
                    GUIClient.icon = image;
                    tmpImg = new ImageIcon(image.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
                } catch (IOException ex) {
                    System.out.println("Erro ao carregar o logo: " + ex.getMessage());
                }
            } else {
                System.out.println("Logo não encontrado!");
            }

            String host = (String) JOptionPane.showInputDialog(
                null,
                "Digite o host do servidor:",
                "Conexão",
                JOptionPane.PLAIN_MESSAGE,
                tmpImg,
                null,
                ""
            );
            if (host == null || host.trim().isEmpty()) return;

            String portStr = (String) JOptionPane.showInputDialog(
                null,
                "Digite a porta do servidor:",
                "Porta",
                JOptionPane.PLAIN_MESSAGE,
                tmpImg,
                null,
                ""
            );
            if (portStr == null || portStr.trim().isEmpty()) return;

            int port;
            try {
                port = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Porta inválida!",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE,
                    tmpImg
                );
                return;
            }

            GUIClient client = new GUIClient();
            client.startClient(host, port);
        });
    }
}
