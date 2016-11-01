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
import entity.Message;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 *
 * @author Joachim
 */
public class Aggregator {
  private static final String hostName = "datdb.cphbusiness.dk";
  private static final String queueName = "fireBug"; 
  private  ArrayList<Message> messageList;
  private Message bestMessage;
   
  
  
 public void main() throws Exception {
		 
        Message m1 = new Message("1234567", 12212, 1222.0, 121);
        messageList.add(m1);
         
        System.out.println(findSmallestLoan(messageList).ssn);
  }
  

  
    private Message findSmallestLoan(ArrayList<Message> messages) throws Exception {
		
        bestMessage = new Message("", 0, 0, 0);
        
        messages.forEach((i) -> 
        {
            System.out.print(i.ssn);
            if(i.creditScore > bestMessage.creditScore)
            {
                bestMessage = i;
            }
            
        });
        messageList.removeAll(messageList);

	 
     return bestMessage;
    }

    private void addMessageToList(String ssn, int creditScore, double loanAmount, int loanDuration)
    { 
        Message m = new Message(ssn,creditScore,loanAmount,loanDuration);
        messageList.add(m); 
    }
        
    private void recive() throws IOException, TimeoutException, InterruptedException
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
            chan.queueDeclare(queueName, false, false, false, null);
	    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	    QueueingConsumer consumer = new QueueingConsumer(chan);
	    chan.basicConsume(queueName, true, consumer);
	    
            //start polling messages
	    while (true) {
	      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	      String m = new String(delivery.getBody());
              Gson gson = new GsonBuilder().create();
              Message fm = gson.fromJson(m, Message.class);
	      System.out.println(" [x] Received '" + fm + "'");
              addMessageToList(fm.ssn,fm.creditScore,fm.loanAmount,fm.loanDuration);
	    }
        
    
    }
    public void send(Message message) throws IOException, TimeoutException, Exception
    {
     ConnectionFactory connfac = new ConnectionFactory();
        connfac.setHost(hostName);
        connfac.setPort(5672);
        connfac.setUsername("student");
        connfac.setPassword("cph");
        Gson gson = new GsonBuilder().create();
        findSmallestLoan(messageList);
        String fm = gson.toJson(bestMessage);
        
        Connection connection = connfac.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(queueName, false, false, false, null);
          
        for (int i = 0; i < 3; i++) {
            channel.basicPublish("", queueName, null, fm.getBytes());
        }
        System.out.println(" [x] Sent '" + fm + "'");

        channel.close();
        connection.close();
    
    }
    
}
