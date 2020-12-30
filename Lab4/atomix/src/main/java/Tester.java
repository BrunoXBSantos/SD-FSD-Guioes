import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Tester {
    public static void main(String[] args) throws Exception {
        ScheduledExecutorService es = Executors.newScheduledThreadPool(1);

        //nao tenho sockets nem nada. Crio o servidor???
        NettyMessagingService ms = new NettyMessagingService("nome", Address.from(12345), new MessagingConfig());


        // FICO À escuta
        // Netty é o tipo de comunicao por eventos que permite por varias camadas sobre um sockets para enviar mensagens.  Depois de configurar damos start e fica há escuta de servidores.
        ms.registerHandler("hello", (a,m)->{
            System.out.println("Hello "+new String(m)+" from "+a);
        }, es);

        ms.start();

        //agendar para daqui a 1 seconds
        es.schedule(()-> {
            System.out.println("Timeout!");
        }, 1, TimeUnit.SECONDS);

        // envia para o servidor
        ms.sendAsync(Address.from("localhost", 12345), "hello", "world!".getBytes())
                .thenRun(()->{
                    System.out.println("Mensagem enviada!");
                })
                .exceptionally(t->{
                    t.printStackTrace();
                    return null;
                });
    }
}
