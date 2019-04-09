package com.maojianwei.network.emulator.example;


import com.maojianwei.network.emulator.bus.DefaultSwitch;
import com.maojianwei.network.emulator.bus.api.Switch;
import com.maojianwei.network.emulator.lib.MaoUtils;
import com.maojianwei.network.emulator.management.MonitorA;
import com.maojianwei.network.emulator.node.Router;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple example for using this emulator framework. Network_Emulator :)
 *
 * Monitor intermediate result by {@link MonitorA#debug() debug}, for example.
 *
 * 2019.04.09 Jianwei Mao
 */
@Component
public class ExperimentA implements CommandLineRunner {
    private List<Router> routers = new ArrayList<>();
    private List<DefaultSwitch> switches = new ArrayList<>();
    public List<Router> getRouters() {
        return routers;
    }
    public List<DefaultSwitch> getSwitches() {
        return switches;
    }

    @Override
    public void run(String... args) {
        Router r1 = new Router("r1");
        Router r2 = new Router("r2");
        Router r3 = new Router("r3");
        DefaultSwitch s1 = new DefaultSwitch("s1");
        DefaultSwitch s2 = new DefaultSwitch("s2");

        MaoUtils.connectRouterToSwitch(r1, "10.0.0.1", s1);
        MaoUtils.connectRouterToSwitch(r2, "10.0.0.2", s1);
        MaoUtils.connectRouterToSwitch(r2, "192.168.0.2", s2);
        MaoUtils.connectRouterToSwitch(r3, "192.168.0.3", s2);

        routers.add(r1);
        routers.add(r2);
        routers.add(r3);
        switches.add(s1);
        switches.add(s2);

        switches.forEach(Switch::start);
        routers.forEach(Router::start);
    }
}
