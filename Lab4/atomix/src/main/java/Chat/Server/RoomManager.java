package Chat.Server;

import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomManager {
    private Map<String, List<Address>> rooms;
    private Map<Address, String> users_connected;

    public RoomManager() {
        this.rooms = new HashMap<>();
        this.users_connected = new HashMap<>();
    }

    public boolean is_user_connected(Address user) {
        return users_connected.get(user) != null;
    }

    public String user_room(Address user) {
        return users_connected.get(user);
    }

    public boolean joinRoom(Address address, String roomName) throws UserArlreadyInRoom {

        if(!is_user_connected(address)) {
            List<Address> room = this.rooms.get(roomName);

            users_connected.put(address, roomName);

            if(room != null) {
                room.add(address);
                return true;
            }
            else {
                List<Address> newRoom = new ArrayList<>();
                newRoom.add(address);
                rooms.put(roomName, newRoom);
                return false;
            }
        }
        else {
            throw new UserArlreadyInRoom();
        }
    }


    public void leaveRoom(Address address) throws UserNotInRoom {

        String room = users_connected.get(address);

        if(room != null) {
            users_connected.remove(address);
            List<Address> room_list = rooms.get(room);
            room_list.remove(address);
        }
        else {
            throw new UserNotInRoom();
        }

    }


    public List<Address> getUserRoom(Address user) throws UserNotInRoom {
        String room = users_connected.get(user);

        if(room != null) {
            return rooms.get(room);
        }
        else {
            throw  new UserNotInRoom();
        }
    }

    public List<Address> getRoomMembers(String roomName) {
        return rooms.get(roomName);
    }




}
