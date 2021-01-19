package lexicons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import utils.Settings;

public class IBMUnigrams {

	private static final Map<String, Double> unigrams;
	
	static {
		unigrams = new HashMap<String, Double>();
		readUnigrams();
	}
	
	public static double getValue(String word) {
		if(unigrams.keySet().contains(word)) {
			double value = unigrams.get(word);
			return value;
		}
		return 0.0;
	}
	
	private static void readUnigrams() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(Settings.UNIGRAMS)));
			br.readLine();
			while(br.ready()) {
				String line = br.readLine().trim();
				if(!line.isEmpty()) {
					String split[] = line.split(" ");
					String word = split[0];
					double score = Double.parseDouble(split[1]);
					unigrams.put(word, score);
				}
			}
			
			br.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		System.out.println(IBMUnigrams.getValue("terrorist"));
	}
}
