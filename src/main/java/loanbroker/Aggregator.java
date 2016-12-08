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
import config.ExchangeName;
import config.RoutingKeys;
import entity.Bank;
import entity.LoanResponse;
import static java.lang.Integer.parseInt;
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
    public static void main(String[] args) throws Exception {
        // TODO code application logic here

        Hashtable<String, Message> messagesFromBankList = new Hashtable<String, Message>();
        Hashtable<String, Message> messagesFromNormalizer = new Hashtable<String, Message>();
        ArrayList<Message> foundMessages = new ArrayList<Message>();
 
        Aggregator ag = new Aggregator();

        //Costumer 1
        ag.reciveFromNormalizer(messagesFromBankList, messagesFromNormalizer, foundMessages);
        ag.reciveFromRecieptList(messagesFromBankList);
       
    }

    private void checkLoanMessages(Hashtable<String, Message> messageBank, Hashtable<String, Message> mn, ArrayList<Message> fm) throws IOException, InterruptedException, TimeoutException, Exception {
        System.out.println("CheckLoan");

        Iterator<Map.Entry<String, Message>> mb = messageBank.entrySet().iterator();
        String key = "";
        while (mb.hasNext()) {
            Map.Entry<String, Message> entryMB = mb.next();
            if (mn.get(entryMB.getKey()) != null) {
                mn.get(entryMB.getKey()).setBanks(entryMB.getValue().getBanks());
                fm.add(mn.get(entryMB.getKey()));
                mn.remove(mn.get(entryMB.getKey()));  
            }
        }
        
         for (int k = 0; k < fm.size(); k++) {
             System.out.println("LOAN:"+fm.get(k));
             
         }
       
        
         findBest(fm,messageBank,key);

    }

    private void findBest(ArrayList<Message> messages, Hashtable<String, Message> messageBank, String key) throws Exception {
        System.out.println("findBest");

        System.out.println("Loops through the messsages in the message array. ");
        for (int i = 0; i < messages.size(); i++) {
            Message y = messages.get(i);
            

            ArrayList<Message> finalMessages = new ArrayList<Message>();
            for (int k = 0; k < messages.size(); k++) {

                System.out.println("Check if message contains SSN number");
                if (messages.get(k).getSsn().contains(y.getSsn())) {
                    //Do whatever you want here

                    finalMessages.add(messages.get(k));

                }
                Message bestMessage = new Message("", 0, 0, "");
                System.out.println("Checking if all messages from the banks are recived.");
                System.out.println(""+finalMessages.size()+" - "+y.getBanks().size()+"");
                if (finalMessages.size() == y.getBanks().size()) {
                    System.out.println("Finding the best creditscore");
                    for (int o = 0; o < finalMessages.size(); o++) {

                        if (finalMessages.get(o).getCreditScore() > bestMessage.getCreditScore()) {
                            bestMessage = finalMessages.get(o);
                        }
                    }
                } else {
                    System.out.println("Missing some messages.");
                }
                System.out.println("Checks if SSN number exists");
                if (bestMessage.getSsn() != "") {
                    System.out.println("Sending the message thought Rabbitmq.");
                    send(bestMessage, RoutingKeys.Result);
                    finalMessages.removeAll(messages);
                    messages.removeAll(messages);
                    messageBank.remove(key);
                    
                    bestMessage.setSsn("");

                } else {
                    System.out.println("NOTHING SENT");

                }
            }

        }
      

    }

    private void send(entity.Message m, String routingKey) throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostName);
        factory.setPort(5672);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(ExchangeName.GLOBAL, "direct");

        
        //creating LoanResponse object
        LoanResponse lp = new LoanResponse(parseInt(m.getSsn()), m.getCreditScore(),m.getLoanDuration(), "");
        
        Gson gson = new GsonBuilder().create();
        String fm = gson.toJson(lp); 
        BasicProperties props = new BasicProperties.Builder()
                .build();

        channel.basicPublish(ExchangeName.GLOBAL, routingKey, props, fm.getBytes());

        System.out.println(" [x] Sent '" + ExchangeName.GLOBAL + routingKey + "':'" + fm + "'");

        channel.close();
        connection.close();

    }

    public void reciveFromNormalizer(Hashtable<String, Message> messageFroumBankList, Hashtable<String, Message> messagesFromNormalizer, ArrayList<Message> foundMessages) throws IOException, TimeoutException, Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostName);
        factory.setPort(5672);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // channel.exchangeDeclare(inputEXCHANGE_NAME, "direct");
        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, ExchangeName.GLOBAL, RoutingKeys.NormalizerToAggregator);
        System.out.println(" [*] Waiting for messages on " + ExchangeName.GLOBAL + RoutingKeys.NormalizerToAggregator + ". To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                    AMQP.BasicProperties properties, byte[] body) throws IOException {
                String m = new String(body, "UTF-8"); 
                
                Gson gson = new GsonBuilder().create();
                LoanResponse lp = gson.fromJson(m, LoanResponse.class); 
                Message fm = new Message(""+lp.getSsn(), (int) lp.getInterestRate(), 0, lp.getBank()); 
                messagesFromNormalizer.put(lp.getCorrelationId(), fm);

                System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + fm.toString() + "'");
                 try {
                    checkLoanMessages(messageFroumBankList, messagesFromNormalizer, foundMessages);
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

    public void reciveFromRecieptList(Hashtable<String, Message> messagesFromBankList) throws IOException, TimeoutException, Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostName);
        factory.setPort(5672);
        factory.setUsername("student");
        factory.setPassword("cph");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(ExchangeName.GLOBAL, "direct");
        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, ExchangeName.GLOBAL, RoutingKeys.RecipientListToAggregator);
        System.out.println(" [*] Waiting for messages on " + ExchangeName.GLOBAL + RoutingKeys.RecipientListToAggregator + ".. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            public void handleDelivery(String consumerTag, Envelope envelope,
                    AMQP.BasicProperties properties, byte[] body) throws IOException {
                String m = new String(body, "UTF-8");
               // System.out.println("reciveFromRecieptList" + m);
                String p = properties.getCorrelationId();
                if(p != null){
                //send to translator
                 Gson g = new Gson(); 
                Message fm = g.fromJson(m, Message.class);
                if (fm.getBanks() != null) {
                    Message k = new Message(fm.getSsn(), fm.getCreditScore(), fm.getLoanAmount(), fm.getLoanDuration());
                    
                    k.setBanks(fm.getBanks());
                    
                    messagesFromBankList.put(p, k);
                }

                System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + fm.toString() + "'");
                //   System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + m + "'");
                 }
                else
                   {
                      System.out.println("No correlationId");
                  }    
                    
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }

}
