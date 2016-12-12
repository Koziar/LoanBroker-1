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
import dto.DtoOurSoapXmlBank;
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
public class NormalizerOurSoapXmlBank {

    private static final String EXCHANGE_NAME = "teamFirebugsSoapxmlBankResponse";

    public static void main(String[] argv) throws IOException, InterruptedException, TimeoutException {
        //Connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //Consumer
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        //s channel.queueBind(queueName, EXCHANGE_NAME, "OurSoapXmlBank");
        channel.queueBind(queueName, EXCHANGE_NAME, "");
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        //Producer
        Channel channelOutput = connection.createChannel();
        channelOutput.exchangeDeclare("TeamFirebug", "direct");
        
        LoanResponse loanResponse;
        DtoOurSoapXmlBank dtoOurSoapXmlBank;
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            String routingKey = delivery.getEnvelope().getRoutingKey();

             dtoOurSoapXmlBank = JAXB.unmarshal(new StringReader(message), DtoOurSoapXmlBank.class);
            loanResponse = new LoanResponse(dtoOurSoapXmlBank.getSsn(), dtoOurSoapXmlBank.getInterestRate(), "Our Soap Xml bank", delivery.getProperties().getCorrelationId());
            // loanResponse.setBank(routingKey);

            System.out.println("renter: " + loanResponse.getInterestRate());
            System.out.println("ssn: " + loanResponse.getSsn());
            System.out.println("bank : " + loanResponse.getBank());
            JSONObject jsonObj = new JSONObject(loanResponse);
            // channelOutput.basicPublish("", RoutingKeys.Aggregator, null, jsonObj.toString().getBytes());
            channelOutput.basicPublish("TeamFirebug", "normalizerToAggregator", null, jsonObj.toString().getBytes());
        }
    }

}
