package linj.recommendation;

import java.util.Arrays;
import java.util.HashMap;

public class AppMF {
	
	public static String base_file = "";	// base data
    public static String test_file = "";	// test data
    public static int n = 0;	// number of users
	public static int m = 0;	// number of items
	
	public static String method = "PMF";	// PMF, RSVD
	public static int d = 20;	// The number of latent dimensions
	public static int T = 100;	// Iteration number
	public static float gamma = 0.9F;	// The learning rate
	
	public static float alphaU = 0;
	public static float alphaV = 0;
	public static float betaU = 0;
	public static float betaV = 0;
	
	public static HashMap<Integer, HashMap<Integer,Float>> rating_baseU2IR;
	public static HashMap<Integer, HashMap<Integer,Float>> rating_baseI2UR;
	public static HashMap<Integer, HashMap<Integer,Float>> rating_testU2IR;
	public static int[][] rating_testMt;
	public static float rating_average;
	public static int count_test_user;

	public static void main(String[] args) {
		
		for (int k=0; k < args.length; k++) 
        {
			if (args[k].equals("-fnTrainData")) base_file = args[++k];
    		else if (args[k].equals("-fnTestData")) test_file = args[++k];
    		else if (args[k].equals("-n")) n = Integer.parseInt(args[++k]);
    		else if (args[k].equals("-m")) m = Integer.parseInt(args[++k]);
    		else if (args[k].equals("-method")) method = args[++k];
    		else if (args[k].equals("-d")) d = Integer.parseInt(args[++k]);
    		else if (args[k].equals("-T")) T = Integer.parseInt(args[++k]);
			else if (args[k].equals("-gamma")) gamma = Float.parseFloat(args[++k]);
			else if (args[k].equals("-alphaU")) gamma = Float.parseFloat(args[++k]);
			else if (args[k].equals("-alphaV")) gamma = Float.parseFloat(args[++k]);
			else if (args[k].equals("-betaU")) gamma = Float.parseFloat(args[++k]);
			else if (args[k].equals("-betaV")) gamma = Float.parseFloat(args[++k]);
        }
		System.out.println(Arrays.toString(args));
    	System.out.println("base_file: " + base_file);
    	System.out.println("test_file: " + test_file);
    	System.out.println("n: " + Integer.toString(n));
    	System.out.println("m: " + Integer.toString(m)); 
    	System.out.println("method: " + method);
    	System.out.println("d: "+Integer.toString(d));
    	System.out.println("T: "+Integer.toString(T));
    	System.out.println("gamma: "+Float.toString(gamma));
		
    	ReadDataFile rdfB = new ReadDataFile(base_file);
    	rating_baseU2IR = rdfB.getRatingHashmapU2IR();
    	rating_baseI2UR = rdfB.getRatingHashmapI2UR();
    	rating_average = rdfB.getAverageRating();
    	ReadDataFile rdfT = new ReadDataFile(test_file);
		rating_testU2IR = rdfT.getRatingHashmapU2IR();
		rating_testMt = rdfT.getRatingMatrix();
    	count_test_user = rdfT.getUserCount();
		
		
		if(method.equals("PMF")) {
			double[][] rp_PMF = new ProbabilisticMF().getRatingMatrix();
			System.out.println("Method\tRMSE\tMAE\t");
			EvaluationMetrics em = new EvaluationMetrics(rp_PMF, rating_testMt);
			double mae = em.getMAE();
			double rmse = em.getRMSE();
			System.out.println("PMF\t" + rmse + "\t" + mae + "\t");
		}
		
		if(method.equals("RSVD")) {
			double[][] rp_RSVD = new RegularizedSVD().getRatingMatrix();
			System.out.println("Method\tRMSE\tMAE\t");
			EvaluationMetrics em = new EvaluationMetrics(rp_RSVD, rating_testMt);
			double mae = em.getMAE();
			double rmse = em.getRMSE();
			System.out.println("RSVD\t" + rmse + "\t" + mae + "\t");
		}
		
	}

}
