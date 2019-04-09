package com.maojianwei.network.emulator.bus.api;

import com.maojianwei.network.emulator.lib.Message;
import com.maojianwei.network.emulator.node.api.Node;

/**
 * L3 switch, match dst-ip exactly, support broadcast(255.255.255.255).
 *
 * Multicast is on the way ...
 * VLAN is unsupported
 */
public interface Switch {

    /**
     * Reentry is not supported.
     *
     * @return true if switch stopped before. false otherwise.
     */
    boolean start();

    /**
     * Reentry is supported.
     */
    void stop();

    /**
     * Connect one node to switch.
     *
     * Will replace the existing node with the same ip addr.
     *
     * @param node
     */
    void plugInNode(Node node);

    /**
     * Disconnect one node from switch.
     * @param node
     */
    void plugOutNode(Node node);

    /**
     * Block until sent msg.
     *
     * @param msg routing protocol message, with src-dst ip pair
     * @return false if InterruptedException is caught. true otherwise.
     */
    boolean sendMessage(Message msg);
}
