package com.stenbergroom.blackjack;

public class PlayerRun {

    public static void main(String[] args) {
        Player application;
        application = new Player("127.0.0.1");
        application.runClient();
    }
}
