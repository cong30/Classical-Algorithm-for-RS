package linj.recommendation;

public class EvaluationMetrics {
	
	private int[][]	r_test;
	private double[][] r_pred;
	private int yRt;
	private double mae;
	private double rmse;
	
	public EvaluationMetrics(double[][] rPred, int[][] rTest) {
		
		r_pred = new double[AppMF.count_test_user][AppMF.m];
		r_test = new int[AppMF.count_test_user][AppMF.m];
		
		yRt = 0; mae = 0; rmse = 0;
		for(int i = 0; i < AppMF.count_test_user; i++) {
			for(int j = 0; j < AppMF.m; j++) {
				r_pred[i][j] = rPred[i][j];
				r_test[i][j] = rTest[i][j];
				if(r_pred[i][j] != 0 && r_test[i][j] != 0) {
					yRt++;
					mae += Math.abs(r_pred[i][j] -r_test[i][j]);
					rmse += Math.pow((r_pred[i][j] -r_test[i][j]), 2);
				}
			}
		}
		if(yRt++ != 0) {
			mae /= yRt;
			rmse /= yRt;
			rmse = Math.sqrt(rmse);
		} else {
			mae = -1;
			rmse = 0;
		}
	}
	
	// Mean Absolute Error
	public double getMAE() {
		
		return mae;
	}
	
	// Root Mean Square Error
	public double getRMSE() {
		
		return rmse;
	}

}
