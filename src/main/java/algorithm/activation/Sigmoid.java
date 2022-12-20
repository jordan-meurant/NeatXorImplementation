package algorithm.activation;

public class Sigmoid implements Activation {

    @Override
    public double activate(double x) {
        return 1 / (1 + Math.exp(-x));
    }

}
