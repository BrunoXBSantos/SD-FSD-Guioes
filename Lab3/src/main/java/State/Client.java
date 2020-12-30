package State;

import spullara.nio.channels.FutureSocketChannel;

import java.util.concurrent.locks.ReentrantLock;

public class Client {

    private FutureSocketChannel socket;
    private int messageID;
    private ReentrantLock lock;

    public Client(FutureSocketChannel fsc, int messageID){
        this.socket = fsc;
        this.messageID = messageID;
    }

    public FutureSocketChannel getSocket(){
        return this.socket;
    }

    public int getMessageID(){
        return messageID;
    }


    public void incrementMessageID(){
        this.messageID++;
    }

    public void lock(){ this.lock.lock();}

    public void unlock(){ this.lock.unlock();}
}
