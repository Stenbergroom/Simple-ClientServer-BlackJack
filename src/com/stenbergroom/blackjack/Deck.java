package com.stenbergroom.blackjack;

import java.util.Random;

public class Deck{

    private Cards[] deck;
    private int count;
    private static final int NUMBER_OF_CARDS = 52;
    private static final Random rand = new Random();

    public Deck(){
        String[] faces = {"Ace","2","3","4","5","6","7","8","9","10","Jack","Queen","King"};
        deck = new Cards[NUMBER_OF_CARDS];
        count = 0;
        for(int i = 0; i<deck.length; i++){
            deck[i] = new Cards(faces[i%13]);
        }
    }

    public void shuffle(){
        count = 0;
        for(int i = 0; i < deck.length; i++){
            int random = rand.nextInt(NUMBER_OF_CARDS);
            Cards t = deck[i];
            deck[i] = deck[random];
            deck[random] = t;
        }
    }

    public String dealCard(){
        if(count < deck.length){
            return deck[count++].toString();
        } else {
            return null;
        }
    }
}
