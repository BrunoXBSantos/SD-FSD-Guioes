import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

class DataBaseServer {

    private HashMap<String, ClientData> clientsList;  // lista de clientes
    private ReentrantLock dataBaseServer;


    public DataBaseServer() {
        this.clientsList = new HashMap<>();
        this.dataBaseServer = new ReentrantLock();
    }

    // Verificar se Existe cliente
    public boolean existClient(String name) {
        boolean flag;
        this.dataBaseServer.lock();
        flag = this.clientsList.containsKey(name);
        this.dataBaseServer.unlock();
        return flag;
    }

    public int getId(String name){
        return this.clientsList.get(name).getId();
    }

    // cria um cliente, recebendo o nome e a pass
    public boolean createClient(String name, String pass, Integer id) {
        this.dataBaseServer.lock();
        if (!this.clientsList.containsKey(name)) {
            ClientData clientData = new ClientData(pass, id);
            this.clientsList.put(name, clientData);
            this.dataBaseServer.unlock();
            return true;  // cliente criado
        } else {
            this.dataBaseServer.unlock();
            return false;  // cliente já existe
        }
    }

    // VERIFICA A PASSWORD
    public boolean checkPassword(String name, String pass) {
        this.dataBaseServer.lock();
        if (this.clientsList.containsKey(name)) {
            ClientData clientData = this.clientsList.get(name);
            clientData.lock();
            this.dataBaseServer.unlock();
            boolean flag = pass.equals(clientData.getPassword());
            clientData.unlock();
            if (flag) {
                return true; // pass faz match
            } else {
                return false; // pass nao corresponde
            }
        } else {
            this.dataBaseServer.unlock();
            return false;
        }

    }


}