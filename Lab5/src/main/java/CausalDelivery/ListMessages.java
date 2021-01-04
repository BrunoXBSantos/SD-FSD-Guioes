package CausalDelivery;

import java.util.ArrayList;
import java.util.List;

public class ListMessages {

    private List<Message> messages;

    public ListMessages(){
        this.messages = new ArrayList<>();
    }

    public List<Message> getMessages(){
        return this.messages;
    }

    public synchronized void add(Message m){
        this.messages.add(m);
    }

    public synchronized void pop(Message m){
        this.messages.remove(m);
    }
}
