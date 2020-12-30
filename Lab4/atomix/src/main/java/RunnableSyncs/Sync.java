package RunnableSyncs;

import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sync implements Runnable {
    private int myPort;
    private int []ports;
    private List<Integer> candidates;

    public Sync(int myPort, int[] ports) {
        this.myPort = myPort;
        this.ports = ports;
        this.candidates = new ArrayList<>();

        candidates.add(myPort);
    }

    private void Announce(NettyMessagingService ms) {
        for(Integer p : ports) {
            if(p != myPort)
                ms.sendAsync(Address.from(p), "leaderWannaBe", "".getBytes());
        }
    }

    @Override
    public void run() {
        ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
        NettyMessagingService ms = new NettyMessagingService("nome", Address.from(myPort), new MessagingConfig());

        ms.registerHandler("leaderWannaBe", (a,m) -> {
            System.out.println(myPort + ": Recebi mensagem de " + a);
            candidates.add(a.port());
        }, es);

        ms.start();


        es.schedule(() -> {
            Announce(ms);
        }, 3, TimeUnit.SECONDS);

        es.schedule(() -> {
            System.out.println(myPort + ": Timeout has Ended!");
            System.out.println(myPort + ": My leader is -> " + candidates.stream().max(Integer::compareTo));
        }, 10, TimeUnit.SECONDS);

    }
}
