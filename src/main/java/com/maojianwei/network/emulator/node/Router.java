package com.maojianwei.network.emulator.node;

import com.maojianwei.network.emulator.bus.DefaultSwitch;
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

public class Router implements Node {

    private String routerId;
    private String loopbackIp;

    private Map<String, Switch> ipToSwitch;
    private Map<Switch, String> switchToIp;

    private ExecutorService recvThread;
    private BlockingQueue<Message> routerRecvQueue;

    /**
     *
     * @param loopbackIp can be null
     */
    public Router(String loopbackIp) {
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
