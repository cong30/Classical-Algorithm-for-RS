package CF_teacher;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// javac CFlowMemory.java

// 1. User-based CF
// java -Xmx2048m CFlowMemory -lambdaUCF 1 -KNNneighborNum 0 -fnTrainData u1.base -fnTestData u1.test -n 943 -m 1682

// 2. Item-based CF
// java -Xmx2048m CFlowMemory -lambdaUCF 0 -KNNneighborNum 0 -fnTrainData u1.base -fnTestData u1.test -n 943 -m 1682

// 3. Hybrid CF
// java -Xmx2048m CFlowMemory -lambdaUCF 0.5 -KNNneighborNum 0 -fnTrainData u1.base -fnTestData u1.test -n 943 -m 1682

// --- CF for numerical rating prediction
public class CFlowMemory
{
	// === Input data files
    public static String fnTrainData = "";
    public static String fnTestData = "";
    public static float lambdaUCF = 1; // 1:UCF; 0:ICF; (0,1):hybrid CF
    
    // ===
    public static int n = 0; // number of users
	public static int m = 0; // number of items
	public static int num_train = 0; // number of the total (user, item) pairs in training data
	public static int num_test = 0; // number of the total (user, item) pairs in training data
	public static float minRating = 1;
	public static float maxRating = 5;

	// === Evaluation
	public static int KNNneighborNum = 0; // 0: all neighbors

	// === data structure
	public static HashMap<Integer, HashMap<Integer,Float>> TrainData = new HashMap<Integer, HashMap<Integer,Float>>();
	public static HashMap<Integer, HashMap<Integer,Float>> TrainDataItem2User = new HashMap<Integer, HashMap<Integer,Float>>();
	public static HashMap<Integer, HashMap<Integer,Float>> TestData = new HashMap<Integer, HashMap<Integer,Float>>();
	public static HashMap<Integer, HashMap<Integer,Float>> TestDataItem2User = new HashMap<Integer, HashMap<Integer,Float>>();

	// --- user->(item,rating)
	public static HashMap<Integer, HashMap<Integer,Float>> PredictionUCF = new HashMap<Integer, HashMap<Integer,Float>>();
	// --- user->(item,rating)
	public static HashMap<Integer, HashMap<Integer,Float>> PredictionICF = new HashMap<Integer, HashMap<Integer,Float>>();
	
	// === some statistics
	public static int[] userRatingNumTrain; // start from index "1"
	public static float[] userRatingAvgTrain; // start from index "1"
	public static int[] itemRatingNumTrain; // start from index "1"
	public static float[] itemRatingAvgTrain; // start from index "1"	
	public static float g_avg = 0; // global average
	
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static void main(String []args) throws Exception
	{
		// ------------------------------
		// === Read the configurations
		for (int k=0; k < args.length; k++) 
        {
			if (args[k].equals("-fnTrainData")) fnTrainData = args[++k];
    		else if (args[k].equals("-fnTestData")) fnTestData = args[++k];
    		else if (args[k].equals("-n")) n = Integer.parseInt(args[++k]);
    		else if (args[k].equals("-m")) m = Integer.parseInt(args[++k]);	
    		else if (args[k].equals("-KNNneighborNum")) KNNneighborNum = Integer.parseInt(args[++k]);
			else if (args[k].equals("-lambdaUCF")) lambdaUCF = Float.parseFloat(args[++k]);
        }
 
        // ------------------------------  
    	// === Print the configurations
		System.out.println(Arrays.toString(args));
    	System.out.println("fnTrainData: " + fnTrainData);
    	System.out.println("fnTestData: " + fnTestData);
    	System.out.println("n: " + Integer.toString(n));
    	System.out.println("m: " + Integer.toString(m)); 
    	System.out.println("KNNneighborNum: "+Integer.toString(KNNneighborNum));
    	System.out.println("lambdaUCF: "+Float.toString(lambdaUCF));
    	// ------------------------------
    	
    	// ------------------------------
       	userRatingNumTrain = new int[n+1]; 	
       	userRatingAvgTrain = new float[n+1];
       	
       	itemRatingNumTrain = new int[m+1];       	
       	itemRatingAvgTrain = new float[m+1];       	
       	// ------------------------------
       	
    	// ------------------------------
        // === Read data
	    long TIME_START_READ_DATA = System.currentTimeMillis();
		readDataTrainTestValid();
		long TIME_FINISH_READ_DATA = System.currentTimeMillis();
    	System.out.println("Elapsed Time (read data):" + 
    				Float.toString((TIME_FINISH_READ_DATA-TIME_START_READ_DATA)/1000F)
    				+ "s");
    	// ------------------------------
    	    	
    	// ------------------------------
    	// === Get the average rating of each user and item
    	for(int userID=1; userID<=n; userID++)
    	{
    		if (userRatingNumTrain[userID]>0)
    		{
    			userRatingAvgTrain[userID] = userRatingAvgTrain[userID] / userRatingNumTrain[userID];
    		}
    		else
    		{
    			userRatingAvgTrain[userID] = g_avg;
    		}
    	}
    	// ---
    	for(int itemID=1; itemID<=m; itemID++)
    	{
    		if (itemRatingNumTrain[itemID]>0)
    		{
    			itemRatingAvgTrain[itemID] = itemRatingAvgTrain[itemID] / itemRatingNumTrain[itemID];
    		}
    		else
    		{
    			itemRatingAvgTrain[itemID] = g_avg;
    		}
    	}
    	// ------------------------------
    	
    	// ------------------------------ 
    	System.out.println( "num_train: " + Integer.toString(num_train) );
    	System.out.println( "num_test: " + Integer.toString(num_test) );
    	System.out.println( "g_avg: " + Float.toString(g_avg) );    	
    	System.out.println( "minRating: " + Float.toString(minRating) );
    	System.out.println( "maxRating: " + Float.toString(maxRating) );
    	// ------------------------------

    	// ------------------------------
    	// === Evaluation
    	if (fnTestData.length()>0)
    	{
	    	long TIME_START_TEST = System.currentTimeMillis();
	    	CF_Evaluation();	    	
			long TIME_FINISH_TEST = System.currentTimeMillis();
			System.out.println("Elapsed Time (test):" + 
						Float.toString((TIME_FINISH_TEST-TIME_START_TEST)/1000F)
						+ "s");
    	}
		// ------------------------------
	}
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void readDataTrainTestValid() throws Exception
    {
    	// ----------------------------------------------------
    	BufferedReader br = new BufferedReader(new FileReader(fnTrainData));
    	String line = null; 
    	double rating_sum = 0;
		while ((line = br.readLine())!=null)
    	{    		
    		String[] terms = line.split("\\s+|,|;");
    		int userID = Integer.parseInt(terms[0]);
    		int itemID = Integer.parseInt(terms[1]);
    		float rating = Float.parseFloat(terms[2]);
    		
    		//
    		rating_sum += rating;
    		num_train += 1;

    		// 
    		userRatingAvgTrain[userID] += rating;
    		userRatingNumTrain[userID] += 1;

    		// 
    		itemRatingAvgTrain[itemID] += rating;
    		itemRatingNumTrain[itemID] += 1;

    		//
    		if(rating>maxRating)
    		{
    			maxRating=rating;    				
    		}
    		if(rating<minRating)
    		{
    			minRating=rating;
    		}
    		
    		// TrainData: user->(item,rating)
    		if(TrainData.containsKey(userID))
    		{
    			HashMap<Integer,Float> item2rating = TrainData.get(userID);
    			item2rating.put(itemID,rating);
    			TrainData.put(userID,item2rating);
    		}
    		else
    		{
    			HashMap<Integer,Float> item2rating = new HashMap<Integer,Float>();
    			item2rating.put(itemID,rating);
    			TrainData.put(userID,item2rating);
    		}

    		// TrainDataItem2User: item->(user,rating)
    		if(TrainDataItem2User.containsKey(itemID))
    		{
    			HashMap<Integer,Float> user2rating = TrainDataItem2User.get(itemID);    	    	
    			user2rating.put(userID,rating);
    			TrainDataItem2User.put(itemID,user2rating);
    		}
    		else
    		{
    			HashMap<Integer,Float> user2rating = new HashMap<Integer,Float>();
    			user2rating.put(userID,rating);
    			TrainDataItem2User.put(itemID,user2rating);
    		}
    	}		
		g_avg = (float) (rating_sum/num_train);
    	br.close();
    	
    	// ----------------------------------------------------
    	if (fnTestData.length()>0)
    	{
	    	br = new BufferedReader(new FileReader(fnTestData));
	    	line = null;
	    	while ((line = br.readLine())!=null)
	    	{
	    		String[] terms = line.split("\\s+|,|;");
	    		int userID = Integer.parseInt(terms[0]);
	    		int itemID = Integer.parseInt(terms[1]);
	    		float rating = Float.parseFloat(terms[2]);

	    		// ---
	    		num_test +=1;
	    		
				// TestData: user->(item,rating)
				if(TestData.containsKey(userID))
		    	{
		    		HashMap<Integer,Float> item2rating = TestData.get(userID);
		    		item2rating.put(itemID,rating);
		    		TestData.put(userID,item2rating);
		    	}
		    	else
		    	{
		    		HashMap<Integer,Float> item2rating = new HashMap<Integer,Float>();
		    		item2rating.put(itemID,rating);
		    		TestData.put(userID,item2rating);
		    	}	
				
				if(lambdaUCF<1)
				{
					// TestDataItem2User: item->(user,rating)
		    		if(TestDataItem2User.containsKey(itemID))
		    		{
		    			HashMap<Integer,Float> user2rating = TestDataItem2User.get(itemID);    	    	
		    			user2rating.put(userID,rating);
		    			TestDataItem2User.put(itemID,user2rating);
		    		}
		    		else
		    		{
		    			HashMap<Integer,Float> user2rating = new HashMap<Integer,Float>();
		    			user2rating.put(userID,rating);
		    			TestDataItem2User.put(itemID,user2rating);
		    		}
				}
	    	}
	    	br.close();
    	}
    	// ----------------------------------------------------  	
    }
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
    
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void CF_Evaluation()
    {
    	int num_testcase = 0;
		float mae_sum = 0;
		float rmse_sum = 0;
    	
		// --- Prediction
    	if(lambdaUCF==1)
    	{
    		UCF_Prediction(TestData,PredictionUCF);
    	}
    	if(lambdaUCF==0)
    	{
    		ICF_Prediction(TestDataItem2User,PredictionICF);
    	}
    	if(lambdaUCF>0 && lambdaUCF<1)
    	{
    		UCF_Prediction(TestData,PredictionUCF);
    		ICF_Prediction(TestDataItem2User,PredictionICF);
    	}
    	
    	// --- Evaluation
    	for(int u: TestData.keySet())
		{
			// ---
			HashMap<Integer,Float> item2rating_u_Prediction = new HashMap<Integer,Float>();
			if(lambdaUCF==1)
			{
				item2rating_u_Prediction = PredictionUCF.get(u);
			}
			if(lambdaUCF==0)
			{
				item2rating_u_Prediction = PredictionICF.get(u);
			}
			if(lambdaUCF>0 && lambdaUCF<1)
			{
				item2rating_u_Prediction = PredictionUCF.get(u);
				HashMap<Integer,Float> item2rating_u_Prediction_ICF = PredictionICF.get(u);
				
				for(int j: item2rating_u_Prediction.keySet())
				{
					float r_uj_prediction_UCF = item2rating_u_Prediction.get(j);
					float r_uj_prediction_ICF = item2rating_u_Prediction_ICF.get(j);
					float r_uj_prediction = lambdaUCF*r_uj_prediction_UCF + (1-lambdaUCF)*r_uj_prediction_ICF;
					item2rating_u_Prediction.put(j, r_uj_prediction);
				}
			}
			
			// ---
			HashMap<Integer,Float> item2rating_u_TestData = TestData.get(u);
			// --- MAE, RMSE // !!! We may use some other evaluation metrics
			for(int j: item2rating_u_TestData.keySet())
			{
				//
				float r_uj = item2rating_u_TestData.get(j);
				float r_uj_prediction = item2rating_u_Prediction.get(j);				
				//
				mae_sum += Math.abs(r_uj-r_uj_prediction);
				rmse_sum += (r_uj-r_uj_prediction)*(r_uj-r_uj_prediction);			
				num_testcase += 1;
			}
		}
    	
    	// --- output
		float mae = mae_sum / num_testcase;
		float rmse = (float) Math.sqrt(rmse_sum/num_testcase);
		System.out.println("num_testcase:" + num_testcase);
		System.out.println("MAE:" + mae);
		System.out.println("RMSE: " + rmse);	
    }
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    	
    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static void UCF_Prediction(HashMap<Integer,HashMap<Integer,Float>> TestData,
				               HashMap<Integer,HashMap<Integer,Float>> PredictionUCF)
	{
		for(int u=1; u<=n; u++)
		{
			// --- check whether the user $u$ is in the test set
			if (!TestData.containsKey(u))
				continue;
			HashMap<Integer,Float> item2rating_u_TestData = TestData.get(u);
			
			// --------------------------------------------
			// --- Step 1. (user,user) similarity in User-based CF
			HashMap<Integer,Float> user2similarity_u_all = new HashMap<Integer,Float>();			
			calculateUserUserSimilarity(user2similarity_u_all, u);
			// --------------------------------------------
			
			// --------------------------------------------
			// --- Step 2. Prediction
			for(int j : item2rating_u_TestData.keySet())
			{
				float r_uj_prediction = 0;

				// --- Prediction via UCF
				r_uj_prediction = UCF_PredictionRule(user2similarity_u_all, u, j);
								
				// --- Step 3. post-processing
				if(r_uj_prediction>maxRating)
				{					
					r_uj_prediction=maxRating;
				}
				if(r_uj_prediction<minRating)
				{
					r_uj_prediction=minRating;
				}

				// --- Step 4. save the prediction
				if(PredictionUCF.containsKey(u))
				{
					HashMap<Integer,Float> prediction_u = PredictionUCF.get(u);
					prediction_u.put(j, r_uj_prediction);
					PredictionUCF.put(u, prediction_u);
				}
				else
				{
					HashMap<Integer,Float> prediction_u = new HashMap<Integer,Float>();
					prediction_u.put(j, r_uj_prediction);
					PredictionUCF.put(u, prediction_u);
				}
			}
			// --------------------------------------------
		}
	}
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static float UCF_PredictionRule(HashMap<Integer,Float> user2similarity_u_all, int u, int j)
	{
		float r_uj_prediction = 0;

		if(TrainData.containsKey(u))
		{
			if( !user2similarity_u_all.isEmpty() && TrainDataItem2User.containsKey(j))
			{	
				// ---
				HashMap<Integer,Float> user2rating_j_TrainDataItem2User = TrainDataItem2User.get(j);

				// ============================================
				// --- Sort and obtain the nearest neighbors of user u, $\mathcal{N}_u^j$
				HashMap<Integer,Float> user2similarity_u_nearest = new HashMap<Integer,Float>();
				if( KNNneighborNum>0 && KNNneighborNum<n && user2similarity_u_all.size()>KNNneighborNum )
				{
					// --- intersection
					for( int w : user2rating_j_TrainDataItem2User.keySet() )
					{
						if( user2similarity_u_all.containsKey(w) )
						{
							user2similarity_u_nearest.put(w, user2similarity_u_all.get(w));
						}
					}
					// !!! if approximate prediction rule, then the above *intersection* is NOT needed !!!

					// --- sort
					List<Map.Entry<Integer,Float>> listY = 
							new ArrayList<Map.Entry<Integer,Float>>(user2similarity_u_nearest.entrySet()); 		
					Collections.sort(listY, new Comparator<Map.Entry<Integer,Float>>()
							{
						public int compare( Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2 )   
						{
							return o2.getValue().compareTo( o1.getValue() );
						}
							});

					// --- obtain nearest neighbors
					user2similarity_u_nearest.clear();
					int _KNNneighborNum = 1;		
					for(Map.Entry<Integer,Float> entry : listY)
					{
						if( _KNNneighborNum > KNNneighborNum)
						{
							break;
						}
						else
						{
							user2similarity_u_nearest.put(entry.getKey(), entry.getValue());
							_KNNneighborNum++;
						}
					}
				}
				else
				{
					user2similarity_u_nearest = user2similarity_u_all;
				}
				// ============================================

				// ============================================
				// --- prediction rule
				float prediction_up = 0;
				float prediction_down = 0;
				for( int w : user2rating_j_TrainDataItem2User.keySet() )
				{
					if( user2similarity_u_nearest.containsKey(w) )
					{
						float s_uw = user2similarity_u_nearest.get(w);
						float r_wj = user2rating_j_TrainDataItem2User.get(w);
						float r_w = userRatingAvgTrain[w];
						prediction_up += s_uw*(r_wj-r_w);
						prediction_down += s_uw;  // !!!
					}
				}
				// ---
				if(prediction_down==0) // means the intersection of U_j and N_u is an \emptyset
				{
					r_uj_prediction = userRatingAvgTrain[u];
				}
				else
				{
					r_uj_prediction = userRatingAvgTrain[u] + prediction_up / prediction_down;
				}
				// ============================================
			}
			else // 
			{
				r_uj_prediction = userRatingAvgTrain[u];
			}
		}
		else
		{
			r_uj_prediction = g_avg; //						
		}

		return r_uj_prediction;
	}
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static void calculateUserUserSimilarity(HashMap<Integer,Float> user2similarity_u_all, int u)
	{			
		//
		if(!TrainData.containsKey(u))
			return;

		//
		HashMap<Integer,Float> item2rating_u = TrainData.get(u);

		// ---
		for(int w=1; w<=n; w++)
		{
			ArrayList<Integer> items_uw = new ArrayList<Integer>();
			float similarity_uw = 0;
			if(w!=u)
			{
				// ---
				if( !TrainData.containsKey(w) )
					continue;
				HashMap<Integer,Float> item2rating_w = TrainData.get(w);

				// --- intersection
				for(int _item: item2rating_u.keySet())
				{
					if(item2rating_w.containsKey(_item))
					{
						items_uw.add(_item);
					}
				}
				int items_uw_num = items_uw.size();

				// --- similarity			
				if(items_uw_num>0)
				{
					float similarity_up = 0;
					float similarity_down1 = 0;
					float similarity_down2 = 0;
					for(int _k=0; _k<items_uw_num; _k++)
					{
						int k = items_uw.get(_k);
						// ---
						float r_uk = item2rating_u.get(k);
						float r_wk = item2rating_w.get(k);
						float r_u = userRatingAvgTrain[u];
						float r_w = userRatingAvgTrain[w];

						// --- PCC // !!! We may use some other similarity measures
						similarity_up += (r_uk-r_u)*(r_wk-r_w);
						similarity_down1 += (r_uk-r_u)*(r_uk-r_u);
						similarity_down2 += (r_wk-r_w)*(r_wk-r_w);
					}
					//
					if(similarity_down1==0 || similarity_down2==0)
					{
						similarity_uw = 0;
					}
					else
					{
						similarity_uw = (float) (similarity_up / Math.sqrt(similarity_down1*similarity_down2));
					}
				}

				// ---
				if(similarity_uw>0)
				{
					user2similarity_u_all.put(w,similarity_uw);
				}
			}
		}
	}
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static void ICF_Prediction(HashMap<Integer,HashMap<Integer,Float>> TestDataItem2User,
							   HashMap<Integer,HashMap<Integer,Float>> PredictionICF)
	{		
		for(int j=1; j<=m; j++)
		{
			// --- check whether the item $j$ is in the test set
			if (!TestDataItem2User.containsKey(j))
				continue;
			HashMap<Integer,Float> user2rating_j_TestDataItem2User = TestDataItem2User.get(j);
			
			// --------------------------------------------
			// --- Step 1. (item,item) similarity in Item-based CF
			HashMap<Integer,Float> item2similarity_j_all = new HashMap<Integer,Float>();
			calculateItemItemSimilarity(item2similarity_j_all, j);			
			// --------------------------------------------
			
			// --------------------------------------------
			// --- Step 2. Prediction
			for(int u : user2rating_j_TestDataItem2User.keySet())
			{
				float r_uj_prediction = 0;

				// --- Prediction via ICF
				r_uj_prediction = ICF_PredictionRule(item2similarity_j_all, u, j);
								
				// --- Step 3. post-processing
				if(r_uj_prediction>maxRating)
				{
					r_uj_prediction=maxRating;
				}
				if(r_uj_prediction<minRating)
				{
					r_uj_prediction=minRating;
				}
			
				// --- Step 4. save the prediction
				if(PredictionICF.containsKey(u))
				{
					HashMap<Integer,Float> prediction_u = PredictionICF.get(u);
					prediction_u.put(j, r_uj_prediction);
					PredictionICF.put(u, prediction_u);
				}
				else
				{
					HashMap<Integer,Float> prediction_u = new HashMap<Integer,Float>();
					prediction_u.put(j, r_uj_prediction);
					PredictionICF.put(u, prediction_u);
				}
			}
			// --------------------------------------------
		}
	}
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static float ICF_PredictionRule(HashMap<Integer,Float> item2similarity_j_all, int u, int j)
	{
		float r_uj_prediction = 0;

		if(TrainDataItem2User.containsKey(j))
		{
			if( !item2similarity_j_all.isEmpty() && TrainData.containsKey(u) )
			{
				// ---
				HashMap<Integer,Float> item2rating_u_TrainData = TrainData.get(u);

				// ============================================
				// --- Sort and obtain the nearest neighbors of item j, $\mathcal{N}_j^u$
				HashMap<Integer,Float> item2similarity_j_nearest = new HashMap<Integer,Float>();
				if( KNNneighborNum>0 && KNNneighborNum<n && item2similarity_j_all.size()>KNNneighborNum )
				{
					// --- intersection
					for( int k : item2rating_u_TrainData.keySet() )
					{
						if( item2similarity_j_all.containsKey(k) )
						{
							item2similarity_j_nearest.put(k, item2similarity_j_all.get(k));
						}
					}

					// --- sort
					List<Map.Entry<Integer,Float>> listY = 
							new ArrayList<Map.Entry<Integer,Float>>(item2similarity_j_nearest.entrySet()); 		
					Collections.sort(listY, new Comparator<Map.Entry<Integer,Float>>()
							{
						public int compare( Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2 )   
						{
							return o2.getValue().compareTo( o1.getValue() );
						}
							});

					// --- obtain nearest neighbors
					item2similarity_j_nearest.clear();
					int _KNNneighborNum = 1;		
					for(Map.Entry<Integer,Float> entry : listY)
					{
						if( _KNNneighborNum > KNNneighborNum)
						{
							break;
						}
						else
						{
							item2similarity_j_nearest.put(entry.getKey(), entry.getValue());
							_KNNneighborNum++;
						}
					}
				}
				else
				{
					item2similarity_j_nearest = item2similarity_j_all;
				}
				// ============================================

				// ============================================
				// --- prediction rule
				float prediction_up = 0;
				float prediction_down = 0;
				for( int k : item2rating_u_TrainData.keySet() )
				{
					if( item2similarity_j_nearest.containsKey(k) )
					{
						float s_kj = item2similarity_j_nearest.get(k);
						float r_uk = item2rating_u_TrainData.get(k);
						prediction_up += s_kj*r_uk;
						prediction_down += s_kj;  // !!!
					}
				}
				// ---
				if(prediction_down==0) // means the intersection is an \emptyset
				{
					r_uj_prediction = itemRatingAvgTrain[j];
				}
				else
				{
					r_uj_prediction = prediction_up / prediction_down;
				}
				// ============================================
			}
			else //
			{
				r_uj_prediction = itemRatingAvgTrain[j];
			}
		}
		else
		{
			r_uj_prediction = g_avg; //						
		}

		return r_uj_prediction;
	}
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static void calculateItemItemSimilarity(HashMap<Integer,Float>item2similarity_j_all, int j)
	{
		// ---
		if( !TrainDataItem2User.containsKey(j) )
			return;
		
		HashMap<Integer,Float> user2rating_j = TrainDataItem2User.get(j);

		// ---
		for(int k=1; k<=m; k++)
		{
			ArrayList<Integer> users_kj = new ArrayList<Integer>();
			float cosine_kj = 0;
			if(k!=j)
			{
				// ---
				if( !TrainDataItem2User.containsKey(k) )
					continue;
				HashMap<Integer,Float> user2rating_k = TrainDataItem2User.get(k);

				// --- intersection
				for(int _user: user2rating_j.keySet())
				{
					if(user2rating_k.containsKey(_user))
					{
						users_kj.add(_user);
					}
				}
				int users_kj_num = users_kj.size();
				
				// --- similarity
				if(users_kj_num>0)
				{
					float similarity_up = 0;
					float similarity_down1 = 0;
					float similarity_down2 = 0;
					for(int _u=0; _u<users_kj_num; _u++)
					{
						int u = users_kj.get(_u);
						// ---
						float r_uk = user2rating_k.get(u);
						float r_uj_prediction = user2rating_j.get(u);
						float r_u = userRatingAvgTrain[u];    						    	

						// !!! We may use some other similarity measures
						// --- adjusted cosine similarity
						similarity_up += (r_uk-r_u)*(r_uj_prediction-r_u);
						similarity_down1 += (r_uk-r_u)*(r_uk-r_u);
						similarity_down2 += (r_uj_prediction-r_u)*(r_uj_prediction-r_u);
						
						// --- cosine similarity
						//similarity_up += (r_uk)*(r_uj_prediction);
						//similarity_down1 += (r_uk)*(r_uk);
						//similarity_down2 += (r_uj_prediction)*(r_uj_prediction);    						
					}
					
					//
					if(similarity_down1==0 || similarity_down2==0)
					{
						cosine_kj = 0;
					}
					else
					{
						cosine_kj = (float) (similarity_up / Math.sqrt(similarity_down1*similarity_down2));
					}
				}

				// ---
	    		if(cosine_kj>0)
	    		{
	    			item2similarity_j_all.put(k,cosine_kj);
	    		}
			}
		}
	}
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
}


	