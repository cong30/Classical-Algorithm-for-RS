package linj.recommendation;

import java.util.HashMap;
import java.util.Random;

public class ProbabilisticMF {
	
	private HashMap<Integer, HashMap<Integer,Float>> rating_predU2IR = 
			new HashMap<Integer, HashMap<Integer,Float>>();
	private double[][] rating_predMt = new double[AppMF.n][AppMF.m];
	
	public ProbabilisticMF() {
		
		int USER_COUNT = AppMF.n;
		int ITEM_COUNT = AppMF.m;
		int DIMENSION_NUM = AppMF.d;
		int ITERATION_NUM = AppMF.T;
		float GAMMA = AppMF.gamma;
		float ALPHA_U = AppMF.alphaU;
		float ALPHA_V = AppMF.alphaV;
		HashMap<Integer, HashMap<Integer,Float>> rBaseU2IR = AppMF.rating_baseU2IR;
		
		// Initialize model parameters
		double[][] user_space = new double[USER_COUNT][DIMENSION_NUM];
		double[][] item_space = new double[ITEM_COUNT][DIMENSION_NUM];
		Random rand = new Random();;
		for(int i = 0; i < USER_COUNT; i++){
			for (int k = 0 ; k < DIMENSION_NUM; k++){
				user_space[i][k] = (rand.nextDouble() - 0.5) * 0.01;
			}
		}
		for(int j = 0; j < ITEM_COUNT; j++){
			for (int k = 0 ; k < DIMENSION_NUM; k++){
				item_space[j][k] = (rand.nextDouble() - 0.5) * 0.01;
			}
		}
		
		// Iteration
		for (int t = 0; t < ITERATION_NUM; t++){
			
			double learningRate = GAMMA;
//			long timeStart = System.currentTimeMillis();
			
			for(int i = 0; i <= USER_COUNT; i++)
			{
				if (!rBaseU2IR.containsKey(i)) {
					continue;
				}
					
				HashMap<Integer,Float> rI2R = rBaseU2IR.get(i);
				for(int j : rI2R.keySet()) {
					double s = 0;
					for(int k1 = 0; k1 < DIMENSION_NUM; k1++) {
						s += (user_space[i][k1] * item_space[j][k1]);
					}
					double err = rI2R.get(j) - s;
					for(int k2 = 0 ; k2 < DIMENSION_NUM; k2++){
						double grad_u = - err * item_space[j][k2] + ALPHA_U * user_space[i][k2];
						double grad_i = - err * user_space[i][k2] + ALPHA_V * item_space[j][k2];
						user_space[i][k2] = user_space[i][k2] - learningRate * grad_u;
						item_space[j][k2] = item_space[j][k2] - learningRate * grad_i;
					}	
				}
			}
			
//			long timeSpent = System.currentTimeMillis() - timeStart;
			learningRate *= 0.9;
//			System.out.println("loop:" + t + " finished~  Time spent: " + (timeSpent / 1000.0) + "  next alpha :" + learningRate);
		}
		
		for(int i = 0; i < USER_COUNT; i++) {
			for(int j = 0; j < ITEM_COUNT; j++) {
				rating_predMt[i][j] = 0;
				if(rBaseU2IR.containsKey(i) && rBaseU2IR.get(i).get(j) == null) {
					for(int k = 0; k < DIMENSION_NUM; k++) {
						rating_predMt[i][j] += (user_space[i][k] * item_space[j][k]);
//						System.out.print("" + rating_predMt[i][j] + " ");
					}
				} else if(rBaseU2IR.containsKey(i)){
					rating_predMt[i][j] = rBaseU2IR.get(i).get(j);
				}
				
			}
//			System.out.println("");
		}
	}
	
	public HashMap<Integer, HashMap<Integer,Float>> getRatingHashmapU2IR() {
		
		return rating_predU2IR;
	}
	
	public double[][] getRatingMatrix() {
		
		return rating_predMt;
	}

}
