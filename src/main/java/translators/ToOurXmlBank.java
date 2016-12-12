/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package translators;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import config.ExchangeName;
import config.RabbitConnection;
import entity.Message;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeoutException;
import javax.lang.model.element.Element;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

//TO DO look in makeXmlString() method
/**
 *
 * @author nikolai
 */
public class ToOurXmlBank {

    final static String EXCHANGE_NAME_SCHOOL = "cphbusiness.webserviceXml";
    final static String exchangeName = "TeamFirebug";
    final static String replyQueueName = "teamFirebugsSoapxmlBankResponse";

    private static Message getFromJson(String json) {
        System.out.println(json);
        Gson g = new Gson();
        return g.fromJson(json, Message.class);
    }

    private static void connectToWebService(Message msg, String corrId, String exchangeName, String replyQueueName) {
        try { // Call Web Service Operation
            services.LoanResponseService_Service service = new services.LoanResponseService_Service();
            services.LoanResponseService port = service.getLoanResponseServicePort();
            // TODO initialize WS operation arguments here

            // TODO process result here
            int ssnMsg = Integer.parseInt(msg.getSsn());
            boolean result = port.loanResponse(ssnMsg, msg.getCreditScore(), msg.getLoanAmount(), null, replyQueueName, corrId);
            System.out.println("Result = " + result);
        } catch (Exception ex) {
            System.out.println(ex);
            // TODO handle custom exceptions here
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(exchangeName, "direct");
        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, exchangeName, "ourBankXML");

        //get banks from queue. "Get banks" component
        QueueingConsumer consumer = new QueueingConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                Message messageFromJson = getFromJson(message);
                connectToWebService(messageFromJson, properties.getCorrelationId(), EXCHANGE_NAME_SCHOOL, replyQueueName);

            }
        };
        channel.basicConsume(queueName, true, consumer);

    }

}
