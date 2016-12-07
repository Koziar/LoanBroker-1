package loanbroker;

import com.google.gson.Gson;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import config.ExchangeName;
import entity.Message;

public class LoanRequest {

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        
        Gson gson = new Gson();
        
        String ssn = "123456-7890";
        String loanDuration = "1973-01-01 01:00:00.0 CET";
        
        Message message = new Message(ssn, 0, 100, loanDuration);
        
        String jsonMessage = gson.toJson(message);
        
        channel.basicPublish(ExchangeName.OUTPUT_LOAN_REQUEST, "", null, jsonMessage.getBytes());
        System.out.println(" [x] CPR Sent '" + jsonMessage + "'");

        channel.close();
        connection.close();
    }
}