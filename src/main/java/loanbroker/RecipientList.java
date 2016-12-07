/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import com.google.gson.Gson;
import entity.Bank;

import com.rabbitmq.client.*;
import java.io.IOException;

import com.rabbitmq.client.*;
import config.RabbitConnection;
import config.RoutingKeys;
import entity.Message;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author nikolai
 */
public class RecipientList {

    //Rabbit mq make function send to all banks to their channel (direct)
    public static void main(String[] args) throws Exception {
        //make correlationId for Aggregator
        final String corrId = java.util.UUID.randomUUID().toString();
        //rabbit connect
        final String exchangeName = "TeamFirebug";
        
        RabbitConnection rabbitConnection = new RabbitConnection();
        
        Channel channel = rabbitConnection.makeConnection();
        channel.exchangeDeclare(exchangeName, "direct");
        
        String queueName = channel.queueDeclare().getQueue();
        
        //routingKey = RoutingKeys.RecipientListInput
        channel.queueBind(queueName, exchangeName, RoutingKeys.RecipientListInput);
         
        //get banks from queue. "Get banks" component
        QueueingConsumer consumer = new QueueingConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                //send to Aggregator first
                sendMessage(exchangeName,RoutingKeys.RecipientListToAggregator,message, corrId);
                
                //send to translator
                Message request = getFromJson(message);
                if (request.getBanks() != null) {
                    for (Bank bank : request.getBanks()) {
                        sendMessage(exchangeName,bank.getRoutingKey(), message, corrId);
                        //remember that the component "Get banks" must choose which banks we need to send to(according to credit score)
                    }
                }
            }
        };
        channel.basicConsume(queueName, true, consumer);

    }

    private static Message getFromJson(String json) {
        Gson g = new Gson();
        return g.fromJson(json, Message.class);
    }

    //can be used for sending message to Translator and Aggregator
    private static void sendMessage(String exchangeName,String routingKey, String msg, String corrId) {
        RabbitConnection rabbitConnection = new RabbitConnection();
        Channel channel = rabbitConnection.makeConnection();
        try {
            //set correlationId for Aggregator
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .correlationId(corrId)
                    .build();

            channel.exchangeDeclare(exchangeName, "direct");
            channel.basicPublish(exchangeName, routingKey, null, msg.getBytes("UTF-8"));
            rabbitConnection.closeChannelAndConnection();
            System.out.println(" [x] Sent :" + msg + "");
        } catch (IOException ex) {
            System.out.println("Error in RecipientList class - sendToTranslator()");
            System.out.println(ex.getStackTrace());
        }

    }
    

}