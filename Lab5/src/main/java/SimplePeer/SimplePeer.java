package SimplePeer;

import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledExecutorService;

public class SimplePeer {

    private int myPort;
    private NettyMessagingService ms;
    private String name;

    // outros peers
    private int[] ports = {12340, 12341, 12342, 12343};

    public SimplePeer(int port){
        this.myPort = port;
        this.ms = new NettyMessagingService("nome", Address.from(this.myPort), new MessagingConfig());
        // this.name = name;
    }

    public void registerHandler(ScheduledExecutorService es){
        this.ms.registerHandler("message", (a,m) -> {
            System.out.println("[" + a + "] :" + new String(m));
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

    public void broadCastMessage(String m){
        for(int p:this.ports){
            if(p != this.myPort){
                this.ms.sendAsync(Address.from(p),"message",m.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

}