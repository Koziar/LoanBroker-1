/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import config.ExchangeName;
import config.RoutingKeys;
import entity.LoanResponse;
import entity.Message;
import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Joachim
 */
public class Result {
  private static final String hostName = "datdb.cphbusiness.dk"; 
 
    /**
     * @param args the command line arguments
     */
     public static void main(String[] args) throws TimeoutException, Exception {
        // TODO code application logic here
      Result r = new Result(); 
         r.reciveResultFromAggregator();
        
    } 
    public void reciveResultFromAggregator() throws IOException, TimeoutException, Exception
    { 
         ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(hostName);
         factory.setPort(5672);
         factory.setUsername("student");
         factory.setPassword("cph");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
 
      String queueName = channel.queueDeclare().getQueue(); 
      channel.queueBind(queueName, ExchangeName.GLOBAL, RoutingKeys.Result); 
      System.out.println(" [*] Waiting for messages on "+ ExchangeName.GLOBAL + RoutingKeys.Result + ".. To exit press CTRL+C");
    
    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope,
                                 AMQP.BasicProperties properties, byte[] body) throws IOException {
        String m = new String(body, "UTF-8");
        //  System.out.println("reciveFromRecieptList"+ m);
        String p = properties.getCorrelationId();
           
              Gson gson = new GsonBuilder().create();
              LoanResponse fm = gson.fromJson(m, LoanResponse.class);
       
              
       // System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + fm.toString() + "'");
      //   System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + m + "'");
         System.out.println("**** [info]**** You're best interestrate is '" + fm.getInterestRate() + "' on the CPR number. '" + fm.getSsn() + "' at '" + fm.getBank() + "'");
      }
    };
    channel.basicConsume(queueName, true, consumer);
    }  
   
}
