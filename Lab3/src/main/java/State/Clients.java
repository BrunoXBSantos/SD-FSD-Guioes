package State;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Clients {

    private List<Client> clients;  // each client only have socket and messageID
    private ReentrantLock clientsLock;

    public Clients(){
        this.clients = new ArrayList<Client>();
        this.clientsLock = new ReentrantLock();
    }

    public void addClient(Client c){
        this.clientsLock.lock();
        try{
            this.clients.add(c);
        }
        finally {
            this.clientsLock.unlock();
        }
    }

    public void removeClient(Client c){
        this.clientsLock.lock();
        try{
            this.clients.remove(c);
        }
        finally {
            this.clientsLock.unlock();
        }
    }

    public List<Client> getClients(){
        this.clientsLock.lock();
        try{
            return this.clients;
        }
        finally {
            this.clientsLock.unlock();
        }
    }

}
