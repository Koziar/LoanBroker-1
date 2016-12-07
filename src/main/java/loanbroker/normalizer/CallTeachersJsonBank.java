/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker.normalizer;

/**
 *
 * @author Jonathan
 */
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import java.io.IOException;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.AMQP.BasicProperties;
import dto.DtoJsonBank;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author nikolai http://datdb.cphbusiness.dk:15672/ student cph
 */
public class CallTeachersJsonBank {

    private static final String EXCHANGE_NAME = "cphbusiness.bankJSON";
    private static final String replyQueueName = "testQueue_reply";

    public static void main(String[] args) {
        Gson gson = new Gson();
        DtoJsonBank jsonBank = null;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("datdb.cphbusiness.dk");
            factory.setPort(5672);
            factory.setUsername("student");
            factory.setPassword("cph");

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

            //reply
            String corrId = java.util.UUID.randomUUID().toString();
            BasicProperties props = new BasicProperties.Builder()
                    .correlationId("test1122")
                    .replyTo(replyQueueName)
                    .build();
            
            
             String message = gson.toJson(new DtoJsonBank("1605789787", 598, 10.0, ""));
            channel.basicPublish(EXCHANGE_NAME, "", props, message.getBytes());

            channel.close();
            connection.close();

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }

}
