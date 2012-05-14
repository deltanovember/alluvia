package com.alluvialtrading.rabbit;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class EmitLogDirect {

    private static final String EXCHANGE_NAME = "order_routing";

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("alluvial-db.cmcrc.com");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        String severity = getSeverity(argv);
        String message = getMessage(argv);

        channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes());
        System.out.println(" [x] Sent '" + severity + "':'" + message + "'");

        String queueName = channel.queueDeclare().getQueue();
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);
        channel.queueBind(queueName, EXCHANGE_NAME, "clientonly");
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String receivingMessage = new String(delivery.getBody());
            String routingKey = delivery.getEnvelope().getRoutingKey();

            System.out.println(" [x] Received '" + routingKey + "':'" + receivingMessage + "'");
        }

        //channel.close();
        //connection.close();
    }

    private static String getSeverity(String[] strings) {
        if (strings.length < 1)
            return "info";
        return strings[0];
    }

    private static String getMessage(String[] strings) {
        if (strings.length < 2)
            return "Hello World!";
        return joinStrings(strings, " ", 1);
    }

    private static String joinStrings(String[] strings, String delimiter, int startIndex) {
        int length = strings.length;
        if (length == 0) return "";
        if (length < startIndex) return "";
        StringBuilder words = new StringBuilder(strings[startIndex]);
        for (int i = startIndex + 1; i < length; i++) {
            words.append(delimiter).append(strings[i]);
        }
        return words.toString();
    }
}
