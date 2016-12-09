/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bank;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import config.ExchangeName;
import config.RoutingKeys;
import entity.LoanResponse;
import entity.Message;
import java.util.Random;

/**
 *
 * @author Mathias
 */
public class OurRabbitBank {

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, ExchangeName.GLOBAL, RoutingKeys.OUR_JSON_BANK);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            AMQP.BasicProperties properties = delivery.getProperties();
            String message = new String(delivery.getBody());

            Gson g = new Gson();
            
            Message msg = g.fromJson(message, Message.class);
            
            System.out.println(" [x] Received '" + message + "'");

            sendToNormalizer(msg, properties);
        }
    }

    private static void sendToNormalizer(Message msg, AMQP.BasicProperties props) {
        try {
            Gson g = new Gson();
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("datdb.cphbusiness.dk");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            //channel.exchangeDeclare(ExchangeName.OUR_JSON_BANK_RESPONSE, "direct");

            int ssn = Integer.valueOf(msg.getSsn());
            double interestRate = calcRate();
            String bank = "OurRabbitBank";
            String correlationId = props.getCorrelationId();

            LoanResponse response = new LoanResponse(ssn, interestRate, bank, correlationId);
            
            String res = g.toJson(response);

            channel.basicPublish(ExchangeName.OUR_JSON_BANK_RESPONSE, "", props, res.getBytes());

            System.out.println(" [x] Sent '" + res + "'");
            
            channel.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Error in OutRabbitBank: " + e.getMessage());
        }
    }

    private static double calcRate() {
        double rangeMin = 1.5;
        double rangeMax = 7.3;
        Random r = new Random();
        return rangeMin + (rangeMax - rangeMin) * r.nextDouble();
    }

}
