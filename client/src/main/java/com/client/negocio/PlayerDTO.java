package com.client.negocio;

public class PlayerDTO {
    private String nickname;
    private int score;

    public PlayerDTO() {}

    public PlayerDTO(String nickname, int score) {
        this.nickname = nickname;
        this.score = score;
    }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}