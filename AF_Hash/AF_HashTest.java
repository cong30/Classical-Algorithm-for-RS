package AF_Hash;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;

public class AF_HashTest {
	public static final int USERNUM=944;
	public static final int ITEMNUM=1683;
	
	public static final String U1_BASE="F:\\study\\Recommendation\\ml-100k\\u1.base";
	public static final String U1_TEST="F:\\study\\Recommendation\\ml-100k\\u1.test";
	public static float g_avg=0;
	public static float minRating = 1;
	public static float maxRating = 5;
	public static int ruleIndex=0;
	
	//
	public static int[] userRatingNum=new int[USERNUM]; // 943
	public static float[] userRatingSum=new float[USERNUM]; // 943
	public static float[] userRatingAvg=new float[USERNUM];//943
	public static float[] biasofUser=new float[USERNUM];
	
	public static int[] itemRatingNum=new int[ITEMNUM]; // 1682
	public static float[] itemRatingSum=new float[ITEMNUM]; // 1682
	public static float[] itemRatingAvg=new float[ITEMNUM];//1682
	public static float[] biasofItem=new float[ITEMNUM];
	
	//data structure 
	public static HashMap<Integer,HashMap<Integer,Float>> TrainDataUser_Item = new HashMap<Integer,HashMap<Integer,Float>>();
	public static HashMap<Integer,HashMap<Integer,Float>> TrainDataItem_User = new HashMap<Integer,HashMap<Integer,Float>>();
	public static HashMap<Integer,HashMap<Integer,Float>> TestDataUser_Item = new HashMap<Integer,HashMap<Integer,Float>>();
	public static HashMap<Integer,HashMap<Integer,Float>> TestDataItem_User = new HashMap<Integer,HashMap<Integer,Float>>();
	
	public static HashMap<Integer, HashMap<Integer,Float>> PredictionRating = new HashMap<Integer, HashMap<Integer,Float>>();


	public static void main(String[] args) throws Exception {
		System.out.println("please enter the number of rule: ");    
		Scanner input=new Scanner(System.in);
		ruleIndex=input.nextInt();
		readTrainTestData();
		get_Ru_Ri();
		get_biasUser_biasItem();
		AF_Evaluation();
	}
	
	//
	public static void readTrainTestData() throws Exception {
		//get train data
		BufferedReader br=new BufferedReader(new FileReader(U1_BASE));
		double ratingSum=0;
		String s=null;
		
		while((s=br.readLine())!=null) {
			String[] temp=s.split("\t");
			int userID=Integer.parseInt(temp[0]);
			int itemID=Integer.parseInt(temp[1]);
			float rating=Integer.parseInt(temp[2]);
			
			ratingSum+=rating;
			userRatingSum[userID]+=rating;
			userRatingNum[userID]++;
			itemRatingSum[itemID]+=rating;
			itemRatingNum[itemID]++;
			
			//TrainData:user->(item,rating)
			if(TrainDataUser_Item.containsKey(userID)) {
				HashMap<Integer,Float> item_rating=TrainDataUser_Item.get(userID);
				item_rating.put(itemID,rating);
				TrainDataUser_Item.put(userID, item_rating);	
			}
			else {
				HashMap<Integer,Float> item_rating=new HashMap<Integer,Float>();
				item_rating.put(itemID,rating);
				TrainDataUser_Item.put(userID, item_rating);	
				
			}
			//TrainData:item->(user,rating)
			if(TrainDataItem_User.containsKey(itemID)) {
				HashMap<Integer,Float> user_rating=TrainDataItem_User.get(itemID);
				user_rating.put(userID,rating);
				TrainDataItem_User.put(itemID, user_rating);	
			}
			else {
				HashMap<Integer,Float> user_rating=new HashMap<Integer,Float>();
				user_rating.put(userID,rating);
				TrainDataItem_User.put(itemID, user_rating);	
			}
		}
		g_avg=(float)ratingSum/80000;
		br.close();	
		
		//get test data
		br=new BufferedReader(new FileReader(U1_TEST));
	    s=null;
		
		while((s=br.readLine())!=null) {
			String[] temp=s.split("\t");
			int userID=Integer.parseInt(temp[0]);
			int itemID=Integer.parseInt(temp[1]);
			float rating=Integer.parseInt(temp[2]);
			
			//TestData:user->(item,rating)
			if(TestDataUser_Item.containsKey(userID)) {
				HashMap<Integer,Float> item_rating=TestDataUser_Item.get(userID);
				item_rating.put(itemID,rating);
				TestDataUser_Item.put(userID, item_rating);	
			}
			else {
				HashMap<Integer,Float> item_rating=new HashMap<Integer,Float>();
				item_rating.put(itemID,rating);
				TestDataUser_Item.put(userID, item_rating);	
				
			}
			//TestData:item->(user,rating)
			if(TestDataItem_User.containsKey(itemID)) {
				HashMap<Integer,Float> user_rating=TestDataItem_User.get(itemID);
				user_rating.put(userID,rating);
				TestDataItem_User.put(itemID, user_rating);	
			}
			else {
				HashMap<Integer,Float> user_rating=new HashMap<Integer,Float>();
				user_rating.put(userID,rating);
				TestDataItem_User.put(itemID, user_rating);	
			}
			
		
	    }
		br.close();	
	}//read data
	
	//get every variable
	public static void get_Ru_Ri() {
		//user
		for(int userID=1; userID<USERNUM; userID++){
	    	if (userRatingNum[userID]>0){
	    		userRatingAvg[userID] = userRatingSum[userID] / userRatingNum[userID];
	    	}
	    	else{
	    		userRatingAvg[userID] = g_avg;
	    	}
	    		
	    }
	    //item
	    for(int itemID=1; itemID<ITEMNUM; itemID++){
	    	if (itemRatingNum[itemID]>0){
	    		itemRatingAvg[itemID] = itemRatingSum[itemID] / itemRatingNum[itemID];
	    	}
	    	else{
	    		itemRatingAvg[itemID] = g_avg;
	    	}
	    }		
	}
		
	//get bias of user and item	
	public static void get_biasUser_biasItem() {
		int biasofUserNum=0;
		int biasofItemNum=0;
		
		//bias of user
		for(int userID=1; userID<USERNUM; userID++) {
			if(TrainDataUser_Item.containsKey(userID)) {
				HashMap<Integer,Float> item_rating=TrainDataUser_Item.get(userID);
				for(int j:item_rating.keySet()) {
					biasofUser[userID]+=item_rating.get(j)-itemRatingAvg[j];
					biasofUserNum++;
				}
				biasofUser[userID]=biasofUser[userID]/biasofUserNum;
				biasofUserNum=0;
			}
			else biasofUser[userID]=0;
			
		}
		
		//bias of item
		for(int itemID=1;itemID<ITEMNUM;itemID++) {
			if(TrainDataItem_User.containsKey(itemID)) {
				HashMap<Integer,Float> user_rating=TrainDataItem_User.get(itemID);
				for(int j:user_rating.keySet()) {
					biasofItem[itemID]+=user_rating.get(j)-userRatingAvg[j];
					biasofItemNum++;
				}
				biasofItem[itemID]=biasofItem[itemID]/biasofItemNum;
				biasofItemNum=0;
			}
			else biasofItem[itemID]=0;
		}
			
	}
	
	//Evaluation
	public static void AF_Evaluation() {
		int num_testcase = 0;
		float mae_sum = 0;
		float rmse_sum = 0;
		
		AF_Prediction();
		for(int u:TestDataUser_Item.keySet()) {
			HashMap<Integer,Float> item_rating_TestData = TestDataUser_Item.get(u);
			HashMap<Integer,Float> item_rating_Prediction = PredictionRating.get(u);
			
			for(int i:item_rating_TestData.keySet()) {
				float r_uj=item_rating_TestData.get(i);
				float r_uj_prediction = item_rating_Prediction.get(i);
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
	
	//AFprediction
	public static void AF_Prediction() {
		for(int u=1;u<USERNUM;u++) {
			if (!TestDataUser_Item.containsKey(u)) continue;
			HashMap<Integer,Float> item_rating_TestData = TestDataUser_Item.get(u);
			for(int j:item_rating_TestData.keySet()) {
				float r_uj_prediction = 0;
				r_uj_prediction =predictionRule(u, j);
				
				if(r_uj_prediction>maxRating){					
					r_uj_prediction=maxRating;
				}
				if(r_uj_prediction<minRating){
					r_uj_prediction=minRating;
				}
				
				//save the prediction
				if(PredictionRating.containsKey(u)){
					HashMap<Integer,Float> prediction_u = PredictionRating.get(u);
					prediction_u.put(j, r_uj_prediction);
					PredictionRating.put(u, prediction_u);
				}
				else
				{
					HashMap<Integer,Float> prediction_u = new HashMap<Integer,Float>();
					prediction_u.put(j, r_uj_prediction);
					PredictionRating.put(u, prediction_u);
				}
				
			}
				
		}
	}
	
	//prediction rule
	public static float predictionRule(int u,int i) {
		float r_ui_prediction=0;
		if(ruleIndex==1) {
			r_ui_prediction=userRatingAvg[u];
		}
		if(ruleIndex==2) {
			r_ui_prediction=itemRatingAvg[i];
		}
		if(ruleIndex==3) {
			r_ui_prediction=userRatingAvg[u]/2+itemRatingAvg[i]/2;
		}
		if(ruleIndex==4) {
			r_ui_prediction=biasofUser[u]+itemRatingAvg[i];
		}
		if(ruleIndex==5) {
			r_ui_prediction=biasofItem[i]+userRatingAvg[u];
		}
		if(ruleIndex==6) {
			r_ui_prediction=biasofUser[u]+biasofItem[i]+g_avg;
		}
		return r_ui_prediction;
		
	}
	
	
}//class
