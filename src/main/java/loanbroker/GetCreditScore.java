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
     void recive() throws IOException, TimeoutException, InterruptedException, Exception
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
            chan.exchangeDeclare(ExchangeName.OUTPUT_LOAN_REQUEST, "fanout");
            String queueName = chan.queueDeclare().getQueue();
            chan.queueBind(queueName,ExchangeName.OUTPUT_LOAN_REQUEST,"");
            
	    System.out.println(" [*] Waiting for messages on "+ ExchangeName.OUTPUT_LOAN_REQUEST +". To exit press CTRL+C");
            
	    QueueingConsumer consumer = new QueueingConsumer(chan);
	    chan.basicConsume(queueName, true, consumer);
	    
            //start polling messages
	    while (true) {
	      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	      String m = new String(delivery.getBody());
	      System.out.println(" [x] Received '" + m + "'");
               Gson gson = new GsonBuilder().create();
                Message fm = gson.fromJson(m, Message.class);
              int creditScore  = creditScore(fm.getSsn());
                fm.setCreditScore(800);
                fm.setSsn(fm.getSsn().replace("-", ""));
                send(fm);
                 
              
	    }
        
    
    } 
    public void send(entity.Message creditScoreMessage) throws IOException, TimeoutException, Exception
    {
     ConnectionFactory connfac = new ConnectionFactory();
        connfac.setHost(hostName);
        connfac.setPort(5672);
        connfac.setUsername("student");
        connfac.setPassword("cph");
        Gson gson = new GsonBuilder().create();
        
        String fm = gson.toJson(creditScoreMessage);
        
        Connection connection = connfac.newConnection();
        Channel channel = connection.createChannel(); 
        channel.exchangeDeclare(ExchangeName.OUTPUT_GET_CREDITSCORE, "fanout");
        channel.basicPublish(ExchangeName.OUTPUT_GET_CREDITSCORE,"" , null, fm.getBytes()); 
        
        System.out.println(" [x] Sent '" + fm.toString() + "'");

        channel.close();
        connection.close();
    
    } 
}
