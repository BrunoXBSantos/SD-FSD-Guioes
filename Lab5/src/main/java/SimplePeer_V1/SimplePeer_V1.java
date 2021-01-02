package SimplePeer_V1;

import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SimplePeer_V1 {

    static int myPort;
    static int[] peers = {12340, 12341, 12342, 12343};

    public static void broadCastMessage(NettyMessagingService ms, String message) {
        for(int p : peers) {
            if(p != myPort) {
                ms.sendAsync(Address.from(p), "message", message.getBytes());
            }
        }
    }

    public static void main(String[] args) {

        myPort = Integer.parseInt(args[0]);

        ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
        NettyMessagingService ms = new NettyMessagingService("nome", Address.from(myPort), new MessagingConfig());

        ms.registerHandler("message", (a, m) -> {

            System.out.println(a + ": " + new String(m));

        }, es);

        ms.start();

        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        String s;

        while(true) {
            try {
                s = bf.readLine();

                broadCastMessage(ms, s);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
