import algorithm.fitness.Fitness;
import algorithm.Neat;
import algorithm.models.forward.Node;
import visual.NeatGraph;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        double[][] inputs = new double[][]{
                {0, 0},
                {0, 1},
                {1, 0},
                {1, 1}
        };

        // initialize outputs for XOR
        double[] outputs = {
                0.0,
                1.0,
                1.0,
                0.0
        };

        Fitness fitness = (genome, solution) -> {
            ArrayList<Node>[] layers = genome.getLayers();
            double score = 4;
            for (int j = 0; j < inputs.length; j++) {
                double[] out = genome.computeOutput(layers[0], layers[1], layers[2], inputs[j]);
                score -= Math.abs(out[0] - outputs[j]);
            }
            return score;
        };

        Neat neat = Neat.builder().inputSize(2).outputSize(1).inputs(inputs).fitnessFunction(fitness).solutions(outputs).BIAS(true).build();
        neat.run();

        NeatGraph graph = new NeatGraph(neat.getBestGenome());
        graph.display();
    }
}