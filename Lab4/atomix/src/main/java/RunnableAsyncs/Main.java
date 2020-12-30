package RunnableAsyncs;

public class Main {
    public static void main(String[] args) {

        int[] ports = {12340,12341,12342,12343};

        Async async1 = new Async(ports[0], ports);
        Async async2 = new Async(ports[1], ports);
        Async async3 = new Async(ports[2], ports);
        Async async4 = new Async(ports[3], ports);

        new Thread(async1).start();
        new Thread(async2).start();
        new Thread(async3).start();
        new Thread(async4).start();
    }
}
