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
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.io.IOException; 
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import config.RoutingKeys;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.scene.input.KeyCode.K;
/**
 *
 * @author Joachim
 */
public class Aggregator {
  private static final String hostName = "datdb.cphbusiness.dk"; 
  private Message bestMessage;
   Hashtable<String,Message> messagesFromBankList = new Hashtable<String,Message>(); 
   Hashtable<String,Message> messagesFromNormalizer = new Hashtable<String,Message>();  
   private static final String EXCHANGE_NAME = RoutingKeys.NormulizerInput;
   
  
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        ArrayList a = new ArrayList<Message>();
        Message m1 = new Message("1234567", 12212, 1222.0, 121);
        a.add(m1);
        Message m2 = new Message("1234568", 12212, 1222.0, 121);
        a.add(m2); 
        
        Aggregator ag = new Aggregator();
       // System.out.println(ag.findSmallestLoan(a));
        
        ag.reciveFromNormalizer();
        ag.reciveFromRecieptList();
        ag.send();
        int count = 1;
        while (count < 30) {
            System.out.println("Count is: " + count);
            count++;
        }
        
        
    }
   

  
    private Message findSmallestLoan(Hashtable<String,Message> MB,Hashtable<String,Message> MN) throws Exception {
		      System.out.println("FindSmallestLoan");
        bestMessage = new Message("", 0, 0, 0);
        
        
        Iterator<Map.Entry<String, Message>> it = MB.entrySet().iterator();

         while (it.hasNext()) {
         Map.Entry<String, Message> entry = it.next();
         
             System.out.println(entry.getKey());
             System.out.println(entry.getValue().toString());
         
        }
         return null;
            }
    /*
        messages.forEach((i) -> 
        {
            System.out.print(i.getSsn());
            if(i.getCreditScore() > bestMessage.getCreditScore())
            {
                bestMessage = i;
            }
          
        }); 
         */
	 
     
  
    private void send() throws IOException, TimeoutException, InterruptedException
    {
       
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostName);
        factory.setPort(5672);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare("TeamFirebug", "direct");

        String severity = "13"; 
        Gson gson = new GsonBuilder().create();
        bestMessage = new Message("sdfsdfsdaf", 2, 1, 20);
        String fm = gson.toJson(bestMessage);
        channel.basicPublish("TeamFirebug", severity, null, fm.getBytes());
       
        System.out.println(" [x] Sent '" + severity + "':'" + fm + "'");

        channel.close();
        connection.close();
        
    }
    public void reciveFromNormalizer() throws IOException, TimeoutException, Exception
    { 
         ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(hostName);
         factory.setPort(5672);
         factory.setUsername("student");
         factory.setPassword("cph");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare("TeamFirebug", "direct");
    String queueName = channel.queueDeclare().getQueue();
 
      channel.queueBind(queueName, "TeamFirebug","13"); 
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope,
        AMQP.BasicProperties properties, byte[] body) throws IOException {
        String m = new String(body, "UTF-8");
        String p = properties.getCorrelationId();
        Gson gson = new GsonBuilder().create();
        Message fm = gson.fromJson(m, Message.class);
        
        messagesFromNormalizer.put(p, fm); 
        System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + fm.toString() + "'");
      }
    };
    channel.basicConsume(queueName, true, consumer);
    }
    
    
    
    public void reciveFromRecieptList() throws IOException, TimeoutException, Exception
    { 
         ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(hostName);
         factory.setPort(5672);
         factory.setUsername("student");
         factory.setPassword("cph");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare("TeamFirebug", "direct");
    String queueName = channel.queueDeclare().getQueue();
 
      channel.queueBind(queueName, "TeamFirebug", "listToAggregator"); 
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope,
                                 AMQP.BasicProperties properties, byte[] body) throws IOException {
        String m = new String(body, "UTF-8");
        String p = properties.getCorrelationId();
           
              Gson gson = new GsonBuilder().create();
              Message fm = gson.fromJson(m, Message.class);
        messagesFromBankList.put(p, fm);
        System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + fm.toString() + "'");
      }
    };
    channel.basicConsume(queueName, true, consumer);
    }
    
}

