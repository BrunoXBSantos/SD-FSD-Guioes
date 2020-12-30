package Chat.Server;

import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;


import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Server {

    static int port = 12345;

    static ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
    static NettyMessagingService ms = new NettyMessagingService("nome", Address.from(port), new MessagingConfig());

    static RoomManager rm = new RoomManager();


    public static void main(String[] args) {


        ms.registerHandler("join_room", (a,m)->{

            try {
                boolean b = rm.joinRoom(a, new String(m));

                if(b) {
                    ms.sendAsync(a, "joined_room", "".getBytes());
                }
                else {
                    ms.sendAsync(a, "joined_room", "Room Created!".getBytes());
                }
            }
            catch (UserArlreadyInRoom e) {
                ms.sendAsync(a, "already_in_room", "".getBytes());
            }

        }, es);

        ms.registerHandler("leave_room", (a,m)->{

            try {
                rm.leaveRoom(a);
                ms.sendAsync(a, "left_room", "".getBytes());
            }
            catch (UserNotInRoom e) {
                ms.sendAsync(a, "not_in_room", "".getBytes());
            }

        }, es);

        ms.registerHandler("message", (a,m)->{

            System.out.println("Message Received");

            try {
                List<Address> members = rm.getUserRoom(a);

                for(Address dest : members) {
                    ms.sendAsync(dest, "message", (a + ": " + new String(m)).getBytes());
                }

                ms.sendAsync(a, "message_sent", "".getBytes());
            }
            catch (UserNotInRoom e) {
                ms.sendAsync(a, "not_in_room", "".getBytes());
            }

        }, es);

        ms.start();
    }
}
