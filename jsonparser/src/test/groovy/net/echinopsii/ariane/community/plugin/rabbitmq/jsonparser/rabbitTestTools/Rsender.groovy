package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.rabbitTestTools

import com.rabbitmq.client.Channel

/**
 * Created by echinopsii on 25/09/14.
 */
class Rsender extends Worker {
    String exchange, queue;
    Channel channel

    Rsender(Channel channel, String exchange, String queue) {
        this.channel = channel
        this.exchange = exchange;
        this.queue    = queue;
    }

    @Override
    void init() {
        this.exchange = exchange
        this.queue = queue
    }

    @Override
    void work() {
        channel.basicPublish(this.exchange,this.queue,null,"msg body".bytes)
        Thread.sleep(2000)
    }
}
