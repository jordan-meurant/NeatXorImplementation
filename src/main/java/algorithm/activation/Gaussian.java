package algorithm.activation;

public class Gaussian implements Activation {

    @Override
    public double activate(double x){
        return Math.pow(Math.E, -Math.pow(x, 2));
    }
}