/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dto;

/**
 *
 * @author Jonathan
 */
public class DtoJsonBank {

    private String ssn;
    private int creditScore;
    private double loanAmount;
    private String loanDuration;

    public DtoJsonBank(String ssn, int creditScore, double loanAmount, String loanDuration) {
        this.ssn = ssn;
        this.creditScore = creditScore;
        this.loanAmount = loanAmount;
        this.loanDuration = loanDuration;
    }

    public DtoJsonBank() {
    }


}
