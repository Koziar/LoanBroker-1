/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

/**
 *
 * @author Jonathan
 */
public class LoanResponse {
    
   int ssn;
   double interestRate;
   String bank;


    public LoanResponse(int ssn, double interestRate, String bank) {
        this.ssn = ssn;
        this.interestRate = interestRate;
        this.bank = bank; 

    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }


    public LoanResponse(){
        
    }
    

    public int getSsn() {
        return ssn;
    }

    public void setSsn(int ssn) {
        this.ssn = ssn;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    
}