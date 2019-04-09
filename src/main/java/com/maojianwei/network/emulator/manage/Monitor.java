package com.maojianwei.network.emulator.manage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest API for monitoring intermediate result in run time.
 */
@RestController
@RequestMapping("/mao")
public class Monitor {

    @Value("${mao.debug:true}")
    private boolean debug;

    /**
     * http://ip:port/node/debug
     */
    @RequestMapping("/debug")
    public boolean debug() {
        return debug;
    }
}
