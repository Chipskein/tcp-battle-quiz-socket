package com.server.negocio;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientMessageUtil {
    private static final Logger log = Logger.getLogger(ClientMessageUtil.class.getName());

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T waitFor(Player p, Command expected, Class<T> clazz) throws RuntimeException {
        while (true) {

            if (Thread.currentThread().isInterrupted()) {
                log.log(Level.INFO, "waitFor interrompido para jogador {0}", p.getNickname());
                throw new RuntimeException("waitFor interrupted");
            }

            try {
                log.log(Level.INFO, "Aguardando comando {0} de {1}", new Object[]{expected, p.getNickname()});
                //SENT_PLAY_AGAIN,{"again":true}
                //SENT_YOUR_NICKNAME,{"nickname":"Player1"}
                //SENT_ANSWER,{"answer":"A"}
                String raw = p.waitClientMessage();

                if (raw == null) {
                    log.log(Level.SEVERE, "Player {0} desconectou.", p.getNickname());
                    throw new RuntimeException("Client disconnected");
                }

                if (raw.trim().isEmpty())
                    continue;

                String[] parts = raw.split(",", 2);

                if (parts.length != 2){
                    log.log(Level.WARNING, "Malformed message from {0}: {1}", new Object[]{p.getNickname(), raw});
                    p.sendError("Malformed message: expected COMMAND,{json}");
                    continue;
                }

                Command cmd;
                try {
                    cmd = Command.valueOf(parts[0]);
                } catch (IllegalArgumentException e) {
                    log.log(Level.WARNING, "Unknown command: {0} de {1}", new Object[]{parts[0], p.getNickname()});
                    p.sendError("Unknown command: " + parts[0]);
                    continue;
                }

                if (cmd == expected) {
                    try{
                        log.log(Level.INFO, "Comando {0} recebido de {1}", new Object[]{cmd, p.getNickname()});
                        return mapper.readValue(parts[1], clazz);
                    } catch (JsonProcessingException e){
                        log.log(Level.WARNING, "Error parsing message from {0}: {1}", new Object[]{p.getNickname(), e.getMessage()});
                        p.sendError("Error parsing message: " + e.getMessage());
                        continue;
                    }

                }

                log.log(Level.WARNING, "Unexpected command: {0} de {1}, esperando {2}", new Object[]{cmd, p.getNickname(), expected});
                p.sendError("Unexpected command: " + cmd + ", expected " + expected);

            } catch (IOException e) {
                log.log(Level.SEVERE, "I/O error from " + p.getNickname(), e);
                throw new RuntimeException("Client disconnected", e);
            }

        }
    }

    public static class PlayAgainData {
        public boolean  again;
    }

    public static class NicknameData {
        public String nickname;
    }

    public static class AnswerData {
        public String answer;
    }
}
