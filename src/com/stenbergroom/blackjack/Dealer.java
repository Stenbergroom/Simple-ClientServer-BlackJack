package com.stenbergroom.blackjack;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dealer {

    private Deck newDeck;
    private ExecutorService executor;
    private ServerSocket server;
    private SocketServer[] socketServer;
    private int countOfConnections = 1;
    private String firstCard, secondCard;
    private ArrayList<PlayGame> players;
    private PlayGame dCards;
    private int playersLeft;
    private boolean roundStarted = false;
    private Scanner scan = new Scanner(System.in);

    public Dealer() {
        players = new ArrayList();
        socketServer = new SocketServer[100];
        executor = Executors.newFixedThreadPool(100);
        displayMessage("\n\t\t*****Welcome to Blackjack game!*****\n");
    }

    // set up and run server
    public void runDeal() {
        try {
            server = new ServerSocket(1992, 100);
            while (true) {
                try {
                    socketServer[countOfConnections] = new SocketServer(countOfConnections);
                    socketServer[countOfConnections].waitForConnection();
                    executor.execute(socketServer[countOfConnections]);
                } catch (EOFException e) {
                    displayMessage("\nServer terminated connection");
                } finally {
                    ++countOfConnections;
                }
            }
        } catch (IOException e) {}
    }

    private void displayMessage(final String messageToDisplay) {
        System.out.println(messageToDisplay);
    }

    private void dealCards(){
        try{
            playersLeft = countOfConnections -1;
            newDeck.shuffle();
            firstCard = newDeck.dealCard();
            secondCard = newDeck.dealCard();
            displayMessage("\n\n" + firstCard + " " + secondCard);

            for (int i = 1;i <= countOfConnections;i++) {
                String c1,c2;
                c1 = newDeck.dealCard();
                c2 = newDeck.dealCard();
                PlayGame p = new PlayGame(c1,c2);
                players.add(p);
                socketServer[i].sendData("You were Dealt:\n" + c1 + " " + c2);
                socketServer[i].sendData("Select hit or stay? - (h/s) Your Total: " + p.getCardTotal());
            }
        } catch (NullPointerException e){}
    }

    private void results() {
        try{
            for (int i = 1;i <= countOfConnections;i++) {
                socketServer[i].sendData("Dealer has " + dCards.getCardTotal());
                if( (dCards.getCardTotal() <= 21) && (players.get(i-1).getCardTotal() <= 21 ) ){
                    if (dCards.getCardTotal() > players.get(i-1).getCardTotal()) {
                        socketServer[i].sendData("\nYou Lose!");
                    }
                    if (dCards.getCardTotal() < players.get(i-1).getCardTotal()) {
                        socketServer[i].sendData("\nYou Win!");
                    }
                    if (dCards.getCardTotal() == players.get(i-1).getCardTotal()) {
                        socketServer[i].sendData("\nTie!");
                    }
                }

                if(dCards.checkBust()){
                    if(players.get(i-1).checkBust()){
                        socketServer[i].sendData("\nTie!");
                    }
                    if(players.get(i-1).getCardTotal() <= 21){
                        socketServer[i].sendData("\nYou Won!");
                    }
                }
                if(players.get(i-1).checkBust() && dCards.getCardTotal() <= 21){
                    socketServer[i].sendData("\nYou Lose!");
                }
            }
        } catch (NullPointerException e){}
    }

    private class SocketServer implements Runnable {

        private ObjectOutputStream output;
        private ObjectInputStream input;
        private Socket connection;
        private int myConID;

        public SocketServer(int countIn) {
            myConID = countIn;
        }

        public void run() {
            try {
                try {
                    getStreams();
                    processConnection();
                } catch (EOFException e) {
                    displayMessage("\nServer " + myConID + " terminated connection");
                } finally {
                    closeConnection();
                }
            } catch (IOException e) {}
        }

        private void waitForConnection() throws IOException {
            displayMessage("Waiting for connection " + myConID + "\n");
            connection = server.accept();
            displayMessage("Connection " + myConID + " received from: " + connection.getInetAddress().getHostName() + "\n");
        }

        private void getStreams() throws IOException {
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            displayMessage("\nGot I/O streams\n");
        }

        private void processConnection() throws IOException {
            String message = "Connection " + myConID + " successful";
            sendData(message);
            if(!roundStarted) {
                roundStarted = true;
                displayMessage("\tTO DEAL CARDS PRESS - 'Y' OR PRESS - 'N' TO LEAVE GAME:\n");
                String step = scan.next();
                if (step.equals("y")) {
                    newDeck = new Deck();
                    dealCards();
                    displayMessage("\nCARDS DEALT\n");
                } else if (step.equals("n")) {
                    displayMessage("Bye Bye");
                    closeConnection();
                    System.exit(0);
                } else {
                    displayMessage("Not correct symbol, try again...\n");
                }
            }
            do {
                try {
                    if (message.contains("hit")) {
                        cardHit();
                    }
                    if (message.contains("stay")) {
                        this.sendData("Please Wait...");
                        playersLeft--;
                        checkDone();
                    }
                    message = (String) input.readObject();
                } catch (ClassNotFoundException e) {
                    displayMessage("\nUnknown object type received");
                }
            } while (!message.equals("CLIENT >>> TERMINATE"));
        }

        private void dealerGo() {
            dCards = new PlayGame(firstCard, secondCard);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (dCards.getCardTotal() < 16){
                while(dCards.getCardTotal() < 16){
                    String card1 = newDeck.dealCard();
                    dCards.cardHit(card1);
                    displayMessage("Dealer hits... " + card1 + "\n" + "Total:" + dCards.getCardTotal() + "\n");
                }
            }
            if(dCards.checkBust()){
                displayMessage("Dealer Busts!");
            } else {
                displayMessage("Dealer has" + " " + dCards.getCardTotal());
            }
            results();
        }

        private void cardHit() {
            String nextCard = newDeck.dealCard();
            sendData(nextCard);
            players.get(this.myConID -1).cardHit(nextCard);
            if(players.get(this.myConID -1).checkBust()) {
                sendData("Bust! Your Total: " + players.get(this.myConID -1).getCardTotal());
                playersLeft--;
                if(playersLeft == 0){
                    dealerGo();
                }
            }else{
                sendData("Select hit or stay? - (h/s) Your Total: " + players.get(this.myConID -1).getCardTotal());
            }
        }

        private void checkDone() {
            if(playersLeft == 0){
                dealerGo();
            }
        }

        private void closeConnection() {
            displayMessage("Terminating connection " + myConID + ". Player is left");
            try {
                output.close();
                input.close();
                connection.close();
                scan.close();
            } catch (IOException ioException) {}
        }

        private void sendData(String message) {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException ioException) {
                displayMessage("\nError writing object");
            }
        }
    }
}