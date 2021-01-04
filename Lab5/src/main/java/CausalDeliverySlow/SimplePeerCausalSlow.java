package CausalDeliverySlow;

import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SimplePeerCausalSlow {

    private NettyMessagingService ms;
    private int myPort;
    private int[] peers = {12340, 12341, 12342}; //12343

    // vetor local em cada peer
    private int[] l = {0, 0, 0, 0};
    private int myIndex;
    private ListMessages waiting;

    public SimplePeerCausalSlow(int port){
        this.myPort = port;
        this.myIndex = port-12340;
        this.waiting = new ListMessages();
        this.ms = new NettyMessagingService("nome", Address.from(myPort), new MessagingConfig());
    }

    public void broadCastMessage(String message) {

        // Incrementar o proprio contador
        this.l[this.myIndex] ++;

        Message m = new Message(message, l, myPort);

        // Enviar mensagem para os outros peers
        for(int p : peers) {
            if(p != myPort) {
                ms.sendAsync(Address.from(p), "message", Message.serialize(m));
            }
        }
    }

    public boolean verifyMessage(Message m) {
        int port = m.port;
        int i = port - 12340;

        if(this.l[i] + 1 == m.r[i]) {
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

    public synchronized void processMessage(Message m) {

        for(int i = 0; i < 4; i++) {
            l[i] = Integer.max(l[i], m.r[i]);
        }
        System.out.println(Arrays.toString(l) + "     " + m.port + ": " + m.message);

        for(Message msg : waiting.getMessages()) {
            if( verifyMessage(msg) ) {
                waiting.pop(msg);
                System.out.println("''''''''''''''''''''''''Estava na fila: " + msg.message);
                processMessage(msg);
            }
        }
    }

    public void registerHandler(ScheduledExecutorService es){
        this.ms.registerHandler("message", (a, m) -> {

            Message recv = Message.deserialize(m);

            if(this.myPort == 12342) {
                try {
                    int i = new Random().nextInt(10);
                    System.out.println("''''''''''''''''''''''''Espera " + i + " segundos");
                    System.out.println("''''''''''''''''''''''''" + Arrays.toString(recv.r));
                    Thread.sleep(i*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if( verifyMessage(recv) ) {
                processMessage(recv);
            }
            else {
                this.waiting.add(recv);
            }

        }, es);
    }

    public void start(){
        this.ms.start();
    }

    public void writeMessage(){
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        String s;

        try {
            s = bf.readLine();

            while(!(bf.equals("exit")) && s.length() > 0) {
                broadCastMessage(s);
                s = bf.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("[" + this.myPort + "] disconnected");
        }
    }
}
