package algorithm.activation;

public class SteepenedSigmoid implements Activation {

    @Override
    public double activate(double x) {
        return 1 / (1 + Math.exp(-4.9 * x));
    }
}
