package algorithm.selection;

import algorithm.models.Genome;
import algorithm.models.RandomList;

public class WheelSelection implements Selection {

    @Override
    public Genome select(RandomList<Genome> population) {
        double r = Math.random() * getSumAllFitness(population);
        double sumFitness = 0;
        int index = 0;

        for (int i = 0; i < population.size(); i++) {
            if (sumFitness >= r) {
                index = i;
                break;
            } else {

                sumFitness += population.get(i).getFitness();
            }
        }
        return population.get(index);
    }

    private double getSumAllFitness(RandomList<Genome> population) {
        double sum = 0;
        for (int i = 0; i < population.size(); i++) {
            sum += population.get(i).getFitness();
        }
        return sum;
    }


}
