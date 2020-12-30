package RunnableAsyncs;

import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Async implements Runnable {
    private int myPort;
    private int []ports;
    private List<Integer> candidates;

    public Async(int myPort, int[] ports) {
        this.myPort = myPort;
        this.ports = ports;
        this.candidates = new ArrayList<>();

        candidates.add(myPort);
    }

    private void Announce(NettyMessagingService ms, ScheduledExecutorService es) {

        Random rand = new Random();

        for(Integer p : ports) {
            if(p != myPort) {
                es.schedule(() -> {
                    ms.sendAsync(Address.from(p), "leaderWannaBe", "".getBytes());
                }, rand.nextInt(5), TimeUnit.SECONDS);
            }
        }
    }

    public void whoIsMyLeader(ScheduledExecutorService es) {
        System.out.println(myPort + ": My leader atm is -> " + candidates.stream().max(Integer::compareTo));

        es.schedule(() -> {
            whoIsMyLeader(es);
        }, 1, TimeUnit.SECONDS);
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

        Announce(ms, es);

        es.schedule(() -> {
            whoIsMyLeader(es);
        }, 1, TimeUnit.SECONDS);

    }
}
