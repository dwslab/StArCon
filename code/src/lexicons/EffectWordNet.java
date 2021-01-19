package lexicons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.Pair;
import utils.Settings;

public class EffectWordNet implements Comparable<EffectWordNet> {

	private String id;
	private short effect;
	private Set<String> synset;
	private Set<String> explanations;
	private Set<String> examples;

	private static Map<String, Set<EffectWordNet>> instances;
	private static Map<String, Set<EffectWordNet>> instancesByStem;

	private EffectWordNet() {
		synset = new TreeSet<>();
		explanations = new TreeSet<>();
		examples = new TreeSet<>();
	}

	static {
		if (Settings.EFFECT_WORDNET) {
			instances = new HashMap<String, Set<EffectWordNet>>();
			instancesByStem = new HashMap<String, Set<EffectWordNet>>();
			try {
				BufferedReader br = new BufferedReader(
						new FileReader(new File(Settings.EFFECT_LEXICON)));

				while (br.ready()) {
					String line[] = br.readLine().trim().toLowerCase().split("\t");
					EffectWordNet ewn = new EffectWordNet();
					ewn.id = line[0];
					if (line[1].startsWith("+")) {
						ewn.effect = 1;
					} else if (line[1].startsWith("-")) {
						ewn.effect = -1;
					} else {
						ewn.effect = 0;
					}

					for (String s : line[2].split(",")) {
						ewn.synset.add(s);
					}

					for (String s : line[3].trim().split("; ")) {
						if (s.startsWith("\"")) {
							ewn.examples.add(s.substring(1, s.length() - 1));
						} else {
							ewn.explanations.add(s);
						}
					}
					for (String s : ewn.synset) {
						if (!instances.containsKey(s)) {
							instances.put(s, new TreeSet<>());
						}
						instances.get(s).add(ewn);

						String stem = Settings.stemmer.stem(s);
						if (!instancesByStem.containsKey(stem)) {
							instancesByStem.put(stem, new TreeSet<>());
						}
						instancesByStem.get(stem).add(ewn);
					}
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static Set<IndexedWord> getChildWithRelationPrefix(SemanticGraph sg, IndexedWord index, String relation) {
		Set<IndexedWord> set = new TreeSet<>();
		List<Pair<GrammaticalRelation, IndexedWord>> list = sg.childPairs(index);
		for (Pair<GrammaticalRelation, IndexedWord> pair : list) {
			if (pair.first().toString().startsWith(relation)) {
				set.add(pair.second());
			}
		}
		return set;
	}

	public String toString() {
		return "[" + id + ", " + effect + ", " + synset.toString() + ", " + explanations.toString() + ", "
				+ examples.toString() + "]";
	}

	public static double getEffect(String word) {
		Set<EffectWordNet> set = instances.get(word);
		if (set == null) {
			return 0.0;
		}
		double sum = 0.0;
		for (EffectWordNet e : set) {
			sum += e.effect;
		}
		sum /= set.size();

		return sum;
	}

	public static double getEffectByStem(String stem) {
		Set<EffectWordNet> set = instancesByStem.get(stem);
		if (set == null) {
			return 0.0;
		}
		double sum = 0.0;
		int size = 0;
		for (EffectWordNet e : set) {
			sum += e.effect;
			size++;
		}
		if (size == 0) {
			return 0.0;
		}

		sum /= size;

		if (sum < Settings.negativeEffectThreshold_EffectWordnet || sum > Settings.positiveEffectThreshold_EffectWordnet) {
			return sum;
		} else {
			return 0.0;
		}
	}

	@Override
	public int compareTo(EffectWordNet o) {
		return id.compareTo(o.id);
	}

}
