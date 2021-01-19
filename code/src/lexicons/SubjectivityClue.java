package lexicons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import utils.Settings;

public class SubjectivityClue {

	static private final Map<POS, Map<String, SubjectivityClue>> subjectivityClues;

	static {
		subjectivityClues = new EnumMap<POS, Map<String, SubjectivityClue>>(SubjectivityClue.POS.class);
		for (POS pos : POS.values()) {
			subjectivityClues.put(pos, new HashMap<String, SubjectivityClue>());
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(Settings.MPQA)));

			while (br.ready()) {
				String line = br.readLine();
				if (!line.isEmpty()) {
					String[] splitted = line.trim().split(" ");
					if (splitted.length != 6) {
						System.err.println("Unexpected format in " + Settings.MPQA + ": " + line);
//						System.exit(0);
						continue;
					}
					String word = splitted[2].split("=")[1];
					POS pos = POS.convertToPos(splitted[3].split("=")[1]);
					String polarity = splitted[5].split("=")[1];
					if (polarity.startsWith("neutr") || polarity.startsWith("both")) {
						continue;
					}
					
					String strength = splitted[0].split("=")[1];
					double score = 0.0;
					if (polarity.startsWith("pos")) {
						score = 1.0;
					} else if (polarity.startsWith("neg")) {
						score = -1.0;
					} else {
						System.err.println("Unreachable Code 3476");
					}

					if (strength.startsWith("weak")) {
						score *= 0.5;
					}

					

					subjectivityClues.get(pos).put(word, new SubjectivityClue(word, pos, score));

				}
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (utils.Settings.extendSubjectivityCluesWithWordnet) {
			extendViaWordnet();
		}
	}

	public boolean equals(Object o) {
		if (!(o instanceof SubjectivityClue)) {
			return false;
		}
		SubjectivityClue sc = (SubjectivityClue) o;
		if (sc.word.equals(this.word) && sc.pos == this.pos) {
			return true;
		}
		return false;
	}

	private static void extendViaWordnet() {

		for (POS pos : POS.values()) {
			if (pos == POS.ANY) {
				continue;
			}
			Map<String, SubjectivityClue> toAdd = new HashMap<String, SubjectivityClue>();
			for (SubjectivityClue sc : subjectivityClues.get(pos).values()) {
				Set<String> set = WordNet.getSynonyms(sc.getWord(), WordNet.toPos(pos.toString()));
				for (String s : set) {
					if (subjectivityClues.get(pos).keySet().contains(s)
							|| subjectivityClues.get(POS.ANY).keySet().contains(s) || toAdd.keySet().contains(s)
							|| s.contains("_")) {
						continue;
					}
					double value = 0.0;
					int count = 0;
					for (String w : WordNet.getSynonyms(s, WordNet.toPos(pos.toString()))) {
						double v = getValue(w, pos.toString());
						if (v == 0.0) { // ignore neturals
							continue;
						}
						if (value * v < 0) {
							value = 0.0;
							break;
						}
						value += v;
						count++;
					}

					if (value != 0.0) {
						value = value / (double) count;
						toAdd.put(s, new SubjectivityClue(s, pos, value));
					}
				}
			}

			for (String key : toAdd.keySet()) {
				subjectivityClues.get(pos).put(key, toAdd.get(key));
			}
		}
	}

	private String word;
	private POS pos;
	private double polarityScore;

	private SubjectivityClue(String word, POS pos, double polarityScore) {
		this.word = word;
		this.pos = pos;
		this.polarityScore = polarityScore;
	}

	private static SubjectivityClue getSubjectivityClue(String word, String corenlpPos) {
		word = word.toLowerCase();
		POS pos;
		if (corenlpPos.startsWith("N")) {
			pos = POS.NOUN;
		} else if (corenlpPos.startsWith("J") || corenlpPos.startsWith("ADJ")) {
			pos = POS.ADJ;
		} else if (corenlpPos.startsWith("RB") || corenlpPos.startsWith("ADV")) {
			pos = POS.ADVERB;
		} else if (corenlpPos.startsWith("V")) {
			pos = POS.VERB;
		} else {
			pos = POS.ANY;
		}

		SubjectivityClue sc = subjectivityClues.get(pos).get(word);
		if (sc == null) {
			sc = subjectivityClues.get(POS.ANY).get(word);
		}
		return sc;
	}

	public static double getValue(String word, String corenlpPos) {

		SubjectivityClue sc = getSubjectivityClue(word, corenlpPos);
		if(sc == null) {
			return 0.0;
		}
		
		return sc.polarityScore;
	}

	public String getWord() {
		return word;
	}

	public POS getPos() {
		return pos;
	}

	public enum POS {
		ADJ, NOUN, VERB, ADVERB, ANY;

		public static POS convertToPos(String s) {
			if (s.toLowerCase().startsWith("adj")) {
				return ADJ;
			} else if (s.toLowerCase().startsWith("noun")) {
				return NOUN;
			} else if (s.toLowerCase().startsWith("verb")) {
				return VERB;
			} else if (s.toLowerCase().startsWith("adverb")) {
				return ADVERB;
			} else if (s.toLowerCase().startsWith("any")) {
				return ANY;
			} else {
				return null;
			}
		}
	}

	@Override
	public String toString() {
		return "[" + word + ", " + pos + ", " + polarityScore + "]";
	}

}
