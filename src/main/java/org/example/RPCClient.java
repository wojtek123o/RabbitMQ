package org.example;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RPCClient implements AutoCloseable{
    private final Connection connection;
    private final Channel channel;
    private final String requestQueueName = "rpc_queue";

    public RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public String call(String message) throws IOException, InterruptedException, ExecutionException {
        final String correlationId = UUID.randomUUID().toString(); //unikalny identyfikator wiadomosci
        final CompletableFuture<String> response = new CompletableFuture<>();

        String replyQueueName = channel.queueDeclare().getQueue(); //kolejka zwrotna na odpowiedzi serwera
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(correlationId)
                .replyTo(replyQueueName)
                .build();

        channel.basicPublish("", requestQueueName, props, message.getBytes(StandardCharsets.UTF_8)); //wyslanie wiadomosci żądania

        //odbior wiadomosci
        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                response.complete(new String(delivery.getBody(), StandardCharsets.UTF_8));
            }
        }, consumerTag -> {});

        String result = response.get();
        channel.basicCancel(ctag);
        return result;
    }

    public void close() throws Exception {
        connection.close();
    }

    public static void main(String[] args) {
        try (RPCClient rpcClient = new RPCClient()) {
            Scanner scanner = new Scanner(System.in);
            String userInput;
            do {
                System.out.print("Enter a number (or 'exit' to quit): ");
                userInput = scanner.nextLine();
                if (!userInput.equalsIgnoreCase("exit")) {
                    try {
                        String result = rpcClient.call(userInput);
                        System.out.println("Result: " + result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } while (!userInput.equalsIgnoreCase("exit"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
