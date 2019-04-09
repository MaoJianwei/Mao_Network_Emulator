package com.maojianwei.network.emulator.lib;

import com.maojianwei.network.emulator.bus.api.Switch;
import com.maojianwei.network.emulator.node.Router;

public final class MaoUtils {

    public static void connectRouterToSwitch(Router router, String routerPortIp, Switch sw) {

        // Add info to Router first, then add info to Switch
        // Because Switch will use info from Router.

        router.connectSwitch(routerPortIp, sw);
        sw.plugInNode(router);
    }

    public static void disconnectRouterFromSwitch(Router router, Switch sw) {

        // Remove info from Switch first, then remove info from Router
        // Because Switch will use info from Router.

        sw.plugOutNode(router);
        router.disconnectSwitch(sw);
    }
}
