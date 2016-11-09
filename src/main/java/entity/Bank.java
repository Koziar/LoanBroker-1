/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

/**
 *
 * @author Mathias
 */
public class Bank {

    private int minimumCreditScore;
    private String name;
    private String routingKey;

    public Bank(int minimumCreditScore, String name, String routingKey) {
        this.minimumCreditScore = minimumCreditScore;
        this.name = name;
        this.routingKey = routingKey;
    }

    public int getMinimumCreditScore() {
        return minimumCreditScore;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getName() {
        return name;
    }

}
