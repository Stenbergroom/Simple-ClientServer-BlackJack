package com.stenbergroom.blackjack;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Player {

    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message = "";
    private String chatServer;
    private Socket client;
    private Scanner scan = new Scanner(System.in);

    public Player(String host) {
        chatServer = host;
        displayMessage("\n\t\t*****Welcome to Blackjack game!*****\n");
    }

    public void runClient() {
        try {
            connectToServer(); // create a Socket to make connection
            getStreams(); // get the input and output streams
            processConnection(); // process connection
        } catch (EOFException e) {
            displayMessage("\nClient terminated connection");
        } catch (IOException e) {
            //
        } finally {
            closeConnection();
        }
    }

    private void connectToServer() throws IOException {
        displayMessage("Attempting connection\n");
        client = new Socket(InetAddress.getByName(chatServer), 1992);
        displayMessage("Connected to: " + client.getInetAddress().getHostName() );
    }

    private void getStreams() throws IOException {
        output = new ObjectOutputStream(client.getOutputStream());
        output.flush();
        input = new ObjectInputStream(client.getInputStream());
        displayMessage("\nGot I/O streams\n");
    }

    private void processConnection() throws IOException {
        do {
            try {
                message = (String) input.readObject(); // read new message
                displayMessage("\n" + message); // display message
                if(message.contains("Select hit or stay? - (h/s)")){
                    while (true) {
                        String step = scan.next();
                        //send message to Server
                        if (step.equals("h")) {
                            sendData("hit");
                            break;
                        } else if (step.equals("s")) {
                            sendData("stay");
                            break;
                        } else {
                            displayMessage("Not correct symbol, try again...\n");
                        }
                    }
                }
                if (message.contains("Bust!") || message.contains("Please Wait")) {
                    //
                }

                if (message.contains("Connect failed")) {
                    displayMessage("Connect failed");
                }
            } catch (ClassNotFoundException e) {
                displayMessage("\nUnknown object type received");
            }
        } while (!message.equals("SERVER >>> TERMINATE"));
    }

    private void closeConnection() {
        displayMessage("\nClosing connection");
        try {
            output.close();
            input.close();
            client.close();
            scan.close();
        } catch (IOException e) {}
    }

    private void sendData(String message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            displayMessage("\nError writing object");
        }
    }

    private void displayMessage(final String messageToDisplay) {
        System.out.println(messageToDisplay);
    }
}