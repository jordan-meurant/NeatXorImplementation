package algorithm.models.forward;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Connection {

    private Node from;
    private Node to;

    private double weight;
    private boolean enabled = true;

    public Connection(Node from, Node to) {
        this.from = from;
        this.to = to;
    }

}
