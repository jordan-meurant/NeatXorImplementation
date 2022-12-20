package algorithm.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class NodeGene extends Gene implements Comparable<NodeGene> {

    private double layer;
    private double output;

    private NodeType type;
    private ArrayList<ConnectionGene> connectionGenes = new ArrayList<>();

    public NodeGene(int innovationNumber, double layer, NodeType type) {
        super(innovationNumber);
        this.layer = layer;
        this.type = type;
    }

    public NodeGene(int innovationNumber) {
        super(innovationNumber);
    }

    public void addConnectionGene(ConnectionGene connectionGene) {
        connectionGenes.add(connectionGene);
    }

    public int getNumberOfConnections() {
        return connectionGenes.size();
    }


    // calculate output of node based on input from other nodes and weights of connections to this node from other nodes
    public void calculateOutput() {
        double s = 0;

        for (ConnectionGene c : connectionGenes) {
            if (c.isEnabled()) {
                s += c.getWeight() * c.getFrom().getOutput();
            }
        }


        output = sigmoid(s);

    }

    private double sigmoid(double s) {
        return 1d / (1 + Math.exp(-s));
    }


    @Override
    public boolean equals(Object o){
        if(!(o instanceof NodeGene)) return false;
        return innovationNumber == ((NodeGene) o).getInnovationNumber();
    }

    @Override
    public int hashCode() {
        return innovationNumber;
    }

    @Override
    public String toString() {
        return "NodeGene{" +
                "layer=" + layer +
                ", type=" + type +
                ", innovationNumber=" + innovationNumber +
                '}';
    }

    @Override
    public int compareTo(NodeGene o) {
        return Double.compare(o.layer, this.layer);
    }
}
