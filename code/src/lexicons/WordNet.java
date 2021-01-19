package lexicons;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;
import utils.Settings;

public class WordNet {

	private static Dictionary dictionary;

	static {
		if (!Settings.EFFECT_WORDNET) {
			try {
				JWNL.initialize(new FileInputStream(Settings.WORDNET));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (JWNLException e) {
				e.printStackTrace();
			}
			dictionary = Dictionary.getInstance();
		}
	}

	public static Set<String> getSynonyms(String lemma, POS pos) {
		TreeSet<String> syns = new TreeSet<String>();
		try {
			IndexWord indexWord = dictionary.getIndexWord(pos, lemma);
			Synset[] senses = indexWord.getSenses();

			for (Synset set : senses) {
				for (Word word : set.getWords()) {
					syns.add(word.getLemma());
				}
			}
			return syns;
		} catch (Exception e) {
			return syns;
		}
	}

	public static Set<Set<String>> getSynsets(String lemma, POS pos) {
		Set<Set<String>> syns = new HashSet<Set<String>>();
		try {
			IndexWord indexWord = dictionary.getIndexWord(pos, lemma);
			Synset[] senses = indexWord.getSenses();

			for (Synset synset : senses) {
				Set<String> set = new TreeSet<String>();
				for (Word word : synset.getWords()) {
					set.add(word.getLemma());
				}
				syns.add(set);
			}
			return syns;
		} catch (Exception e) {
			return syns;
		}
	}

	public static boolean isSynonymous(String lemma1, String lemma2, POS pos) {
		if (getSynonyms(lemma1, pos).contains(lemma2)) {
			return true;
		} else {
			return false;
		}
	}

	public static POS toPos(String pos) {
		pos = pos.toLowerCase();
		if (pos.startsWith("adj") || pos.startsWith("j")) {
			return POS.ADJECTIVE;
		} else if (pos.startsWith("n")) {
			return POS.NOUN;
		} else if (pos.startsWith("adv") || pos.startsWith("rb")) {
			return POS.ADVERB;
		} else if (pos.startsWith("v")) {
			return POS.VERB;
		} else {
			return null;
		}
	}
}
