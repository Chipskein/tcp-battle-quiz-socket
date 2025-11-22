package com.server.negocio;

import java.util.List;

public class Match {
    private final Player p1;

    private final Player p2;

    public Match(Player p1, Player p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Player getP1() {
        return p1;
    }
    public Player getP2() {
        return p2;
    }

    public boolean contains(Player p) {
        return p.equals(p1) || p.equals(p2);
    }

    public List<Player> getPlayers() {
        return List.of(p1, p2);
    }
}
