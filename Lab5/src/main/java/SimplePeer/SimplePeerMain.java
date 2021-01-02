package SimplePeer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SimplePeerMain {

    public static void main(String[] args) {

        ScheduledExecutorService es = Executors.newScheduledThreadPool(1);

        int myPort = Integer.parseInt(args[0]);

        SimplePeer peer = new SimplePeer(myPort);

        peer.registerHandler(es);

        peer.start();

        peer.writeMessage();
    }
}
