/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 *
 * @author Mathias
 */
public class GetBanks {

    private static final String HOST_NAME = "datdb.cphbusiness.dk";
    private static final String EXCHANGE_NAME = "cphbusiness.bankXML";

    private static Channel channel;
    private static QueueingConsumer consumer;

    public static void main(String[] args) throws Exception {
        init();
        while (true) {
            consume();
        }
    }

    private static void consume() throws Exception {
        QueueingConsumer.Delivery delivery = consumer.nextDelivery();

        BasicProperties props = delivery.getProperties();
        BasicProperties replyProps = new BasicProperties.Builder().correlationId(props.getCorrelationId()).build();

        String message = new String(delivery.getBody());

        System.out.println(message);

        MessageProcessor messageProcessor = new MessageProcessor(message);
        messageProcessor.processMessage();

        String response = messageProcessor.getResponse();

        channel.basicPublish("", props.getReplyTo(), replyProps, response.getBytes());
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    }

    private static void init() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST_NAME);
        factory.setUsername("student");
        factory.setPassword("cph");

        Connection connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");
        channel.basicQos(1);

        consumer = new QueueingConsumer(channel);

        System.out.println("Initilazation Completed");
    }

}
