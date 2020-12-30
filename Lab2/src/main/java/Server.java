import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.defaultThreadFactory;

class Context {

    public Context(ByteBuffer buf, AsynchronousSocketChannel sc, HashMap<Integer, AsynchronousSocketChannel> logged) {
        this.buf = buf;
        this.sc = sc;
        this.logged = logged;
    }

    ByteBuffer buf;
    AsynchronousSocketChannel sc;
    HashMap<Integer, AsynchronousSocketChannel> logged;
}

public class Server {

    private Thread currentThread;

    private static void recRead(AsynchronousSocketChannel sc, Context c){
        sc.read(c.buf, c, new CompletionHandler<Integer, Context>() {
            @Override
            public void completed(Integer integer, Context c) {
                if(integer == -1) {
                    // Fecha socket e faz return
                    try {
                        sc.close();
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Le outra vez
                    recRead(sc, c);
                }
                c.buf.flip();

                for(AsynchronousSocketChannel sc: c.logged.values()) {
                    sc.write(c.buf.duplicate(), c, new CompletionHandler<Integer, Context>() {
                        @Override
                        public void completed(Integer integer, Context context) {
                            System.out.println("Done!");
                        }
                        @Override
                        public void failed(Throwable throwable, Context context) {

                        }
                    });
                }
                c.buf.clear();
            }

            @Override
            public void failed(Throwable throwable, Context c) {
                System.out.println("Client closed.");
            }
        });
    }

    static String extract(final ByteBuffer buffer) {
        if (Objects.isNull(buffer)) {
            throw new IllegalArgumentException("Buffer required");
        }

        buffer.flip();
        return new String(buffer.array()).trim();
    }

    private static final CompletionHandler<AsynchronousSocketChannel, ServerReady> ach =
            new CompletionHandler<AsynchronousSocketChannel, ServerReady>() {
                @Override
                public void completed(AsynchronousSocketChannel sc, ServerReady server) {
                    System.out.println("Accepted!");

                    ByteBuffer buf = ByteBuffer.allocate(1000);

                    sc.read(buf, server, new CompletionHandler<Integer, ServerReady>() {
                        @Override
                        public void completed(Integer integer, ServerReady serverReady) {
                            // verificacao se existe
                            String s = extract(buf);
                            System.out.println(s);
                            String name;
                            String password;
                            StringTokenizer st = new StringTokenizer(s, " ");
                            name = st.nextToken();
                            password = st.nextToken();
                            int id = 0;
                            boolean connected = true;

                            boolean existClient = server.dataBaseServer.existClient(name);
                            if (!existClient){
                                id = GenerateId.getId();
                                // criar novo cliente
                                server.dataBaseServer.createClient(name, password, id);
                            }
                            else{
                                boolean verifiedPass = server.dataBaseServer.checkPassword(name, password);
                                if(!verifiedPass){
                                    connected = false;
                                }
                                else{
                                    id = server.dataBaseServer.getId(name);
                                }
                            }

                            if(!connected){
                                try {
                                    sc.close();
                                    return;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            Context c = new Context(buf, sc, server.logged);
                            server.connect(id, c.sc);
                            recRead(sc, c);

                        }

                        @Override
                        public void failed(Throwable throwable, ServerReady serverReady) {

                        }
                    });

                    acceptRec(server);
                }

                @Override
                public void failed(Throwable throwable, ServerReady o) {

                }
            };

    public static void acceptRec(ServerReady server) {
        server.ssc.accept(server, ach);
    }

    public static void main(String[] args) throws Exception {

        AsynchronousChannelGroup g =
                AsynchronousChannelGroup.withFixedThreadPool(1, defaultThreadFactory());

        AsynchronousServerSocketChannel ssc =
                AsynchronousServerSocketChannel.open(g);
        ssc.bind(new InetSocketAddress(12345));

        ServerReady server = new ServerReady(ssc);

        acceptRec(server);

        g.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        System.out.println("Terminei!");
    }
}


class ServerReady{
    HashMap<Integer, AsynchronousSocketChannel> logged;
    AsynchronousServerSocketChannel ssc;
    DataBaseServer dataBaseServer;

    public ServerReady(AsynchronousServerSocketChannel ssc){
        this.logged = new HashMap<Integer, AsynchronousSocketChannel>();
        this.ssc=ssc;
        this.dataBaseServer = new DataBaseServer();
    }

    public synchronized void connect(Integer id, AsynchronousSocketChannel sc) {
        this.logged.put(id, sc);
    }

    public synchronized void disconnect(Integer id) {
        logged.remove(id);
    }
}


class GenerateId{
    public static int id = 0;

    public static synchronized int getId(){
        return ++id;
    }
}

