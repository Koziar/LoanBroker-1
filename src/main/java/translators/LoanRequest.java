/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package translators;

/**
 *
 * @author nikolai
 */

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoanRequest {
    private String ssn;
    private int creditScore;
    private double loanAmount;
    private String loanDuration;

    public String getSsn() {
        return ssn;
    }
    
    @XmlElement
    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public int getCreditScore() {
        return creditScore;
    }
    
    @XmlElement
    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    public double getLoanAmount() {
        return loanAmount;
    }
    
    @XmlElement
    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getLoanDuration() {
        return loanDuration;
    }
    
    @XmlElement
    public void setLoanDuration(String loanDuration) {
        this.loanDuration = loanDuration;
    }
    
    
}
