package CausalDeliverySlow;

import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SimplePeerCausalMain {

    public static void main(String[] args) {

        System.out.println("Welcome to this Amazing Chat Service!");
        System.out.println("Type a message to begin!");

        ScheduledExecutorService es = Executors.newScheduledThreadPool(4);

        int myPort = Integer.parseInt(args[0]);

        SimplePeerCausalSlow peer = new SimplePeerCausalSlow(myPort);

        peer.registerHandler(es);

        peer.start();

        peer.writeMessage();
    }
}
