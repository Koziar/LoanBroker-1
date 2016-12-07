/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker.normalizer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import config.RoutingKeys;
import dto.DtoTeachersXmlBank;
import entity.LoanResponse;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.TimeoutException;
import javax.xml.bind.JAXB;
import org.json.JSONObject;

/**
 *
 * @author Jonathan
 */
public class NormalizerTeachersJsonBank {

    private static final String EXCHANGE_NAME = "cphbusiness.bankJSON";
    private static final String RPC_QUEUE_NAME = "teachersJsonReply";

    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("datdb.cphbusiness.dk");
            factory.setPort(5672);
            factory.setUsername("student");
            factory.setPassword("cph");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            //String queueName = channel.queueDeclare().getQueue();
            //channel.queueBind(queueName, EXCHANGE_NAME, "");   
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(RPC_QUEUE_NAME, true, consumer);
            //producer 
            Channel channelOutput = connection.createChannel();
            channelOutput.exchangeDeclare("TeamFirebug", "direct");
            String queueName = channelOutput.queueDeclare().getQueue();
            channelOutput.queueBind(queueName, EXCHANGE_NAME, "normalizerToAggregator");

            LoanResponse loanResponse;
            while (true) {
                System.out.println("Reading");
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                System.out.println("CorrelationId: " + delivery.getProperties().getCorrelationId());

                String message = new String(delivery.getBody());
              
                JSONObject jsonObj = new JSONObject(message);


                loanResponse = new LoanResponse(jsonObj.getInt("ssn"), jsonObj.getDouble("interestRate"), "Teachers Json Bank");
                System.out.println("renter: " + loanResponse.getInterestRate());
                System.out.println("ssn: " + loanResponse.getSsn());
                System.out.println("bank : " + loanResponse.getBank());
//             System.out.println(" [x] Received '" + message + "'");
                channelOutput.basicPublish("TeamFirebug", "normalizerToAggregator", null, jsonObj.toString().getBytes());

            }

        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
