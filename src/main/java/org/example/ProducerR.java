package org.example;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProducerR {
    private final static String QUEUE_NAME = "my_queue";
    private static final String EXCHANGE_NAME = "my_exchange2";

    public static void main(String[] args) throws Exception {
        Connection connection = null;
        Channel channel = null;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");

            connection = factory.newConnection();
            channel = connection.createChannel();

//            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            //deklaracja exchange typu direct
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

            for (int i = 0; i < 10; i++) {
                String message = "Wiadomość " + i;
                List<String> list = new ArrayList<>();
                list.add("info");
                list.add("alert");
                list.add("news");
                Random random = new Random();
                String routingKey = list.get(random.nextInt(list.size()));
                channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());
                System.out.println(" [x] Wysłano '" + message + "' " + routingKey);
            }
        } finally {
            if (channel != null) {
                channel.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
