import State.Client;
import State.State;
import spullara.nio.channels.FutureServerSocketChannel;
import spullara.nio.channels.FutureSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class Server {

    private final State state;
    private final FutureServerSocketChannel fssc;

    public Server() throws IOException {
        this.state = new State();
        this.fssc = new FutureServerSocketChannel();
        this.fssc.bind(new InetSocketAddress(12345));
    }

    public Server(State state) throws IOException {
        this.state = state;
        this.fssc = new FutureServerSocketChannel();
        this.fssc.bind(new InetSocketAddress(12345));
    }

    public FutureServerSocketChannel getFutureServerSocketChannel(){
        return this.fssc;
    }

    public void acceptNew() {
        CompletableFuture<FutureSocketChannel> fClient = this.getFutureServerSocketChannel().accept();

        fClient.thenAccept(client -> {
            // Create the structure to hold his info
            Client c = new Client(client, state.mq.currentID());
            // Add the client to the state
            state.clients.addClient(c);
            // Initialize a buffer for the client
            ByteBuffer buf = ByteBuffer.allocate(1000);
            // Put the client in a reading state
            this.read(c, buf);
            // Accept more connections
            acceptNew();
        });
    }

    public  void read(Client client, ByteBuffer buf) {
        FutureSocketChannelReader.readLine(client.getSocket(), buf).thenAccept(message -> {
            if(message == null){
                // Connection was closed
                state.clients.removeClient(client);
                return;
            }
            // Process message read otherwise
            System.out.println("Received: " + message);
            state.mq.putMessage(message);
            handleWrites();
            read(client, buf);
        });
    }

    // Broadcast
    private  void handleWrites() {
        for(Client c : this.state.clients.getClients()){
            write(c);
        }
    }

    private  void write(Client c) {
        String message = this.state.mq.getMessage(c.getMessageID());
        CompletableFuture<Void> writter = FutureSocketChannelWritter.write(c.getSocket(), message);

        writter.thenAccept(vd -> {
            c.incrementMessageID();
            //write(c);
        });
    }


}

