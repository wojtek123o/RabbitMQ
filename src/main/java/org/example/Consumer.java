package org.example;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Consumer {
    private final static String QUEUE_NAME = "my_queue";

    public static void main(String[] args) throws Exception {
        Connection connection = null;
        Channel channel = null;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println(" [x] Odebrano '" + msg + "'");
            }
        };

        // Rejestracja konsumenta do kolejki
        channel.basicConsume(QUEUE_NAME, true, consumer);
        System.out.println(" [*] Czekam na wiadomości. Aby zakończyć, naciśnij CTRL+C");

        // Oczekiwanie na zakończenie
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
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
