import java.util.concurrent.locks.ReentrantLock;

class ClientData{
    private String password;	// password
    private int id;			// id do client
    private ReentrantLock lock;

    public ClientData(String password, int id){
        this.password=password;
        this.id=id;
        this.lock = new ReentrantLock();
    }

    // devolve a password
    public String getPassword(){
        return this.password;
    }

    public int getId(){
        return this.id;
    }

    public void lock(){
        this.lock.lock();
    }

    public void unlock(){
        this.lock.unlock();
    }

}