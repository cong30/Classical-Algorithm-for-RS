package linj.recommendation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ReadDataFile {

	private HashMap<Integer, HashMap<Integer,Float>> hmRatings = 
			new HashMap<Integer, HashMap<Integer,Float>>();
	private HashMap<Integer, HashMap<Integer,Float>> hmRatingsI2UR = 
			new HashMap<Integer, HashMap<Integer,Float>>();
	private int[][] mtRatings = new int[AppMF.n][AppMF.m];
	private int count_record, count_user;
	private float r_ave;
	
	public ReadDataFile(String fileName) {
		
		count_record = 0;
		count_user = 0;
		r_ave = 0;
		
		try {
			File file = new File(fileName);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] sdata = line.split("\t");
				int[] idata = new int[4]; 
				for (int i = 0; i < sdata.length; i++) {					   
					idata[i] = Integer.parseInt(sdata[i]);
				}
				
				int user_id = idata[0]-1;
				int item_id = idata[1]-1;
				int rating = idata[2];
				
				if(hmRatings.containsKey(user_id)) {
	    			HashMap<Integer,Float> item2rating = hmRatings.get(user_id);
	    			item2rating.put(item_id,(float) rating);
	    			hmRatings.put(user_id,item2rating);
	    		} else {
	    			HashMap<Integer,Float> item2rating = new HashMap<Integer,Float>();
	    			item2rating.put(item_id,(float) rating);
	    			hmRatings.put(user_id,item2rating);
	    			count_user++;
	    		}
				
				if(hmRatingsI2UR.containsKey(item_id)) {
	    			HashMap<Integer,Float> user2rating = hmRatingsI2UR.get(item_id);
	    			user2rating.put(user_id,(float) rating);
	    			hmRatingsI2UR.put(item_id,user2rating);
	    		} else {
	    			HashMap<Integer,Float> user2rating = new HashMap<Integer,Float>();
	    			user2rating.put(user_id,(float) rating);
	    			hmRatingsI2UR.put(item_id,user2rating);
	    		}
				
				mtRatings[user_id][item_id] = rating;
			 	
			 	r_ave += idata[2];
			 	count_record++;
			}
			r_ave /= count_record;
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public HashMap<Integer, HashMap<Integer,Float>> getRatingHashmapU2IR() {
		
		return hmRatings;
	}
	
	public HashMap<Integer, HashMap<Integer,Float>> getRatingHashmapI2UR() {
		
		return hmRatingsI2UR;
	}
	
	public int[][] getRatingMatrix() {
		
		return mtRatings;
	}
	
	public int getUserCount() {
		
		return count_user;
	}
	
	public float getAverageRating() {
		
		return r_ave;
	}
}
