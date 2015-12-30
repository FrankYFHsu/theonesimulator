package core;

import java.util.Random;

public class ExponentialRNG {
	
	private Random rng;
	private double lambda;	
	
	public ExponentialRNG(Random rng, double lambda){
		this.rng=rng;
		this.lambda=lambda;
				
	}
	
	public double nextExp(){
		
		return -1*Math.log(1-rng.nextDouble())/lambda;
	}
	
}
