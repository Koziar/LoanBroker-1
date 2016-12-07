package test;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import config.ExchangeName;

public class ExchangeEmitter {

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(ExchangeName.OUTPUT_LOAN_REQUEST, "fanout");

        String message = "700";

        channel.basicPublish(ExchangeName.OUTPUT_LOAN_REQUEST, "", null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();
    }
}
