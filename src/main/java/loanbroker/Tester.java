/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import com.google.gson.Gson;
import dto.DtoBank;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nikolai
 */
public class Tester {
    public static void main(String[] args) {
        
        try {
            List<String> resultFromRuleBaseService = getBanks(500);
            List<DtoBank> listOfBanks = resultFromRuleBaseService.isEmpty() ? null : convertFromJson(resultFromRuleBaseService);
            if(listOfBanks != null){
                for (DtoBank listOfBank : listOfBanks) {
                    System.out.println(listOfBank.toString());
                }
            }
            else
                System.out.println("array is empty!!! ---- main");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    private static List<String> getBanks(Integer creditScore) throws Exception {
            rule.RuleBase_Service service = new rule.RuleBase_Service();
            rule.RuleBase port = service.getRuleBasePort();
            java.util.List<java.lang.String> result = port.getBanks(creditScore);
         
            return result;
    }
    
    private static List<DtoBank> convertFromJson(List<String> jsonStrings){
        if(jsonStrings == null || jsonStrings.isEmpty())
            return null;
        
        Gson gson = new Gson();
        List<DtoBank> listOfDtoBanks = new ArrayList<DtoBank>();
        for (String jsonBank : jsonStrings) {
            listOfDtoBanks.add(gson.fromJson(jsonBank, DtoBank.class));            
        }
        return listOfDtoBanks;
    }
}