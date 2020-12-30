import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class BotClient {

    public static void main(String[] args) throws IOException, InterruptedException {

        if(args.length != 1) {
            System.out.println("missing arguments");
            System.exit(1);
        }

        //int sleepTime = Integer.parseInt(args[0]);
        //System.out.println("Sleep time set to " + sleepTime + "ms.");

        BotController bc = new BotController();

        Socket socket = new Socket(InetAddress.getLocalHost(),12348);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream());

        Thread reader = new Thread(new BotReader(in,bc));
        reader.start();

        Thread sender = new Thread(new BotSender(out,bc));
        sender.start();

        reader.join();
        sender.join();
    }

}

class BotController{
    // The controller will manage what the bot can do.
    // If the controllers is asleep, then nothing can happen. The other threads keep this controller updated.
    public boolean asleep;
    public ReentrantLock sleepLock;

    public BotController(){
        asleep = false;
        sleepLock = new ReentrantLock();
    }

    public void set_asleep() throws InterruptedException {
        int time = Util.ms2s(3);
        asleep = true;
        sleepLock.lock();
        Thread.sleep(time);
        sleepLock.unlock();
        asleep = false;
    }
}

class Util{
    public static int ms2s(int nSegundos){
        return nSegundos*1000;
    }
}

class BotSender implements Runnable{

    private int sleepTime;
    private PrintWriter out;
    private BotController bc;

    public BotSender(PrintWriter out, BotController bc){
        this.out = out;
        this.bc = bc;
    }

    public String genRandomString() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

    @Override
    public void run(){
        boolean connected = true;
        while(connected) {
            if(!bc.asleep){
                try {
                    String msg = this.genRandomString();
                    if(msg == null) {
                        connected = false;
                    }
                    else {
                        out.println(msg);
                        bc.set_asleep();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("disconnected!");
        this.out.close();
    }
}
class BotReader implements Runnable{
    private BufferedReader in;
    private int sleepTime;
    private BotController bc;

    public BotReader(BufferedReader in, BotController bc){
        this.in = in;
        this.bc = bc;
    }

    public void run() {
        boolean connected = true;
        while(connected) {
            if(!bc.asleep){
                try {
                    // ler resposta do servidor
                    String msg = this.in.readLine();

                    if(msg == null) {
                        connected = false;
                    } else {
                        System.out.println(msg);
                        bc.set_asleep();
                    }
                } catch(IOException | InterruptedException e) {
                    connected = false;
                }
            }

        }
    }

}
