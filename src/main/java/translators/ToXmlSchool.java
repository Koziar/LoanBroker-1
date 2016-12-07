/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package translators;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import config.ExchangeName;
import config.RabbitConnection;
import entity.Message;
import java.io.IOException;
import java.io.StringWriter;
import javax.lang.model.element.Element;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

//TO DO look in makeXmlString() method

/**
 *
 * @author nikolai 
 */
public class ToXmlSchool {

    public static void main(String[] args) throws IOException {
        //final String replyQueueName = "replyFromBanks";
        final String replyQueueName = "teachersXmlReply";
        final String EXCHANGE_NAME_SCHOOL = "cphbusiness.bankXML";
        final String exchangeName = ExchangeName.GLOBAL;

        RabbitConnection rabbitConnection = new RabbitConnection();

        Channel channel = rabbitConnection.makeConnection();
        channel.exchangeDeclare(exchangeName, "direct");
        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, exchangeName, "keyBankXML");
        
        //get banks from queue. "Get banks" component
        QueueingConsumer consumer = new QueueingConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                Message messageFromJson = getFromJson(message);
                sendMsgToBank(messageFromJson, properties.getCorrelationId(), EXCHANGE_NAME_SCHOOL, replyQueueName);
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

            String message = makeXmlString(msg);
            channel.basicPublish(exchangeName, "", props, message.getBytes());
            rabbitConnection.closeChannelAndConnection();
            System.out.println(" [x] Sent :" + msg.toString() + "");
        } catch (IOException ex) {
            System.out.println("Error in ToXmlSchool class - sendMsgToBank()");
            System.out.println(ex.getStackTrace());
        }
    }

    /**
     *
     * @param msg
     * @return a string of xml type LoanRequest class. if error returns null
     */
    private static String makeXmlString(Message msg) {
        LoanRequest dto = new LoanRequest();
        dto.setSsn(msg.getSsn());
        dto.setCreditScore(msg.getCreditScore());
        dto.setLoanAmount(msg.getLoanAmount());
          //!!!change this LoanDuration in Message class, it needs to be a String instead of int!!! because we need this format '1973-01-01 01:00:00.0 CET'
        dto.setLoanDuration(msg.getLoanDuration());

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(LoanRequest.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            //xml to string
            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(dto, sw);
            String xmlString = sw.toString();

            return xmlString;

        } catch (JAXBException e) {
            e.printStackTrace();
            System.out.println("!!!!!Error in class - ToXmlSchool - makeXmlString()");
        }
        return null;
    }
}
