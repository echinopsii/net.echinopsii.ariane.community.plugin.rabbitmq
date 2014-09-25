package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.rabbitTestTools

import com.rabbitmq.client.Channel
import com.rabbitmq.client.QueueingConsumer

class Rreceiver extends Worker {
    String exchange, queue;
    Channel channel;
    QueueingConsumer consumer;
    QueueingConsumer.Delivery delivery;

    Rreceiver(Channel channel, String exchange, String queue) {
        this.channel = channel
        this.exchange = exchange
        this.queue = queue
    }

    @Override
    void init() {
        this.exchange = exchange;
        this.queue = queue;
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(queue, true, consumer);
    }

    @Override
    void work() {
        delivery = consumer.nextDelivery();
    }
}