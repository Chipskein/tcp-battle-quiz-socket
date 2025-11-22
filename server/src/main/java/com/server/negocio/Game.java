package com.server.negocio;

import java.io.IOException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.server.database.InMemoryDB;
import com.server.negocio.ClientMessageUtil.AnswerData;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class Game {
    private static final Logger log = Logger.getLogger(Game.class.getName());

    private final int POINTS_TO_WIN = 30;

    private final int POINTS_FIRST_HIT = 5;

    private final int POINTS_FIRST_MISS = 3;

    private final Set<Integer> askedQuestions = new HashSet<>();

    private final InMemoryDB db;

    private final Match currentMatch;

    public Game(Match currentMatch) {
        this.db = InMemoryDB.getInstance();
        this.currentMatch = currentMatch;
    }

    public Game(Player p1, Player p2) {
        this.db = InMemoryDB.getInstance();
        this.currentMatch = new Match(p1, p2);
    }

    public void reset(){
        log.info("Resetting game state");
        askedQuestions.clear();
        currentMatch.getPlayers().forEach(p -> p.removeScore(p.getScore()));

    }

    public Question getRandomQuestion() {
        var questions = db.getQuestions();

        Question question = questions.get(
            ThreadLocalRandom.current().nextInt(questions.size())
        );

        while (askedQuestions.contains(question.id)) {
            question = questions.get(
                ThreadLocalRandom.current().nextInt(
                    questions.size() - askedQuestions.size()
                )
            );
        }

        askedQuestions.add(question.id);

        return question;
    }

    public boolean checkAnswer(int questionId, String answer) {
        Optional<Question> questionOpt = db.findById(questionId);

        if (questionOpt.isEmpty()) {
            return false;
        }
        Question question = questionOpt.get();

        Option ans = Option.fromString(answer);

        return question.correctAnswer == ans;
    }

    public void checkRound(Player p1, Player p2, int questionId) throws IOException {
        ExecutorService exec = Executors.newFixedThreadPool(2);

        CompletionService<PlayerAnswer> completion = new ExecutorCompletionService<>(exec);

        Future<PlayerAnswer> f1 = completion.submit(() -> {
            AnswerData ans = ClientMessageUtil.waitFor(p1,Command.SENT_ANSWER,ClientMessageUtil.AnswerData.class);
            return new PlayerAnswer(p1, ans.answer);
        });

        Future<PlayerAnswer> f2 = completion.submit(() -> {
            AnswerData ans = ClientMessageUtil.waitFor(p2,Command.SENT_ANSWER,ClientMessageUtil.AnswerData.class);
            return new PlayerAnswer(p2, ans.answer);
        });

        try {
            PlayerAnswer first = completion.take().get();
            PlayerAnswer second = completion.take().get();

            boolean firstCorrect = checkAnswer(questionId, first.answer);
            boolean secondCorrect = checkAnswer(questionId, second.answer);

            Player firstPlayer = first.player;
            Player otherPlayer = (firstPlayer == p1 ? p2 : p1);

            if (firstCorrect) {
                firstPlayer.addScore(POINTS_FIRST_HIT);
            } else if (secondCorrect) {
                otherPlayer.addScore(POINTS_FIRST_MISS);
            }

        } catch (InterruptedException | ExecutionException e) {
            log.log(Level.SEVERE, "Error during round check: {0}", e.getMessage());
            p1.close();
            p2.close();
            throw new RuntimeException("Error during round check", e);
        }
        finally {
            f1.cancel(true);
            f2.cancel(true);
            exec.shutdownNow();
        }
    }

    public boolean hasWinner() {
        return (currentMatch.getPlayers().stream()
                .anyMatch(p -> p.getScore() >= POINTS_TO_WIN));
    }

}
