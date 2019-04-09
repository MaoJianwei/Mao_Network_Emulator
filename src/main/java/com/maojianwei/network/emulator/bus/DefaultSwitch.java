package com.maojianwei.network.emulator.bus;

import com.maojianwei.network.emulator.bus.api.Switch;
import com.maojianwei.network.emulator.bus.api.SwitchMonitor;
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

public class DefaultSwitch implements Switch, SwitchMonitor {

    private final String switchName;

    private Map<String, Node> nodes;

    private ExecutorService recvThread;
    private BlockingQueue<Message> routerSendQueue;


    public DefaultSwitch(String switchName) {
        this.switchName = switchName;
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
    public String getName() {
        return switchName;
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


    // ====== DefaultSwitch specific function ======
    // now, not need


    // ====== DefaultSwitch monitor function ======
    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("switchName=%s\n", switchName));
        sb.append("nodes=\n");
        sb.append(nodes.toString());
        sb.append(String.format("\nrouterSendQueue.length=%d\n", routerSendQueue.size()));
        sb.append("routerSendQueue=\n");
        sb.append(routerSendQueue.toString());
        return sb.toString();
    }

    public String getPortStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("nodes=\n");
        sb.append(nodes.toString());
        return sb.toString();
    }

    @Override
    public String toString() {
        return getName();
    }



    private class SwitchRecvTask implements Runnable {

        @Override
        public void run() {
            try {
                while(true) {
                    Message msg = routerSendQueue.poll(1, TimeUnit.MILLISECONDS);
                    if (msg != null) {
                        System.out.println(String.format("============= Switch %s get msg =============\n", switchName) + msg.toString()); //FIXME - debug now

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
