package com.maojianwei.network.emulator.bus.api;

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
     * Block until sent msg.
     *
     * @param msg routing protocol message, with src-dst ip pair
     * @return false if InterruptedException is caught. true otherwise.
     */
    boolean sendMessage(Object msg); // TODO - design msg
}
