package lexicons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import utils.Stemmer;
import utils.Settings;

public class ConnotationFrame {

	static private Map<String, ConnotationFrame> connotationFrames;
	static private Map<String, ConnotationFrame> connotationFramesByStem;

	static {
		if (!Settings.EFFECT_WORDNET) {
			connotationFrames = new HashMap<String, ConnotationFrame>();
			connotationFramesByStem = new HashMap<String, ConnotationFrame>();

			Stemmer stemmer = new Stemmer();

			System.out.print("Loading Connotation Frames ... ");

			try {
				BufferedReader br = new BufferedReader(
						new FileReader(new File(Settings.EFFECT_LEXICON)));
				br.readLine();

				while (br.ready()) {
					String line = br.readLine();
					if (!line.isEmpty()) {
						String[] splitted = line.trim().split("\t");
						if (splitted.length != 13) {
							System.err.println("Unexpected array length: " + splitted.length);
//							continue;
							System.exit(0);
						}
						String verb = splitted[0];
						double[] values = new double[12];
						for (int i = 0; i < 12; i++) {
							values[i] = Double.parseDouble(splitted[i + 1]);
						}
						connotationFrames.put(verb, new ConnotationFrame(values));
						connotationFramesByStem.put(stemmer.stem(verb), new ConnotationFrame(values));
					}
				}

				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("complete! (" + realSize(connotationFrames) + " verbs, "
					+ realSize(connotationFramesByStem) + " stems)");

			// Value of a new word is average of all synonyms that already have a value.
			if (Settings.extendConnotationFramesWithWordnet) {
				System.out.print("Extending Connotation Frames via WordNet ... ");
				Set<String> keys = new TreeSet<String>(connotationFrames.keySet());
				Map<String, ConnotationFrame> toAdd = new HashMap<String, ConnotationFrame>();

				for (String word : keys) {
					Set<String> set = WordNet.getSynonyms(word, net.didion.jwnl.data.POS.VERB);
					for (String s : set) {
						if (connotationFrames.keySet().contains(s) || toAdd.keySet().contains(s) || s.contains("_")) {
							continue;
						}
						double value = 0.0;
						int count = 0;
						for (String w : WordNet.getSynonyms(s, net.didion.jwnl.data.POS.VERB)) {
							if (!connotationFrames.keySet().contains(w)) {
								continue;
							}
							double v = connotationFrames.get(w).effectOnObject;
							if (value * v < 0) {
								value = 0.0;
								break;
							}
							value += v;
							count++;
						}

						if (value != 0.0) {
							value /= (double) count;
							toAdd.put(s, new ConnotationFrame(value));
						}
					}
				}

				for (String key : toAdd.keySet()) {
					connotationFrames.put(key, toAdd.get(key));

					String stem = stemmer.stem(key);
					if (connotationFramesByStem.keySet().contains(stem)) {
						continue;
					}
					connotationFramesByStem.put(stem, connotationFrames.get(key));
				}
				System.out.println("complete! (" + realSize(connotationFrames) + " verbs, "
						+ realSize(connotationFramesByStem) + " stems)");
			}
		}
	}

	public static int realSize(Map<String, ConnotationFrame> map) {
		int size = 0;
		for (ConnotationFrame cf : map.values()) {
			if (cf.effectOnObject > Settings.positiveEffectThreshold
					|| cf.effectOnObject < Settings.negativeEffectThreshold) {
				size++;
			}
		}
		return size;
	}

	private double effectOnObject;

	public ConnotationFrame(double[] values) {
		if (values.length != 12) {
			System.err.println("Unexpected array length: " + values.length);
			System.exit(0);
		}
		this.effectOnObject = values[3];
	}

	private ConnotationFrame(double effectOnObject) {
		this.effectOnObject = effectOnObject;
	}

	public static double getEffectOnObject(String verb) {
		verb = verb.toLowerCase();
		ConnotationFrame cf = connotationFrames.get(verb);
		if (cf != null) {
			double score = cf.effectOnObject;
			if (score > Settings.negativeEffectThreshold && score < Settings.positiveEffectThreshold) {
				return 0.0;
			} else {
				return score;
			}
		} else {
			return 0.0;
		}
	}

	public static double getEffectOnObjectByStem(String stem) {
		ConnotationFrame cf = connotationFramesByStem.get(stem);
		if (cf != null) {
			double score = cf.effectOnObject;
			if (score > Settings.negativeEffectThreshold && score < Settings.positiveEffectThreshold) {
				return 0.0;
			} else {
				return score;
			}
		} else {
			return 0.0;
		}
	}
}
