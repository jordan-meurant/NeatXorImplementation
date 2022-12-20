package algorithm.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class ConnectionGene extends Gene implements Comparable<ConnectionGene> {


    private NodeGene from;
    private NodeGene to;

    private double weight;

    private boolean enabled = true;

    public ConnectionGene(NodeGene from, NodeGene to){
        this.from = from;
        this.to = to;
    }

    public ConnectionGene(int innovationNumber) {
        super(innovationNumber);
    }

    public boolean equals(Object o){
        if(!(o instanceof ConnectionGene)) return false;
        ConnectionGene c = (ConnectionGene) o;
        return (from.equals(c.from) && to.equals(c.to));
    }

    @Override
    public String toString() {
        return "models.ConnectionGene{" +
                "from=" + from.getInnovationNumber() +
                ", to=" + to.getInnovationNumber() +
                ", weight=" + weight +
                ", enabled=" + enabled +
                ", innovation_number=" + innovationNumber +
                '}';
    }

    public ConnectionGene cloneConnection() {
        ConnectionGene clone = new ConnectionGene(this.from, this.to);
        clone.setInnovationNumber(this.innovationNumber);
        clone.setEnabled(this.enabled);
        clone.setWeight(this.weight);
        return clone;
    }

    // get middle layer of connection
    public double getMiddleLayer(){
        return (from.getLayer() + to.getLayer()) / 2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public int compareTo(ConnectionGene o) {
        return Integer.compare(innovationNumber, o.innovationNumber);
    }
}
