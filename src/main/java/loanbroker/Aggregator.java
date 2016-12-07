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
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import config.RoutingKeys;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map; 
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Joachim
 */
public class Aggregator {
  private static final String hostName = "datdb.cphbusiness.dk"; 
 
    
//   private static final String EXCHANGE_NAME = RoutingKeys.NormulizerInput; 
   private static final String inputEXCHANGE_NAME = "TeamFirebug";
   private static final String outPutEXCHANGE_NAME = "TeamFirebug";
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
         Hashtable<String,Message> messagesFromBankList = new Hashtable<String,Message>(); 
         Hashtable<String,Message> messagesFromNormalizer = new Hashtable<String,Message>();  
         ArrayList<Message> foundMessages = new ArrayList<Message>();
        
         //Costumer 1
         Message m1 = new Message("1234567", 12212, 1222.0, 121); 
         Message m2 = new Message("1234567", 122213123, 1221111.0, 151);
         Message m3 = new Message("1234567", 123, 1221111.0, 151);
          
         
         //Costumer 2 
         Message c1 = new Message("1234564", 12212, 1222.0, 121); 
         Message c2 = new Message("1234564", 122213123, 1221111.0, 151);
         Message c3 = new Message("1234564", 3123, 1221111.0, 151);
        
        Aggregator ag = new Aggregator(); 
        
        //Costumer 1
        ag.reciveFromNormalizer(messagesFromBankList,messagesFromNormalizer,foundMessages);
        ag.reciveFromRecieptList(messagesFromBankList);
    /*    ag.send(m1,RoutingKeys.Aggregator);   
        ag.send(m1,RoutingKeys.Aggregator);  
        ag.send(m2,RoutingKeys.Aggregator);   
        ag.send(m2,RoutingKeys.Aggregator);
        ag.send(m3,RoutingKeys.Aggregator);   
        ag.send(m3,RoutingKeys.Aggregator);
         
         //Costumer 2
         ag.send(c1,RoutingKeys.Aggregator);   
        ag.send(c1,RoutingKeys.Aggregator);  
        ag.send(c2,RoutingKeys.Aggregator);   
        ag.send(c2,RoutingKeys.Aggregator);
        ag.send(c3,RoutingKeys.Aggregator);   
        ag.send(c3,RoutingKeys.Aggregator);
       */ 
    }
      private void checkLoanMessages(Hashtable<String, Message> messageBank,Hashtable<String, Message> mn,ArrayList<Message> fm) throws IOException, InterruptedException, TimeoutException, Exception {
         System.out.println("CheckLoan");
         
        Iterator<Map.Entry<String, Message>> mb = messageBank.entrySet().iterator();    
        
         while (mb.hasNext()) 
         {
         Map.Entry<String, Message> entryMB = mb.next(); 
             if(mn.get(entryMB.getKey()) != null)
             {  
                fm.add(mn.get(entryMB.getKey()));
                mn.remove(mn.get(entryMB.getKey()));
                messageBank.remove(mn.get(entryMB.getKey())); 
             }
        }
               
         findBest(fm); 
          
    }
   
     private void findBest(ArrayList<Message> messages) throws Exception { 
         System.out.println("findBest"); 
          
         System.out.println("Loops through the messsages in the message array. ");
         for (int i = 0; i < messages.size(); i++) 
        {    
             Message y = messages.get(i); 
             
             ArrayList<Message> finalMessages = new ArrayList<Message>(); 
            for (int k = 0; k < messages.size(); k++) {
               
                 System.out.println("Check if message contains SSN number");
                if (messages.get(k).getSsn().contains(y.getSsn())) 
                { 
                //Do whatever you want here
                
                finalMessages.add(messages.get(k));
                
                }
                 Message bestMessage = new Message("", 0, 0, 0);
                  System.out.println("Checking if all messages from the banks are recived.");
                if(finalMessages.size() == 3)
                { 
                     System.out.println("Finding the best creditscore");
                     for (int o = 0; o < finalMessages.size(); o++) {
                    
                          if(finalMessages.get(o).getCreditScore() > bestMessage.getCreditScore())
                              {
                                  bestMessage  = finalMessages.get(o);
                              } 
                     } 
                } 
                else
                 {     
                  System.out.println("Missing some messages.");
                 }
                System.out.println("Checks if SSN number exists");
                if(bestMessage.getSsn() != ""){ 
                     System.out.println("Sending the message thought Rabbitmq.");
                     send(bestMessage,"AggregatorToResult");
                      finalMessages.remove(bestMessage);
                      messages.remove(bestMessage);
                      
                      } 
                else
                    {
                        System.out.println("NOTHING SENT");
                       
                    }
             } 
             
        }
         
         
    }  
  
    private void send(entity.Message m,String routingKey) throws IOException, TimeoutException, InterruptedException
    { 
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostName);
        factory.setPort(5672);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(outPutEXCHANGE_NAME, "direct");
 
        Gson gson = new GsonBuilder().create();
        String fm = gson.toJson(m);
        String corrId = java.util.UUID.randomUUID().toString();
            BasicProperties props = new BasicProperties.Builder()
                    .correlationId("test1122") 
                    .build();
                    
        channel.basicPublish(outPutEXCHANGE_NAME, routingKey, props, fm.getBytes());
       
        System.out.println(" [x] Sent '"+ outPutEXCHANGE_NAME + routingKey + "':'" + fm + "'");

        channel.close();
        connection.close();
        
    }
    public void reciveFromNormalizer(Hashtable<String, Message> messageFroumBankList, Hashtable<String, Message> messagesFromNormalizer, ArrayList<Message> foundMessages) throws IOException, TimeoutException, Exception
    { 
         ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(hostName);
         factory.setPort(5672);
         factory.setUsername("student");
         factory.setPassword("cph");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

   // channel.exchangeDeclare(inputEXCHANGE_NAME, "direct");
    String queueName = channel.queueDeclare().getQueue();
 
      channel.queueBind(queueName, inputEXCHANGE_NAME, RoutingKeys.NormalizerToAggregator); 
    System.out.println(" [*] Waiting for messages on "+ inputEXCHANGE_NAME + RoutingKeys.NormalizerToAggregator + ". To exit press CTRL+C");

    Consumer consumer = new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope,
        AMQP.BasicProperties properties, byte[] body) throws IOException {
        String m = new String(body, "UTF-8");
            System.out.println("reciveFromNormalizer"+ m);
        String p = properties.getCorrelationId();
        Gson gson = new GsonBuilder().create();
        Message fm = gson.fromJson(m, Message.class);
         
         messagesFromNormalizer.put(p, fm); 
        
        System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + fm.toString() + "'");
          try {
              checkLoanMessages(messageFroumBankList,messagesFromNormalizer,foundMessages);
          } catch (InterruptedException ex) {
              Logger.getLogger(Aggregator.class.getName()).log(Level.SEVERE, null, ex);
          } catch (TimeoutException ex) {
              Logger.getLogger(Aggregator.class.getName()).log(Level.SEVERE, null, ex);
          } catch (Exception ex) {
              Logger.getLogger(Aggregator.class.getName()).log(Level.SEVERE, null, ex);
          }
      }
    };
    channel.basicConsume(queueName, true, consumer);
    }
    
    
    
    public void reciveFromRecieptList(Hashtable<String, Message> messagesFromBankList) throws IOException, TimeoutException, Exception
    { 
         ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(hostName);
         factory.setPort(5672);
         factory.setUsername("student");
         factory.setPassword("cph");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

   // channel.exchangeDeclare(inputEXCHANGE_NAME, "direct");
    String queueName = channel.queueDeclare().getQueue();
 
      channel.queueBind(queueName, inputEXCHANGE_NAME, RoutingKeys.RecipientListToAggregator); 
    System.out.println(" [*] Waiting for messages on "+ inputEXCHANGE_NAME + RoutingKeys.RecipientListToAggregator + ".. To exit press CTRL+C");

    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope,
                                 AMQP.BasicProperties properties, byte[] body) throws IOException {
        String m = new String(body, "UTF-8");
          System.out.println("reciveFromRecieptList"+ m);
        String p = properties.getCorrelationId();
           
              Gson gson = new GsonBuilder().create();
              Message fm = gson.fromJson(m, Message.class);
      
              messagesFromBankList.put(p, fm); 
              
      //  System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + fm.toString() + "'");
         System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + m + "'");
        
      }
    };
    channel.basicConsume(queueName, true, consumer);
    } 

  
}