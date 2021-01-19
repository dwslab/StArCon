package lexicons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import utils.Settings;

public class IBMBigrams {

	private static final Map<String, Double> bigrams;
	
	static {
		bigrams = new HashMap<String, Double>();
		readBigrams();
	}

	public static double getValue(String word1, String word2) {
		return getBigramValue(word1 + " " + word2);
	}
	
	public static double getBigramValue(String bigram) {
		if(bigrams.keySet().contains(bigram)) {
			double value = bigrams.get(bigram);
			return value;
		}
		return 0.0;
	}
	
	private static void readBigrams() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(Settings.BIGRAMS)));
			br.readLine();
			while(br.ready()) {
				String line = br.readLine().trim();
				if(!line.isEmpty()) {
					String split[] = line.split(" ");
					String word = split[0].replace("-", " ");
					double score = Double.parseDouble(split[2]);
					bigrams.put(word, score);
				}
			}
			
			br.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		System.out.println(IBMBigrams.getValue("nuclear", "war"));
	}
}
