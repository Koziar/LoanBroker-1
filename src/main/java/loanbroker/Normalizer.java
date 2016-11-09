/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import entity.LoanResponse;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import jdk.internal.org.xml.sax.InputSource;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Mathias
 */
public class Normalizer {

    private static final String EXCHANGE_NAME = "normalizerInput";

    public static void main(String[] argv) throws IOException, InterruptedException, TimeoutException {
       //Connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //Consumer
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "info");
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        //Producer
        Channel channelOutput = connection.createChannel();
	String queue= "fisk";
	channelOutput.queueDeclare(queue, false, false, false, null);
        
       
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            String routingKey = delivery.getEnvelope().getRoutingKey();
      // if json
      /*
             JSONObject jsonObj = new JSONObject(message);
             System.out.println("ssn: "+jsonObj.getInt("ssn"));
             System.out.println("rate : "+jsonObj.getInt("interestRate"));

              

             System.out.println(" [x] Received '" + routingKey + "':'" + message + "'");
             LoanResponse loanResponse = new LoanResponse(jsonObj.getInt("ssn"),jsonObj.getInt("interestRate"), routingKey);
         */    
        //if xml
            LoanResponse loanResponse = JAXB.unmarshal(new StringReader(message), LoanResponse.class);
            loanResponse.setBank(routingKey);

            System.out.println("renter: " + loanResponse.getInterestRate());
            System.out.println("ssn: " + loanResponse.getSsn());
            System.out.println("bank : " + loanResponse.getBank());
            JSONObject jsonObj = new JSONObject(loanResponse);
            channelOutput.basicPublish("", "fisk", null,jsonObj.toString().getBytes());
              

        }
    }

}