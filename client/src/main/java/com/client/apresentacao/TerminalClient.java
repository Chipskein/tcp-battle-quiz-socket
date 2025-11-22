package com.client.apresentacao;

import com.server.negocio.Command;
import com.server.negocio.Question;
import com.client.negocio.PlayerDTO;
import com.server.negocio.ClientMessageUtil.AnswerData;
import com.server.negocio.ClientMessageUtil.NicknameData;
import com.server.negocio.ClientMessageUtil.PlayAgainData;
import com.client.negocio.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;

public class TerminalClient {

    public static void main(String[] args) {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        ObjectMapper mapper = new ObjectMapper();

        try {
            System.out.print("Digite o host do servidor: ");
            String host = console.readLine();

            System.out.print("Digite a porta do servidor: ");
            int port = Integer.parseInt(console.readLine());

            try (Socket socket = new Socket(host, port)) {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                System.out.println("Conectado ao servidor " + host + ":" + port);

                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        System.out.println("Servidor desconectou.");
                        break;
                    }

                    line = line.trim();
                    if (line.isEmpty() || !line.contains(",")) {
                        continue;
                    }

                    String cmd = line.split(",")[0];
                    String data = line.substring(line.indexOf(",") + 1);

                    Message msg = new Message(Command.valueOf(cmd),data);

                    switch (msg.getCommand()) {
                        case CONNECTED -> System.out.println("Conexão estabelecida com o servidor. Aguardando outro jogador...");

                        case SEND_YOUR_NICKNAME -> {
                            System.out.print("Digite seu nickname: ");
                            String nickname = console.readLine();
                            NicknameData nicknameData = new NicknameData();
                            nicknameData.nickname = nickname;
                            Message nickMsg = new Message(Command.SENT_YOUR_NICKNAME, mapper.writeValueAsString(nicknameData));
                            out.write(nickMsg.toString()+ "\n");
                            out.flush();
                        }

                        case SHOW_QUESTION -> {
                            Question question = mapper.readValue(msg.getData(), Question.class);
							System.out.println("Pergunta: " + question.question);
							question.options.keySet().stream().sorted().forEach(key -> {
								String option = question.options.get(key);
								System.out.println(key + ": " + option);
							});
                            System.out.print("Digite sua resposta: ");
                            String answer = console.readLine();
                            AnswerData answerData = new AnswerData();
                            answerData.answer = answer;
                            Message answerMsg = new Message(Command.SENT_ANSWER, mapper.writeValueAsString(answerData));
                            out.write(answerMsg.toString() + "\n");
                            out.flush();
                        }

                        case ASK_PLAY_AGAIN -> {
                            System.out.print("Deseja jogar novamente? (sim/nao): ");
                            String again = console.readLine();
                            boolean playAgain = again.equalsIgnoreCase("sim");
                            PlayAgainData playAgainData = new PlayAgainData();
                            playAgainData.again = playAgain;
                            Message playAgainMsg = new Message(Command.SENT_PLAY_AGAIN, mapper.writeValueAsString(playAgainData));
                            out.write(playAgainMsg.toString() + "\n");
                            out.flush();
                        }

                        case GAME_START -> System.out.println("O jogo começou!");

                        case GAME_END -> System.out.println("O jogo terminou!");

                        case SHOW_SCORE -> {
							PlayerDTO[] players = mapper.readValue(msg.getData(), PlayerDTO[].class);
							System.out.println("Pontuação Atual:");
							for (PlayerDTO p : players) {
								System.out.println("- " + p.getNickname() + ": " + p.getScore());
							}
						}

                        case ERROR -> System.err.println("Erro do servidor: " + msg.getData());

                        case DISCONNECTED -> {
                            System.out.println("Servidor desconectou!");
                            socket.close();
                            return;
                        }

                        default -> System.out.println("Comando desconhecido: " + msg.getCommand() + ", dados: " + msg.getData());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Erro de comunicação: " + e.getMessage());
        }
    }
}
