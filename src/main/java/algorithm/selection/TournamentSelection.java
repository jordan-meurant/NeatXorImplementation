package algorithm.selection;

import algorithm.models.Genome;
import algorithm.models.RandomList;

public class TournamentSelection implements Selection {

    private final int sizeTournament;

    public TournamentSelection(int sizeTournament) {
        this.sizeTournament = sizeTournament;
    }

    @Override
    public Genome select(RandomList<Genome> population) {
        RandomList<Genome> tournament = new RandomList<>();

        for (int i = 0; i < sizeTournament; i++) {
            int randomIndex = (int) (Math.random() * sizeTournament);
            Genome selectedChromosome = population.get(randomIndex);
            tournament.add(selectedChromosome);
        }

        return tournament.getBest(Genome::compareTo);
    }

}

