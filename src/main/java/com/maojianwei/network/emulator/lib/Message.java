package com.maojianwei.network.emulator.lib;

public class Message {
    private String srcIp;
    private String dstIp;

    byte [] data;


    public Message(String srcIp, String dstIp, byte[] data) {
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.data = data;
    }


    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        // todo - check output
        return String.format("Src=%s, Dst=%s, Msg=\n%.2x\n%s" ,srcIp, dstIp, data, new String(data));
    }
}
