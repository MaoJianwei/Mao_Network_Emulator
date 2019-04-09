package com.maojianwei.network.emulator.bus;

import com.maojianwei.network.emulator.bus.api.Switch;
import com.maojianwei.network.emulator.lib.Message;
import com.maojianwei.network.emulator.node.api.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.maojianwei.network.emulator.lib.MaoConst.BROADCAST_ADDR;

public class DefaultSwitch implements Switch {

    private final String switchId;

    private Map<String, Node> nodes;

    private ExecutorService recvThread;
    private BlockingQueue<Message> routerSendQueue;


    public DefaultSwitch(String switchId) {
        this.switchId = switchId;
        nodes = new HashMap<>();
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
    public void plugInNode(Node node) {
        nodes.put(node.getPortIp(this), node);
    }

    @Override
    public void plugOutNode(Node node) {
        if (node.equals(nodes.getOrDefault(node.getPortIp(this), null))) {
            nodes.remove(node.getPortIp(this));
        }
    }

    @Override
    public boolean sendMessage(Message msg) {
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
                    Message msg = routerSendQueue.poll(1, TimeUnit.MILLISECONDS);
                    if (msg != null) {
                        System.out.println(String.format("============= Switch %s get msg =============\n", switchId) + msg.toString()); //FIXME - debug now

                        if (msg.getDstIp().equals(BROADCAST_ADDR)) {
                            nodes.values().forEach(node -> node.recvMessage(msg));
                        } else {
                            Node node = nodes.getOrDefault(msg.getDstIp(), null);
                            if (node != null) {
                                node.recvMessage(msg);
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {} // When switch is stopped.
        }
    }
}
