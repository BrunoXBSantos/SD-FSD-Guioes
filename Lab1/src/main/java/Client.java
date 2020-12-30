import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        if(args.length != 2) {
            System.out.println("missing arguments");
            System.exit(1);
        }

        String address = args[0];
        int port = Integer.parseInt(args[1]);

        Socket socket = new Socket(address,port);

        System.out.println("connected!");

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Thread reader = new Thread(new SocketReader(in));
        reader.start();

        boolean autoFlush = true;
        PrintWriter out = new PrintWriter(socket.getOutputStream(), autoFlush);

        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

        boolean connected = true;
        while(connected) {
            try {
                // ler algo do keyboard
                String msg = keyboard.readLine();

                if(msg == null) {
                    connected = false;
                } else {
                    // escrever esse mesmo algo no servidor
                    out.println(msg);
                }
            } catch(IOException e) {
                connected = false;
            }
            // desnecessária por causa do autoFlush = true
            // out.flush();
        }

        System.out.println("disconnected!");
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
    }
}
class SocketReader implements Runnable {
    private BufferedReader in;

    public SocketReader(BufferedReader in) {
        this.in = in;
    }

    public void run() {
        boolean connected = true;
        while(connected) {
            try {
                // ler resposta do servidor
                String msg = this.in.readLine();

                if(msg == null) {
                    connected = false;
                } else {
                    System.out.println(msg);
                }
            } catch(IOException e) {
                connected = false;
            }
        }
    }
}
