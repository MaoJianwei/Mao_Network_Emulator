package com.maojianwei.network.emulator.node;

import com.maojianwei.network.emulator.bus.api.Switch;
import com.maojianwei.network.emulator.lib.Message;
import com.maojianwei.network.emulator.node.api.Node;
import com.maojianwei.network.emulator.node.api.NodeMonitor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Router implements Node, NodeMonitor {

    private String routerName;

    private String routerId;
    private String loopbackIp;

    private Map<String, Switch> ipToSwitch;
    private Map<Switch, String> switchToIp;

    private ExecutorService recvThread;
    private BlockingQueue<Message> routerRecvQueue;


    public Router(String routerName) {
        this(routerName, null);
    }

    /**
     *
     * @param loopbackIp can be null
     */
    public Router(String routerName, String loopbackIp) {
        this.routerName = routerName;
        this.loopbackIp = loopbackIp;

        ipToSwitch = new HashMap<>();
        switchToIp = new HashMap<>();

        routerRecvQueue = new LinkedBlockingQueue();
        recvThread = null;
    }

    @Override
    public boolean start() {
        if (recvThread == null) {
            routerRecvQueue.clear();
            recvThread = Executors.newSingleThreadExecutor();
            recvThread.submit(new RouterRecvTask());
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
            routerRecvQueue.clear();
        }
    }

    @Override
    public String getName() {
        return routerName;
    }

    @Override
    public String getPortIp(Switch attachedSwitch) {
        return switchToIp.getOrDefault(attachedSwitch, null);
    }

    @Override
    public void connectSwitch(String portIp, Switch sw) {
        ipToSwitch.put(portIp, sw);
        switchToIp.put(sw, portIp);
    }

    @Override
    public void disconnectSwitch(Switch sw) {
        String ip = switchToIp.remove(sw);
        if (ip != null) {
            ipToSwitch.remove(ip);
        }
    }

    @Override
    public boolean recvMessage(Message msg) {
        try {
            while(!routerRecvQueue.offer(msg, 1, TimeUnit.MILLISECONDS)) {}
            return true;
        } catch (InterruptedException e) {}
        return false;
    }



    // ====== Router specific functions ======

    public void updateLoopbackIp(String loopbackIp) {
        this.loopbackIp = loopbackIp;
        calculateRouterId();
    }

    public String getRouterId() {
        return routerId;
    }

    private void calculateRouterId() {
        //TODO - wait for debug
        //FIXME - now, just update router id while updating loopback.
        if (loopbackIp == null) {

            long maxIp = 0;
            String maxIpStr = null;
            for (String ip : ipToSwitch.keySet()) {
                long tmpIp = 0;
                String [] ip4 = ip.split(".");
                for (String ipPart : ip4) {
                    tmpIp = (tmpIp << 8) | (Integer.valueOf(ipPart) & 0xFF);
                }
                if (tmpIp > maxIp) {
                    maxIp = tmpIp;
                    maxIpStr = ip;
                }
            }
            routerId = maxIpStr;
        } else {
            routerId = loopbackIp;
        }
    }




    // ====== Router monitor functions ======

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("routerName=%s\n", routerName));
        sb.append(String.format("routerId=%s\n", routerId));
        sb.append(String.format("loopbackIp=%s\n", loopbackIp));
        sb.append("ipToSwitch=\n");
        sb.append(ipToSwitch.toString());
        sb.append("\nswitchToIp=\n");
        sb.append(switchToIp.toString());
        sb.append(String.format("\nrouterRecvQueue.length=%d\n", routerRecvQueue.size()));
        sb.append("routerRecvQueue=\n");
        sb.append(routerRecvQueue.toString());
        return sb.toString();
    }

    public String getPortStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("ipToSwitch=\n");
        sb.append(ipToSwitch.toString());
        return sb.toString();
    }

    @Override
    public String toString() {
        return getName();
    }



    private class RouterRecvTask implements Runnable {

        @Override
        public void run() {
            try {
                while(true) {
                    Message msg = routerRecvQueue.poll(1, TimeUnit.MILLISECONDS);
                    if (msg != null) {
                        //todo - add service logic
                        System.out.println(String.format("============= Router %s get msg =============\n") + msg.toString()); //FIXME - debug now
                    }
                }
            } catch (InterruptedException e) {} // When switch is stopped.
        }
    }
}
