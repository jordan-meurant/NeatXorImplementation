package algorithm.models;

import algorithm.activation.Activation;
import algorithm.Neat;
import algorithm.models.forward.Connection;
import lombok.Getter;
import lombok.Setter;
import algorithm.models.forward.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

@Getter
@Setter
public class Genome implements Comparable<Genome> {

    private final Neat neat;
    private RandomList<ConnectionGene> connectionGenes = new RandomList<>();
    private RandomList<NodeGene> nodeGenes = new RandomList<>();

    private double fitness = 0;

    private Activation activation;


    public Genome(Neat neat) {
        this.neat = neat;
        this.activation = neat.getActivation();
    }

    public void mutate() {
        if (Math.random() < neat.getCONNECTION_MUTATION_RATE()) {
            mutateAddConnection();
        }
        if (Math.random() < neat.getNODE_MUTATION_RATE()) {
            mutateAddNode();
        }
        if (Math.random() < neat.getENABLE_MUTATION_RATE()) {
            mutateEnableConnection();
        }
        if (Math.random() < neat.getWEIGHT_MUTATION_RATE()) {
            mutateWeightRandomly();
        }
    }

    public void mutateAddNode() {

        // pick a random connection
        ConnectionGene randomConnection = connectionGenes.getRandom();
        while (randomConnection.getFrom().getType() == NodeType.BIAS || randomConnection.getFrom().getType() == NodeType.OUTPUT) {
            randomConnection = connectionGenes.getRandom();
        }
        // create a new node
        NodeGene newNode = neat.getNode();
        newNode.setType(NodeType.HIDDEN);
        newNode.setLayer(randomConnection.getMiddleLayer());

        nodeGenes.add(newNode);
        // create two new connections
        ConnectionGene connectionGene1 = neat.getConnection(randomConnection.getFrom(), newNode);
        ConnectionGene connectionGene2 = neat.getConnection(newNode, randomConnection.getTo());
        connectionGene1.setWeight(1.0);
        connectionGene2.setWeight(randomConnection.getWeight());
        connectionGene2.setEnabled(randomConnection.isEnabled());

        connectionGenes.add(connectionGene1);
        connectionGenes.add(connectionGene2);
        connectionGenes.remove(randomConnection);
    }

    /**
     * Connects inputs and bias nodes to output nodes
     */
    public void connectInputsToOutputs() {
        var outputs = nodeGenes.getList().stream().filter(nodeGene -> nodeGene.getType() == NodeType.OUTPUT).toList();
        for (NodeGene output : outputs) {
            for (NodeGene node : nodeGenes.getList()) {
                if (node.getType() == NodeType.INPUT) {
                    ConnectionGene con = neat.getConnection(node, output);
                    con.setWeight(randomWeight());
                    connectionGenes.add(con);
                }
                if (node.getType() == NodeType.BIAS) {
                    ConnectionGene con = neat.getConnection(node, output);
                    con.setWeight(1);
                    connectionGenes.add(con);

                }
            }
        }
    }

    public void mutateAddConnection() {
        boolean connectionAdded = false;
        int stopCounter = 0;
        do {
            NodeGene from = nodeGenes.getRandom();
            NodeGene to = nodeGenes.getRandom();

            while (from.getLayer() > to.getLayer() ||from.getType() == NodeType.BIAS || to.getType() == NodeType.BIAS) {
                from = nodeGenes.getRandom();
                to = nodeGenes.getRandom();
            }
            // invert nodes if from node is in bigger layer
            if (from.getLayer() == to.getLayer()) {
                continue;
            }
            // create connection and check if
            ConnectionGene con = neat.getConnection(from, to);
            con.setWeight(randomWeight());

            // add connection
            connectionGenes.add(con);
            connectionAdded = true;
        } while (!connectionAdded && stopCounter++ < 10);

    }

    public void mutateWeightRandomly() {

        ConnectionGene connectionGene = connectionGenes.getRandom();
        while (connectionGene.getFrom().getType() == NodeType.BIAS) {
            connectionGene = connectionGenes.getRandom();
        }
        connectionGene.setWeight(randomWeight());
    }

    public void mutateEnableConnection() {
        ConnectionGene connectionGene = connectionGenes.getRandom();
        while (connectionGene.getFrom().getType() == NodeType.BIAS) {
            connectionGene = connectionGenes.getRandom();
        }
        connectionGene.setEnabled(!connectionGene.isEnabled());
    }


    /**
     * @return random weight between -2 and 2
     */
    private double randomWeight() {

        return new Random().nextDouble() * 4 - 2;
    }


    public ConnectionGene getConnectionGene(int index) {
        return connectionGenes.get(index);
    }

    public void addNode(NodeGene nodeGene) {
        nodeGenes.add(nodeGene);
    }

    public void addConnection(ConnectionGene connectionGene) {
        connectionGenes.add(connectionGene);
    }

    public int getNumberOfConnections() {
        return connectionGenes.size();
    }


    // compute the output of the network
    public double[] computeOutput(ArrayList<Node> inputsNodes, ArrayList<Node> hiddenNodes, ArrayList<Node> outputNodes, double... inputs) {
        double[] output = new double[neat.getOutputSize()];
        for (int i = 0; i < inputs.length; i++) {
            inputsNodes.get(i).setOutput(inputs[i]);
        }

        hiddenNodes.forEach(Node::calculate);
        outputNodes.forEach(Node::calculate);

        for (int i = 0; i < output.length; i++) {
            output[i] = outputNodes.get(i).getOutput();
        }

        return output;
    }

    @Override
    public int compareTo(Genome g) {
        return Double.compare(g.getFitness(), this.getFitness());
    }

    @Override
    public String toString() {
        return "Genome{" +
                "connectionGenes=" + connectionGenes +
                ", fitness=" + fitness +
                '}';
    }


    /**
     * @return 3 lists of nodes : input, hidden and output nodes
     */
    public ArrayList<Node>[] getLayers() {
        HashMap<Integer, Node> nodeHashMap = new HashMap<>();
        ArrayList<Node> input_nodes = new ArrayList<>();
        ArrayList<Node> hidden_nodes = new ArrayList<>();
        ArrayList<Node> output_nodes = new ArrayList<>();


        for (NodeGene n : nodeGenes.getList()) {

            Node node = new Node(n.getLayer(), this.activation);
            nodeHashMap.put(n.getInnovationNumber(), node);
            if (n.getType() == NodeType.INPUT) {
                input_nodes.add(node);
            } else if (n.getType() == NodeType.OUTPUT) {
                output_nodes.add(node);
            } else {
                hidden_nodes.add(node);
            }
        }

        // sort hidden nodes by layer before compute output
        hidden_nodes.sort(Node::compareTo);


        forwardConnections(nodeHashMap);


        ArrayList<Node>[] layers = new ArrayList[3];
        layers[0] = input_nodes;
        layers[1] = hidden_nodes;
        layers[2] = output_nodes;

        return layers;
    }

    private void forwardConnections(HashMap<Integer, Node> nodeHashMap) {
        for (ConnectionGene c : connectionGenes.getList()) {
            NodeGene from = c.getFrom();
            NodeGene to = c.getTo();

            Node node_from = nodeHashMap.get(from.getInnovationNumber());
            Node node_to = nodeHashMap.get(to.getInnovationNumber());

            Connection con = new Connection(node_from, node_to);
            con.setWeight(c.getWeight());
            con.setEnabled(c.isEnabled());
            node_to.addConnection(con);
        }
    }

}
