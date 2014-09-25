package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.rabbitTestTools

abstract class Worker implements Runnable {
    boolean isRunning = false;

    public abstract void init();
    public abstract void work();

    @Override
    void run() {
        init()
        isRunning = true;
        while(isRunning) {
            work()
        }
    }

    void stop() {
        isRunning = false;
    }
}