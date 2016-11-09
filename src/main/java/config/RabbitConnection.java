package config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nikolai hansen
 */
public class RabbitConnection {

    private String host = "localhost";
    private String userName = "";
    private String password = "";
    private boolean localhost = true;
    private Channel channel;
    private ConnectionFactory factory;
    private Connection connection;

    public RabbitConnection() {
    }

    /**
     * @return null if exception or if variable is not set. Else channel
     */
    public Channel makeConnection() {
        if (host != null && userName != null && password != null) {
            try {
                factory = new ConnectionFactory();
                factory.setHost(host);
                if (!localhost) {
                    factory.setUsername(userName);
                    factory.setPassword(password);
                }
                connection = factory.newConnection();
                channel = connection.createChannel();
            } catch (IOException | TimeoutException exc) {
                System.out.println("Error in RabbitConnection class - makeConnection()");
                exc.printStackTrace();
                return null;
            }
            return this.channel;
        }
        return null;
    }

    public String getHost() {
        return host;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public Channel getChannel() {
        return channel;
    }

    public ConnectionFactory getFactory() {
        return factory;
    }

    public void closeChannelAndConnection() {
        if (connection != null && channel != null) {
            try {
                channel.close();
                connection.close();
            } catch (IOException | TimeoutException ex) {
                System.out.println("Error in class RabbitConnection - function closeChannelAndConnection");
            }
        }
    }

}
