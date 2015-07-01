package com.stenbergroom.blackjack;

public class Cards {

    private String face;

    public Cards(String cardValue){
        face = cardValue;
    }

    public String toString(){
        return face;
    }
}
