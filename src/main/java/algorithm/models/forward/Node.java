package algorithm.models.forward;

import algorithm.activation.Activation;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class Node implements Comparable<Node> {

    private final Activation activation;
    private double layer;
    private double output;
    private ArrayList<Connection> connections = new ArrayList<>();

    public Node(double layer, Activation activation) {
        this.layer = layer;
        this.activation = activation;
    }

    public void calculate() {
        double s = 0;
        for(Connection c:connections){
            if(c.isEnabled()){
                s += c.getWeight() * c.getFrom().getOutput();
            }
        }

        output = this.activation.activate(s);
    }

    public void addConnection(Connection connection){
        connections.add(connection);
    }

    public ArrayList<Connection> getConnections() {
        return connections;
    }


    @Override
    public int compareTo(Node o) {
        return Double.compare(o.layer, this.layer);
    }
}
