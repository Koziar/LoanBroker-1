/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package translators;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import config.RabbitConnection;
import config.*;
import entity.Bank;
import entity.Message;
import java.io.IOException;

/**
 *
 * @author Mathias
 */
public class ToOurJsonBank {

    private static final String REPLY_QUEUE_NAME = "ourJsonReply";

    public static void main(String[] args) throws Exception {

        RabbitConnection rabbitConnection = new RabbitConnection();
        Channel channel = rabbitConnection.makeConnection();
        channel.exchangeDeclare(ExchangeName.GLOBAL, "direct");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, ExchangeName.GLOBAL, RoutingKeys.OUR_JSON_BANK);

        QueueingConsumer consumer = new QueueingConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received msg: " + message);
                Message messageFromJson = getFromJson(message);

                sendMsgToBank(messageFromJson, properties.getCorrelationId(), ExchangeName.OUTPUT_TRANSLATOR_TO_OUR_JSON_BANK, REPLY_QUEUE_NAME);
            }
        };
        
        channel.basicConsume(queueName, true, consumer);
    }

    private static Message getFromJson(String json) {
        Gson g = new Gson();
        return g.fromJson(json, Message.class);
    }

    private static void sendMsgToBank(Message msg, String corrId, String exchangeName, String replyQueueName) {
        Gson gson = new Gson();
        RabbitConnection rabbitConnection = new RabbitConnection();
        Channel channel = rabbitConnection.makeConnection();
        try {
            channel.exchangeDeclare(exchangeName, "fanout");

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();

            String message = gson.toJson(new DtoJsonBank(msg.getSsn(), msg.getCreditScore(), msg.getLoanAmount(), "360"));
            channel.basicPublish(exchangeName, "", props, message.getBytes());
            rabbitConnection.closeChannelAndConnection();
            System.out.println(" [x] Sent :" + msg.toString() + "");
        } catch (IOException ex) {
            System.out.println("Error in ToOurJsonBank class - sendMsgToBank()");
            System.out.println(ex.getStackTrace());
        }
    }
}
