import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServerSimple {

    public static void main(String[] args) throws IOException {

        if(args.length != 1) {
            System.out.println("wrong number of arguments");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        ServerSocket listener = new ServerSocket(port);

        System.out.println("running on port " + port);

        Clients clients = new Clients();
        Integer nextId = 1;

        while(true) {
            Socket socket = listener.accept();
            Thread handler = new Thread(new ClientHandler(socket, nextId, clients));
            handler.start();

            nextId++;
        }
    }
}

class ClientHandler implements Runnable {

    private Socket socket;
    private Integer id;
    private Clients clients;

    public ClientHandler(Socket socket, Integer id, Clients clients) {
        this.socket = socket;
        // System.out.println("Meu ID:" + id);
        this.id = id;
        this.clients = clients;
    }

    public void run() {
        System.out.println("new connection");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            boolean autoFlush = true;
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), autoFlush);

            this.clients.register(this.id, out);

            boolean connected = true;
            while(connected) {
                try {
                    // ler algo do cliente
                    String msg = in.readLine();

                    if(msg == null) {
                        connected = false;
                    } else {
                        // escrever esse mesmo algo de volta
                        this.clients.sendAll(this.id, msg);
                    }
                } catch(IOException e) {
                    connected = false;
                }
                // desnecessária por causa do autoFlush = true
                // out.flush();
            }

            this.clients.unregister(this.id);

            System.out.println("client disconnected");
            this.socket.shutdownInput();
            this.socket.shutdownOutput();
            this.socket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}

class Clients {

    private HashMap<Integer, Connection> map;

    public Clients() {
        this.map = new HashMap<>();
    }

    synchronized void register(Integer id, PrintWriter out) {
        System.out.println("id de registo: " +id);
        Connection connection = new Connection(out);
        this.map.put(id, connection);
        System.out.println("Fim do registo do id: " + id);
    }

    synchronized void unregister(Integer id) {
        this.map.remove(id);
    }

    synchronized void sendAll(Integer id, String message) {
        // TODO use this.map.entrySet()
        System.out.println("\nLista");
        for(Integer i : this.map.keySet()){
            System.out.println(i);
        }
        System.out.println("--------------");


        for(Integer owner: this.map.keySet()) {
            Connection connection = this.map.get(owner);
            // connection.lock();
            connection.send(id + ": " + message);
            System.out.println("enviou para o " + owner);

            // connection.unlockhis();
        }
    }

    void unorderedSendAll(Integer id, String message) {
        // TODO use this.map.entrySet()
        for(Integer owner: this.map.keySet()) {
            Connection connection = this.map.get(id);
            // connection.lock();
            connection.send(id + ": " + message);

            // connection.unlock();
        }
    }
}

class Connection {
    private PrintWriter out;

    public Connection(PrintWriter out) {
        this.out = out;
    }

    synchronized void send(String message) {
        this.out.println(message);
    }
}
