package algorithm.fitness;

import algorithm.models.Genome;

public interface Fitness {

    double getFitness(Genome genome, double[] solution);
}
