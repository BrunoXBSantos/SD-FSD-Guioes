import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.defaultThreadFactory;

public class ServerMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Server Online!");
        //Executor executor = Executors.newFixedThreadPool(2);
        // Creates ThreadPool
        AsynchronousChannelGroup g = AsynchronousChannelGroup.withFixedThreadPool(1,defaultThreadFactory());

        // AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open(g);
        // Create the server socket
        //FutureServerSocketChannel ssc = FutureServerSocketChannel.open(g);
        // no git do prof, falta um construtor na classe FutureServerSocket para isto
        // FutureServerSocketChannel ssc = new FutureServerSocketChannel(assc);

        Server server = new Server();

        // Accept connections
        server.acceptNew();

        // This keeps the server running forever
        g.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        System.out.println("Server Offline!");

    }

}
