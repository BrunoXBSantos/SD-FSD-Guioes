package CausalDeliverySlow;

import java.io.*;

public class Message implements Serializable {

    public String message;
    public int[] r;
    public int port;

    public Message(String message, int[] r, int port) {

        this.message = message;
        this.r = r.clone();
        this.port = port;

    }

    public static byte[] serialize(Message obj) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(obj);
            return out.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
            return "ERROR".getBytes();
        }
    }

    public static Message deserialize(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return (Message) is.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
            int[] v = {};
            return new Message("ERROR", v, 0);
        }
    }

}
