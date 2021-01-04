package CausalDelivery;

import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SimplePeerCausal {

    static int myPort;
    static int[] peers = {12340, 12341, 12342, 12343};

    static int[] l = {0, 0, 0, 0};

    static int myIndex;

    static List<Message> waiting = new ArrayList<>();

    public static void broadCastMessage(NettyMessagingService ms, String message) {

        // Incrementar o proprio contador
        l[myIndex] ++;

        Message m = new Message(message, l, myPort);

        // Enviar mensagem para os outros peers
        for(int p : peers) {
            if(p != myPort) {
                ms.sendAsync(Address.from(p), "message", Message.serialize(m));
            }
        }
    }

    public static boolean verifyMessage(Message m) {
        int port = m.port;
        int i = port - 12340;

        if(l[i] + 1 == m.r[i]) {
            boolean b = true;

            for(int j = 0; j < 4 && b; j++) {
                if(j != i) {
                    b = m.r[j] <= l[j];
                }
            }
            return b;
        }
        else {
            return false;
        }

    }

    public static void processMessage(Message m) {


        for(int i = 0; i < 4; i++) {
            l[i] = Integer.max(l[i], m.r[i]);
        }

        System.out.println(Arrays.toString(l) + "     " + m.port + ": " + m.message);

        for(Message msg : waiting) {
            if( verifyMessage(msg) ) {
                waiting.remove(msg);
                processMessage(msg);
            }
        }
    }


    public static void main(String[] args) {

        myPort = Integer.parseInt(args[0]);
        myIndex = myPort - 12340;

        ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
        NettyMessagingService ms = new NettyMessagingService("nome", Address.from(myPort), new MessagingConfig());


        ms.registerHandler("message", (a, m) -> {

            Message recv = Message.deserialize(m);

            if( verifyMessage(recv) ) {
                processMessage(recv);
            }
            else {
                System.out.println("###################");
                System.out.println(Arrays.toString(l));
                System.out.println(Arrays.toString(recv.r));
                System.out.println("###################");
                waiting.add(recv);
            }

        }, es);

        ms.start();

        System.out.println("Welcome to this Amazing Chat Service!");
        System.out.println("Type a message to begin!");

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
