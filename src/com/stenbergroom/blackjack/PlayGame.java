package com.stenbergroom.blackjack;

import java.util.ArrayList;

public class PlayGame {

    private boolean bust = false;
    private int cardTotal = 0;
    private ArrayList<String> cards;
    private ArrayList<String> aces;

    public PlayGame(String card1, String card2) {
        cards = new ArrayList();
        aces = new ArrayList();
        if (card1.equals("Ace")) {
            aces.add(card1);
        } else {
            cards.add(card1);
        }
        if (card2.equals("Ace")) {
            aces.add(card2);
        } else {
            cards.add(card2);
        }

        setTotal();
    }

    public int getCardTotal() {
        return cardTotal;
    }

    public void cardHit(String card){
        if (card.equals("Ace")) {
            aces.add("Ace");
        } else {
            cards.add(card);
        }
        if(aces.size() != 0){
            setTotal();
        } else if (card.equals("Jack") || card.equals("Queen") || card.equals("King")){
            cardTotal += 10;
        } else {
            cardTotal += Integer.parseInt(card);
        }

        checkBust();
    }

    private void setTotal() {
        cardTotal = 0;
        for(String card : cards){
            if (card.equals("Jack") || card.equals("Queen") || card.equals("King")){
                cardTotal += 10;
            } else {
                cardTotal += Integer.parseInt(card);
            }
        }
        for(String a : aces){
            if (cardTotal <= 10){
                cardTotal += 11;
            } else {
                cardTotal += 1;
            }
        }
    }

    public boolean checkBust(){
        if(cardTotal > 21){
            bust = true;
        } else {
            bust = false;
        }
        return bust;
    }
}
