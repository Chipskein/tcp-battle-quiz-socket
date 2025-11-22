package com.server.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.server.negocio.Player;
import com.server.negocio.Question;
import com.server.negocio.Match;

public class InMemoryDB {

    private static InMemoryDB instance;

    private List<Question> questions;

    private List<Match> matches = Collections.synchronizedList(new ArrayList<>());

    private static final String DATA_FILE = "data.json";

    private InMemoryDB() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            InputStream dataStream = InMemoryDB.class
                    .getClassLoader()
                    .getResourceAsStream(DATA_FILE);

            if (dataStream == null) {
                throw new RuntimeException(DATA_FILE + " not found in resources!");
            }

            questions = mapper.readValue(
                dataStream,
                new TypeReference<List<Question>>() {}
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + DATA_FILE, e);
        }
    }


    public static synchronized InMemoryDB getInstance() {
        if (instance == null) instance = new InMemoryDB();
        return instance;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public Optional<Question> findById(int id) {
        return questions.stream().filter(i -> i.id == id).findFirst();
    }

    public void addMatch(Player p1, Player p2) {
        matches.add(new Match(p1, p2));
    }

    public void removeMatch(Player p1, Player p2) {
        matches.removeIf(m -> (m.getP1().equals(p1) && m.getP2().equals(p2)) ||
                            (m.getP1().equals(p2) && m.getP2().equals(p1)));
    }

    public Optional<Match> getMatch(Player p) {
        return matches.stream().filter(m -> m.contains(p)).findFirst();
    }

}
