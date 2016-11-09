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

    private String id;
    private int minimumCreditScore;
    private String name;
    private String routingKey;

    public Bank(String id, int minimumCreditScore, String name, String routingKey) {
        this.id = id;
        this.minimumCreditScore = minimumCreditScore;
        this.name = name;
        this.routingKey = routingKey;
    }

    public String getId() {
        return id;
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
