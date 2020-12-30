package RunnableSyncs;

public class Main {
    public static void main(String[] args) {

        int[] ports = {12340,12341,12342,12343};

        Sync sync1 = new Sync(ports[0], ports);
        Sync sync2 = new Sync(ports[1], ports);
        Sync sync3 = new Sync(ports[2], ports);
        Sync sync4 = new Sync(ports[3], ports);

        new Thread(sync1).start();
        new Thread(sync2).start();
        new Thread(sync3).start();
        new Thread(sync4).start();
    }
}
