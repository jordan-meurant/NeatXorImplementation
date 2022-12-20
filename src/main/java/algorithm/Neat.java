package algorithm;

import algorithm.activation.Activation;
import algorithm.activation.SteepenedSigmoid;
import algorithm.fitness.Fitness;
import algorithm.models.*;
import algorithm.models.forward.Node;
import algorithm.selection.Selection;
import algorithm.selection.WheelSelection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Neat {
    @Builder.Default
    private final int POPULATION_SIZE = 1000;
    @Builder.Default
    private final double STOP_CRITERIA = 3.6;

    @Builder.Default
    private final double MAX_ITERATIONS = 2000;
    @Builder.Default
    private final double CROSSOVER_RATE = 0.5;
    @Builder.Default
    private final double WEIGHT_MUTATION_RATE = 0.02;
    @Builder.Default
    private final double NODE_MUTATION_RATE = 0.03;
    @Builder.Default
    private final double CONNECTION_MUTATION_RATE = 0.05;
    @Builder.Default
    private final double ENABLE_MUTATION_RATE = 0.01;
    private final double[][] inputs;
    private final double[] solutions;
    @Builder.Default
    private Activation activation = new SteepenedSigmoid();
    @Builder.Default
    private HashMap<ConnectionGene, ConnectionGene> connectionsDictionary = new HashMap<>();
    @Builder.Default
    private RandomList<NodeGene> nodesDictionary = new RandomList<>();
    @Builder.Default
    private RandomList<Genome> population = new RandomList<>();
    @Builder.Default
    private int inputSize = 2, outputSize = 1;
    @Builder.Default
    private boolean BIAS = false;

    @Builder.Default
    private Selection selection = new WheelSelection();

    private Fitness fitnessFunction;

    private static double getNormalizedOutput(double score) {
        return score > 0.5 ? 1 : 0;
    }

    public void init() {
        // add input nodes
        for (int i = 0; i < inputSize; i++) {
            NodeGene n = getNode();
            n.setType(NodeType.INPUT);
            n.setLayer(0.1);
        }
        if (BIAS) {
            NodeGene n = getNode();
            n.setType(NodeType.BIAS);
            n.setLayer(0.1);
        }
        // add output nodes
        for (int i = 0; i < outputSize; i++) {
            NodeGene n = getNode();
            n.setType(NodeType.OUTPUT);
            n.setLayer(0.9);
        }

        // create initial population
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Genome g = getBasicGenome();
            g.connectInputsToOutputs();
            population.add(g);
        }
        // evaluate initial population
        evaluatePopulation(this.population, solutions);
    }

    public void run() {
        init();

        int iterationCount = 1;
        while (population.getBest(Genome::compareTo).getFitness() < STOP_CRITERIA && iterationCount < MAX_ITERATIONS) {
            iterationCount++;
            System.out.println("Iteration: " + iterationCount + " Best fitness: " + population.getBest(Genome::compareTo).getFitness() + " Number of nodes: " + nodesDictionary.size() + " Number of connections: " + connectionsDictionary.size());
            this.population = evolve(this.population);
        }


        System.out.println("DONE_AT_ITERATION_N° " + iterationCount);
        Genome best = population.getBest(Genome::compareTo);
        System.out.println("SCORE_FITTEST_GENOME ::" + best.getFitness());


        System.out.println("################### TESTING ###################");
        ArrayList<Node>[] layers = best.getLayers();
        System.out.println("################ NORMAL_OUTPUTS ###################");
        for (double[] input : inputs) {
            double[] out = best.computeOutput(layers[0], layers[1], layers[2], input);
            System.out.println("Input : " + Arrays.toString(input) + "  Output : " + out[0]);
        }
        System.out.println("################ NORMALIZED_OUTPUTS ###################");
        layers = best.getLayers();
        for (double[] input : inputs) {
            double[] out = best.computeOutput(layers[0], layers[1], layers[2], input);
            System.out.println("Input : " + Arrays.toString(input) + "  Output : " + getNormalizedOutput(out[0]));
        }
    }

    private RandomList<Genome> evolve(RandomList<Genome> population) {
        RandomList<Genome> newPopulation = new RandomList<>();
        // add best genome
        newPopulation.add(population.getBest(Genome::compareTo));
        for (int i = 1; i < population.size(); i++) {
            Genome parent1 = selection.select(population);
            Genome parent2 = selection.select(population);
            while (parent1.equals(parent2)) {
                parent2 = selection.select(population);
            }
            Genome offspring = crossOver(parent1, parent2);
            newPopulation.add(offspring);
        }

        for (int i = 1; i < newPopulation.size(); i++) {
            newPopulation.get(i).mutate();
        }
        evaluatePopulation(newPopulation, solutions);

        return newPopulation;
    }

    public Genome getBasicGenome() {
        Genome g = new Genome(this);
        int indexBias = 0;
        if (BIAS) indexBias = 1;
        for (int i = 0; i < inputSize + outputSize + indexBias; i++) {
            g.addNode(getNode(i + 1));
        }
        return g;
    }


    /**
     * Create a new connection and check if it already exists, if not add it to the dictionary
     */
    public ConnectionGene getConnection(NodeGene node1, NodeGene node2) {
        ConnectionGene connectionGene = new ConnectionGene(node1, node2);

        return checkIfConnectionExists(connectionGene);
    }

    public ConnectionGene checkIfConnectionExists(ConnectionGene connectionGene) {
        // verify if connection already exists
        if (connectionsDictionary.containsKey(connectionGene)) {
            connectionGene.setInnovationNumber(connectionsDictionary.get(connectionGene).getInnovationNumber());
        } else {
            connectionGene.setInnovationNumber(connectionsDictionary.size() + 1);
            connectionsDictionary.put(connectionGene, connectionGene);
        }
        return connectionGene;
    }

    public NodeGene getNode() {
        NodeGene nodeGene = new NodeGene(nodesDictionary.size() + 1);
        nodesDictionary.add(nodeGene);
        return nodeGene;
    }


    public NodeGene getNode(int id) {
        if (id <= nodesDictionary.size()) {
            return nodesDictionary.get(id - 1);
        }
        return getNode();
    }

    public List<ConnectionGene> findExcessAndDisjoint(Genome g1, Genome g2) {
        List<ConnectionGene> disjointNodes = new ArrayList<>();

        for (ConnectionGene c1 : g1.getConnectionGenes().getList()) {
            boolean found = false;
            for (ConnectionGene c2 : g2.getConnectionGenes().getList()) {
                if (c1.getInnovationNumber() == c2.getInnovationNumber()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                disjointNodes.add(c1);
            }
        }
        return disjointNodes;
    }

    public Genome crossOver(Genome parent1, Genome parent2) {

        Genome basicGenome = getBasicGenome();
        if (parent1.getFitness() < parent2.getFitness()) {
            Genome temp = parent1;
            parent1 = parent2;
            parent2 = temp;
        }

        for (ConnectionGene c : parent1.getConnectionGenes().getList()) {
            for (ConnectionGene c2 : parent2.getConnectionGenes().getList()) {
                if (c.getInnovationNumber() == c2.getInnovationNumber()) {
                    if (Math.random() < CROSSOVER_RATE) {
                        basicGenome.addConnection(c.cloneConnection());
                    } else {
                        basicGenome.addConnection(c.cloneConnection());
                    }
                }
            }
        }

        List<ConnectionGene> connectionGeneList = findExcessAndDisjoint(parent1, parent2);
        for (ConnectionGene c : connectionGeneList) {
            basicGenome.addConnection(c.cloneConnection());
        }


        for (ConnectionGene c : basicGenome.getConnectionGenes().getList()) {
            basicGenome.addNode(c.getFrom());
            basicGenome.addNode(c.getTo());
        }

        return basicGenome;
    }

    public Genome getBestGenome() {
        return population.getBest(Genome::compareTo);
    }

    public void evaluatePopulation(RandomList<Genome> population, double[] solutions) {
        for (Genome g : population.getList()) {
            g.setFitness(fitnessFunction.getFitness(g, solutions));
        }
    }


}
