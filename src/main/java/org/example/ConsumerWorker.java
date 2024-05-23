package org.example;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class ConsumerWorker {
    private final static String QUEUE_NAME = "my_queue";

    public static void main(String[] args) throws Exception {
        Connection connection = null;
        Channel channel = null;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();

        //deklaracja aktualnie dostarczonych klientowi wiadomosci
        channel.basicQos(1);

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);


        Channel finalChannel = channel;
        DefaultConsumer consumer = new DefaultConsumer(finalChannel) {
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, StandardCharsets.UTF_8);
                System.out.println(" [x] Odebrano '" + msg + "'");

                try {
                    Thread.sleep(Long.parseLong(args[0]));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                //potwierdzenie konsumpcji komunikatu
                finalChannel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        // Rejestracja konsumenta do kolejki - nie potwierdzamy automatycznie konsumpcji wiad
        channel.basicConsume(QUEUE_NAME, false, consumer);
        System.out.println(" [*] Czekam na wiadomości. Aby zakończyć, napisz exit");

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
