package visual;

import algorithm.models.ConnectionGene;
import algorithm.models.Genome;
import algorithm.models.NodeGene;
import algorithm.models.NodeType;
import org.graphstream.graph.implementations.MultiGraph;

import java.util.ArrayList;


public class NeatGraph {

    private static final String STYLE_SHEET =
            "node { fill-color: black; size: 30px; text-size:25;}"
                    + "node.marked {" +
                    "	fill-color: red;" +
                    "}" + "node.output{fill-color: red;}"
                    + "node.input{fill-color: blue;}"
                    + "node.hid{stroke-color: green; size:30px;}"
                    + "edge { fill-color: black; size: 3px; text-size:15;}"
                    + "node.biais{fill-color: purple;}";
    private final Genome genome;
    private final MultiGraph graph;

    public NeatGraph(Genome genome) {
        this.genome = genome;
        this.graph = new MultiGraph("Neat");
        graph.setAttribute("ui.stylesheet", STYLE_SHEET);
        graph.setAutoCreate(true);
        graph.setStrict(false);
    }

    public void display() {
        addNodes(genome.getNodeGenes().getList());
        addEdges(genome.getConnectionGenes().getList());
        graph.display();
    }

    private String getFormattedWeight(double weight){
        return String.format("%.2f", weight);
    }

    private void addEdges(ArrayList<ConnectionGene> connections) {
        connections.sort(ConnectionGene::compareTo);
        connections.forEach(connection -> {
            var n1 = graph.getNode(connection.getFrom().getInnovationNumber() + "");
            var n2 = graph.getNode(connection.getTo().getInnovationNumber() + "");
            var e = graph.addEdge(connection.getInnovationNumber() + "", n1, n2, true);
            e.setAttribute("ui.label", getFormattedWeight(connection.getWeight()));
            if (connection.isEnabled()) {
                e.setAttribute("ui.style", "fill-color: green;");
            } else {
                e.setAttribute("ui.style", "fill-color: red;");
            }
        });
    }

    private void addNodes(ArrayList<NodeGene> nodes) {
        for (NodeGene node : nodes) {
            var n = graph.addNode(node.getInnovationNumber() + "");
            n.setAttribute("ui.label", node.getInnovationNumber() + "");
            if (node.getType() == NodeType.INPUT) {
                n.setAttribute("ui.class", "input");
                n.addAttribute("layout.frozen");
                 n.addAttribute("xy", node.getLayer() * node.getInnovationNumber() * 10,-1);
            } else if (node.getType() == NodeType.OUTPUT) {
                n.setAttribute("ui.class", "output");
                n.setAttribute("layout.weight", 10);
               n.addAttribute("layout.frozen");
                 n.addAttribute("y", 1);
            } else if (node.getType()==NodeType.BIAS){
                n.setAttribute("ui.class", "biais");
            } else {
                n.setAttribute("ui.class", "hid");
            }
        }
    }

}