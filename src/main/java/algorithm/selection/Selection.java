package algorithm.selection;

import algorithm.models.Genome;
import algorithm.models.RandomList;

public interface Selection {
    Genome select(RandomList<Genome> population);
}
