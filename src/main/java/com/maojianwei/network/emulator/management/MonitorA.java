package com.maojianwei.network.emulator.management;

import com.maojianwei.network.emulator.example.ExperimentA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest API for monitoring intermediate result in run time.
 *
 * Simple example for using this emulator framework. Network_Emulator :)
 *
 * 2019.04.09 Jianwei Mao
 */
@RestController
@RequestMapping("/expA")
public class MonitorA {

    @Autowired
    private ExperimentA experimentA;

    /**
     * Pause emulator for inspecting internal status of routers and switches, by set debug breakpoint.
     *
     * http://ip:port/expA/debug
     */
    @RequestMapping("/debug")
    public String debug() {
        return "Set breakpoint here, then you can check all internal status of routers and switches.";
    }

    /**
     * Show summary or some specific information of routers and switches, without pausing emulation.
     *
     * http://ip:port/expA/summary
     */
    @RequestMapping("/summary")
    public String summary() {
        StringBuilder sb = new StringBuilder();
        experimentA.getRouters().forEach(r -> {
            sb.append(String.format("\n====== Router %s ======\n", r.getName()));
            sb.append(r.summary());
        });
        experimentA.getSwitches().forEach(sw -> {
            sb.append(String.format("\n====== Switch %s ======\n", sw.getName()));
            sb.append(sw.summary());
        });
        return sb.toString();
    }
}
