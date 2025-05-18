package mediator;

import java.util.List;
import java.util.Map;

public class Broadcast {

    private final Map<String, Object> data;
    private final List<Long> receivers;

    public Broadcast(Map<String, Object> data, List<Long> receivers) {
        this.data = data;
        this.receivers = receivers;
    }

    public Broadcast(Map<String, Object> data, Long receiver) {
        this(data, List.of(receiver));
    }

    public Broadcast(Map<String, Object> data) {
        this(data, List.of());
    }

    public Map<String, Object> getData() {
        return data;
    }

    public List<Long> getReceivers() {
        return receivers;
    }
}
