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
import entity.Message;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author nikolai
 */
public class RecipientList {

    //Rabbit mq make function send to all banks to their channel (direct)
    public static void main(String[] args) throws Exception {
        
        RabbitConnection rabbitConnection = new RabbitConnection();

        
        //rabbit connect
        String exchangeName = "Banks";
        //Channel channel = connectToChannel("localhost", "", "");
        final Channel channel = rabbitConnection.makeConnection();
        channel.exchangeDeclare(exchangeName, "direct");

        String queueName = channel.queueDeclare().getQueue();
        //banks = routingKey
        channel.queueBind(queueName, exchangeName, "banksQueue");

        //get banks from queue. "Get banks" component
        QueueingConsumer consumer = new QueueingConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                Message request = getFromJson(message);
                if (request.getBanks() != null) {
                    for (Bank bank : request.getBanks()) {
                        //remember that the component "Get banks" must choose which banks we need to send to(according to credit score)
                        //send to translator queue here, make send method
                        //sendToTranslator(channel, bank.getRoutingKey(), "Banks", message);
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

    private static void sendToTranslator(Channel channel, String routingKey, String exchangeName, String msg) {
        try {
            channel.exchangeDeclare(exchangeName, "direct");
            channel.basicPublish(exchangeName, routingKey, null, msg.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + routingKey + "':'" + msg + "'");
        } catch (IOException ex) {
            System.out.println("Error in RecipientList class - sendToTranslator()");
            System.out.println(ex.getStackTrace());
        }

    }

}