/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loanbroker;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import config.ExchangeName;
import entity.Bank;
import entity.Message;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mathias
 */
public class GetBanks {

    private static Connection connection;
    private static QueueingConsumer consumer;

    private static Channel inputChannel;
    private static Channel outputChannel;

    public static void main(String[] args) throws Exception {
        init();
        while (true) {
            consume();
        }
    }

    private static void consume() throws Exception {
        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        String message = new String(delivery.getBody());
        System.out.println(" [x] Received : '" + message + "'");
        sendToRecipientList(message);
    }

    private static void sendToRecipientList(String msg) throws IOException {
        ArrayList<Bank> banks = new ArrayList<>();
        Gson gson = new Gson();
        
        Message message = gson.fromJson(msg, Message.class);
        
        ArrayList<String> banksFromRuleBase = (ArrayList<String>) getBanksFromRuleBase(message.getCreditScore());

        for (String bank : banksFromRuleBase) {
            banks.add(gson.fromJson(bank, Bank.class));
        }

        message.setBanks(banks);
        String toJson = gson.toJson(message);
        outputChannel.basicPublish(ExchangeName.OUTPUT_GET_BANKS, "", null, toJson.getBytes());
        System.out.println(" [x] Sent to : '" + toJson + "'");
    }

    private static void init() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        connection = factory.newConnection();
        inputChannel = connection.createChannel();
        outputChannel = connection.createChannel();

        inputChannel.exchangeDeclare(ExchangeName.OUTPUT_GET_CREDITSCORE, "fanout");
        String queueName = inputChannel.queueDeclare().getQueue();
        inputChannel.queueBind(queueName, ExchangeName.OUTPUT_GET_CREDITSCORE, "");

        outputChannel.exchangeDeclare(ExchangeName.OUTPUT_GET_BANKS, "fanout");

        consumer = new QueueingConsumer(inputChannel);
        inputChannel.basicConsume(queueName, true, consumer);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
    }

    public static List<String> getBanksFromRuleBase(int creditScore) {
        try {
            rule.RuleBase_Service service = new rule.RuleBase_Service();
            rule.RuleBase port = service.getRuleBasePort();
            return port.getBanks(creditScore);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
}
