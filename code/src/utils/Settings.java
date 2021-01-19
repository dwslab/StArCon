package utils;

import java.util.Set;
import java.util.TreeSet;

public class Settings {

	/** Parameters that can be specified upon starting the program. */

	public static String DATASET = "./resources/data/conseq.txt";
	
	public static boolean predict = false;
	public static boolean evaluate = true;
	
	public static String MPQA = "./resources/lexicons/subjclueslen1-HLTEMNLP05.tff";
	public static String BIGRAMS = "./resources/lexicons/LEXICON_BG.txt";
	public static String UNIGRAMS = "./resources/lexicons/LEXICON_UG.txt";
	public static String POSITIVES = "./resources/lexicons/positive-words.txt";
	public static String NEGATIVES = "./resources/lexicons/negative-words.txt";
	public static String WORDNET = "./resources/lexicons/wordnet/Properties.XML";
	
	public static String EFFECT_LEXICON = "./resources/lexicons/full_frame_info.txt"; // "./resources/lexicons/EffectWordNet.tff"
	public static boolean EFFECT_WORDNET = false;
	
	/** Parameters that we consider to be fixed. These can be specified only directly in the code */
	
	public static final double positiveEffectThreshold = 0.1;
	public static final double negativeEffectThreshold = -positiveEffectThreshold;
	
	public static final double positiveEffectThreshold_EffectWordnet = 0.2;
	public static final double negativeEffectThreshold_EffectWordnet = -positiveEffectThreshold_EffectWordnet;
	
	public static final double positiveSentimentThreshold = 0.2;
	public static final double negativeSentimentThreshold = -positiveSentimentThreshold;
	
	public static final String[] lexiconOrder = {"subj", "opinion", "uni"}; // Defines the priority of the lexicons. Bigrams always have highest priority.
	
	public static final boolean extendSubjectivityCluesWithWordnet = false;
	public static final boolean extendConnotationFramesWithWordnet = true;
	
	public static final Set<String> negativePrepositions;
	
	public static final Stemmer stemmer;
	
	static {
		stemmer = new Stemmer();
		
		negativePrepositions = new TreeSet<String>();
		negativePrepositions.add("except");
		negativePrepositions.add("less");
		negativePrepositions.add("minus");
		negativePrepositions.add("opposite");
		negativePrepositions.add("sans");
		negativePrepositions.add("unlike");
		negativePrepositions.add("versus");
		negativePrepositions.add("without");
		negativePrepositions.add("w/o");
		negativePrepositions.add("vice");
		negativePrepositions.add("instead");
		negativePrepositions.add("lack");
	}
}
