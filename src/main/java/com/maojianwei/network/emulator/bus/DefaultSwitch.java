package com.maojianwei.network.emulator.bus;

import com.maojianwei.network.emulator.bus.api.Switch;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DefaultSwitch implements Switch {

    private BlockingQueue<Object> routerSendQueue; //TODO - design msg

    private ExecutorService recvThread;

    public DefaultSwitch() {
        routerSendQueue = new LinkedBlockingQueue();
        recvThread = null;
    }

    @Override
    public boolean start() {
        if (recvThread == null) {
            routerSendQueue.clear();
            recvThread = Executors.newSingleThreadExecutor();
            recvThread.submit(new SwitchRecvTask());
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        if (recvThread != null) {
            recvThread.shutdown();
            try {
                recvThread.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {}
            recvThread = null;
            routerSendQueue.clear();
        }
    }

    @Override
    public boolean sendMessage(Object msg) {
        try {
            while(!routerSendQueue.offer(msg, 1, TimeUnit.MILLISECONDS)) {}
            return true;
        } catch (InterruptedException e) {}
        return false;
    }

    private class SwitchRecvTask implements Runnable {

        @Override
        public void run() {
            try {
                while(true) {
                    Object msg = routerSendQueue.poll(1, TimeUnit.MILLISECONDS); //TODO - design msg
                    if (msg != null) {
                        System.out.println(msg.toString()); //FIXME - debug now
                    }
                }
            } catch (InterruptedException e) {} // When switch is stopped.
        }
    }
}
