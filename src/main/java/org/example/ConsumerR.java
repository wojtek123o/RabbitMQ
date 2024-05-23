package org.example;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class ConsumerR {
    private final static String QUEUE_NAME = "my_queue";
    private static final String EXCHANGE_NAME = "my_exchange2";

    public static void main(String[] args) throws Exception {
        Connection connection = null;
        Channel channel = null;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();

//        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//        channel.exchangeDelete(EXCHANGE_NAME);
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        String queueName = channel.queueDeclare().getQueue();

        System.out.println("Podaj klucze routowania (oddzielone spacją):");
        Scanner scanner = new Scanner(System.in);
        String[] routingKeys = scanner.nextLine().split("\\s+");

        for(String routingKey: routingKeys) {
            channel.queueBind(queueName, EXCHANGE_NAME, routingKey);
        }

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println(" [x] Odebrano '" + msg + "' Typ: " + envelope.getRoutingKey());
            }
        };

        // Rejestracja konsumenta do kolejki
        channel.basicConsume(queueName, true, consumer);
        System.out.println(" [*] Czekam na wiadomości. Aby zakończyć, naciśnij CTRL+C");

        // Oczekiwanie na zakończenie
        Scanner scanner2 = new Scanner(System.in);
        while (true) {
            String input = scanner2.nextLine();
            if ("exit".equals(input.trim())) {
                // Zamknięcie kanału i połączenia
                channel.close();
                connection.close();
                System.out.println("Kanał i połączenie zostały zamknięte.");
                break; // Zakończenie pętli
            }
        }
    }
}
