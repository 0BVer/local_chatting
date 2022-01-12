package chat;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class CLIENT_MODEL {

    Set<user_DATA> user_CACHE = new TreeSet<>();
    Participant participant;

    CLIENT_MODEL() {
        participant = new Participant();
    }

}

class Participant {
    Set<user_DATA> user_LIST = new TreeSet<user_DATA>();

    Optional<user_DATA> search_by_INDEX(int index) {
        return user_LIST.stream().filter(i -> i.INDEX_ == index).findFirst();
    }

    Optional<user_DATA> search_by_ID(String id) {
        return user_LIST.stream().filter(i -> i.ID_ == id).findFirst();
    }

    Optional<user_DATA> search_by_NN(String nn) {
        return user_LIST.stream().filter(i -> i.NN_ == nn).findFirst();
    }
}