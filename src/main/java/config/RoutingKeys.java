/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

/**
 *
 * @author joachim
 */
public class RoutingKeys {

    //Queue names
    public static final String GetBanksInput = "GetBanksInput";
    public static final String RecipientListInput = "RecipientListInput";
    public static final String TranslatorOne = "TranslatorOne";
    public static final String TranslatorTwo = "TranslatorTwo";
    public static final String TranslatorThree = "TranslatorThree";
    public static final String TranslatorFour = "TranslatorFour";
    public static final String NormulizerInput = "Normalizer";
    public static final String RecipientListToAggregator = "recipientlistToAggregator";
    public static final String NormalizerToAggregator = "normalizerToAggregator";
    public static final String Result = "result";

    //Routing keys  
    public static final String SCHOOL_JSON_BANK = "keyBankJSON";
    public static final String SCHOOL_XML_BANK = "keyBankXML";
    public static final String OUR_JSON_BANK = "ourRabbitBankKey";
    public static final String OUR_XML_BANK = "OurSoapXmlBank";
}
