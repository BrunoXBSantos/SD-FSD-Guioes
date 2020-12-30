package Sync;

import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Process1 {

    static int myPort = 12340;
    static int []ports = {12341, 12342, 12343};
    static int index;


    static List<Integer> candidates = new ArrayList<>();


    /*
        NOTA: Na verdade este sender não era necessário ser recursivo....
        Isto porque o problema só acontece ao tentar escrever no msm socket para o msm target,
        como isto estaria a enviar para diferentes targets poderia simplesmente ser um ciclo
     */
    public static void recursiveSender(NettyMessagingService ms) {
        // Says it wants to be the leader
        ms.sendAsync(Address.from("localhost", ports[index++]), "leaderWannaBe", "Random".getBytes())
                .thenRun(()->{
                    System.out.println("Mensagem enviada!");
                    if(index < 3) recursiveSender(ms);
                })
                .exceptionally(t->{
                    t.printStackTrace();
                    return null;
                });
    }

    public static void main(String[] args) throws Exception {

        ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
        NettyMessagingService ms = new NettyMessagingService("nome", Address.from(myPort), new MessagingConfig());

        candidates.add(myPort);

        // When receives a message from someone that wants to be a leader
        ms.registerHandler("leaderWannaBe", (a,m)->{
            System.out.println("Message from " + a);
            candidates.add(a.port());
        }, es);

        ms.start();

        // Starts Recursive call to sender
        es.schedule(() -> {
            recursiveSender(ms);
        }, 10, TimeUnit.SECONDS);

        // Starts timeout
        es.schedule(()-> {
            System.out.println("Timeout has ended!");
            System.out.println("And our leader is: " + candidates.stream().max(Integer::compareTo));
        }, 30, TimeUnit.SECONDS);




    }
}
