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
 * @author nikolai
 */
public class ToJsonSchool {

    //use replyQueueName as ' BasicProperties props' for the school rabbitmq  https://www.rabbitmq.com/tutorials/tutorial-six-java.html
    public static void main(String[] args) throws Exception {
        final String replyQueueName = "teachersJsonReply";
        final String EXCHANGE_NAME_SCHOOL = "cphbusiness.bankJSON";
        final String exchangeName = ExchangeName.GLOBAL;
        
        
        RabbitConnection rabbitConnection = new RabbitConnection();

        Channel channel = rabbitConnection.makeConnection();
        
        channel.exchangeDeclare(exchangeName, "direct");
        String queueName = channel.queueDeclare().getQueue();
        
        channel.queueBind(queueName, exchangeName, "keyBankJSON");
        
        //get banks from queue. "Get banks" component
        QueueingConsumer consumer = new QueueingConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received msg: "+message);
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


    private static void sendMsgToBank(Message msg, String corrId, String exchangeName, String replyQueueName){
        Gson gson = new Gson();
        RabbitConnection rabbitConnection = new RabbitConnection();
        Channel channel = rabbitConnection.makeConnection();
        try {
            channel.exchangeDeclare(exchangeName, "fanout");
            
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .correlationId(exchangeName+"#"+corrId)
                    .replyTo(replyQueueName)
                    .build();
            
            String message = gson.toJson(new DtoJsonBank(msg.getSsn(), msg.getCreditScore(), msg.getLoanAmount(), msg.getLoanDuration()));
            channel.basicPublish(exchangeName, "", props, message.getBytes());
            rabbitConnection.closeChannelAndConnection();
            System.out.println(" [x] Sent :" + msg.toString() + "");
        } catch (IOException ex) {
            System.out.println("Error in ToJsonSchool class - sendMsgToBank()");
            System.out.println(ex.getStackTrace());
        }
    }
}
