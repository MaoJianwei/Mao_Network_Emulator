package com.maojianwei.network.emulator.node.api;

import com.maojianwei.network.emulator.bus.api.Switch;
import com.maojianwei.network.emulator.lib.Message;

public interface Node {

    boolean start();
    void stop();

    String getPortIp(Switch attachedSwitch);

    boolean recvMessage(Message msg);

    void connectSwitch(String portIp, Switch sw);
    void disconnectSwitch(Switch sw);
}
