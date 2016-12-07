/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import config.ExchangeName;
import config.RoutingKeys;
import entity.Message;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Joachim
 */
public class GetCreditScore {
  private static final String hostName = "datdb.cphbusiness.dk"; 
  public int creditScore = 0;
  
    /**
     * @param args the command line arguments
     */
     public static void main(String[] args) throws TimeoutException, Exception {
        // TODO code application logic here
      GetCreditScore g = new GetCreditScore(); 
         g.recive();
        
    }

     private static int creditScore(java.lang.String ssn) {
        org.bank.credit.web.service.CreditScoreService_Service service = new org.bank.credit.web.service.CreditScoreService_Service();
        org.bank.credit.web.service.CreditScoreService port = service.getCreditScoreServicePort();
        return port.creditScore(ssn);
    }
     private void recive() throws IOException, TimeoutException, InterruptedException, Exception
    {
            //setting the connection to the RabbitMQ server
            ConnectionFactory connfac = new ConnectionFactory();
            connfac.setHost(hostName);
            connfac.setUsername("student");
            connfac.setPassword("cph");
            
            //make the connection
            Connection conn = connfac.newConnection();
            //make the channel for messaging
            Channel chan = conn.createChannel();
          
            //Declare a queue
            chan.queueDeclare(ExchangeName.OUTPUT_LOAN_REQUEST, false, false, false, null);
	    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	    QueueingConsumer consumer = new QueueingConsumer(chan);
	    chan.basicConsume(ExchangeName.OUTPUT_LOAN_REQUEST, true, consumer);
	    
            //start polling messages
	    while (true) {
	      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	      String m = new String(delivery.getBody());
	      System.out.println(" [x] Received '" + m + "'");
              int creditScore  = creditScore(m);
                 send(creditScore);
              
	    }
        
    
    } 
    public void send(int creditScore) throws IOException, TimeoutException, Exception
    {
     ConnectionFactory connfac = new ConnectionFactory();
        connfac.setHost(hostName);
        connfac.setPort(5672);
        connfac.setUsername("student");
        connfac.setPassword("cph");
        Gson gson = new GsonBuilder().create();
        
        String fm = gson.toJson(creditScore);
        
        Connection connection = connfac.newConnection();
        Channel channel = connection.createChannel(); 
        channel.exchangeDeclare(ExchangeName.OUTPUT_GET_CREDITSCORE, "fanout");
        channel.basicPublish(ExchangeName.OUTPUT_GET_CREDITSCORE,"" , null, fm.getBytes()); 
        
        System.out.println(" [x] Sent '" + fm.toString() + "'");

        channel.close();
        connection.close();
    
    } 
}
