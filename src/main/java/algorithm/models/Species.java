package algorithm.models;

import algorithm.Neat;
import algorithm.selection.Selection;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Species {

    private RandomList<Genome> genomes;

    private double averageFitness;

    public Species() {
        genomes = new RandomList<>();
    }

    public void addGenome(Genome genome) {
        genomes.add(genome);
    }

    public void removeGenome(Genome genome) {
        genomes.remove(genome);
    }

    public Genome getReferenceGenome() {
        return genomes.getBest(Genome::compareTo);
    }

    public void reproduce(Selection selection) {
        RandomList<Genome> newGenomes = new RandomList<>();
        for (int i = 0; i < genomes.size(); i++) {
            Genome parent1 = selection.select(genomes);
            Genome parent2 = selection.select(genomes);
            Genome child = crossover(parent1,parent2);
            child.mutate();
            newGenomes.add(child);
        }
        this.genomes = newGenomes;
    }


    public double compatibilityDistance(Genome g1, Genome g2) {
        int nbCommonGenes = 0;
        int sizeG1 = g1.getNumberOfConnections();
        int sizeG2 = g2.getNumberOfConnections();
        int N = Math.max(sizeG1, sizeG2);
        // special case when not many genes
        if (N < 20) {
            N = 1;
        }
        if (sizeG1 < sizeG2) {
            Genome temp = g1;
            g1 = g2;
            g2 = temp;
        }

        int indexG1 = 0;
        int indexG2 = 0;
        double excess = 0;
        double disjoint = 0;
        double weightDiff = 0.0;
        while (indexG1 < sizeG1 && indexG2 < sizeG2) {
            ConnectionGene gene1 = g1.getConnectionGene(indexG1);
            ConnectionGene gene2 = g2.getConnectionGene(indexG2);
            int in1 = gene1.getInnovationNumber();
            int in2 = gene2.getInnovationNumber();
            if (in1 == in2) {
                indexG1++;
                indexG2++;
                nbCommonGenes++;
                weightDiff += Math.abs(gene1.getWeight() - gene2.getWeight());
            } else if (in1 > in2) {
                indexG2++;
                disjoint++;
            } else {
                excess++;
                indexG1++;
            }
        }
        // calculate excess genes
        excess += sizeG1 - indexG1;


        return excess / N + disjoint / N + weightDiff / nbCommonGenes;
    }

    /**
     * Eliminate all genomes that are not in the top x% of the species
     *
     * @param percentage percentage of genomes to keep
     */
    public void cull(double percentage) {
        genomes.sort(Genome::compareTo);
        int nbToKeep = (int) (genomes.size() * percentage);
        genomes.getList().subList(0, nbToKeep);
    }

    public boolean isSimilar(Genome g) {
        return compatibilityDistance(g, getReferenceGenome()) < 3;
    }

    public void computeAverageFitness() {
        averageFitness = genomes.getList().stream().mapToDouble(Genome::getFitness).average().orElse(0);
    }

    public void computeAdjustedFitnessForGenomes() {
        genomes.getList().forEach(g -> g.setFitness(g.getFitness() / genomes.size()));
    }

    public Genome crossover(Genome parent1, Genome parent2) {
        Neat neat = parent1.getNeat();
        Genome basicGenome = neat.getBasicGenome();
        if (parent1.getFitness() < parent2.getFitness()) {
            Genome temp = parent1;
            parent1 = parent2;
            parent2 = temp;
        }

        for (ConnectionGene c : parent1.getConnectionGenes().getList()) {
            for (ConnectionGene c2 : parent2.getConnectionGenes().getList()) {
                if (c.getInnovationNumber() == c2.getInnovationNumber()) {
                    if (Math.random() < 0.5) {
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
}
