package lexicons;

import data.Argument.Role;
import data.NLPAnnotatedArgument;
import utils.Settings;

public class LexiconManager {

	public static double getEffect(String word) {
		String stem = Settings.stemmer.stem(word.toLowerCase());
		if (Settings.EFFECT_WORDNET) {
			return EffectWordNet.getEffectByStem(stem);
		} else {
			return ConnotationFrame.getEffectOnObjectByStem(stem);
		}
	}

	public static double getBigramValue(String word1, String word2, String lemma1, String lemma2, boolean threshold) {
		double value;
		value = IBMBigrams.getValue(word1, word2);
		if (value == 0.0) {
			value = IBMBigrams.getValue(word1, lemma2);
		}
		if (value == 0.0) {
			value = IBMBigrams.getValue(lemma1, word2);
		}
		if (value == 0.0) {
			value = IBMBigrams.getValue(lemma1, lemma2);
		}

		if (!threshold
				|| (value > Settings.positiveSentimentThreshold || value < Settings.negativeSentimentThreshold)) {
			return value;
		} else {
			return 0.0;
		}
	}

	public static double getSentiment(Role role, NLPAnnotatedArgument arg, int i, boolean threshold) {
		String pos = arg.getPosTags(role).get(i);
		if (pos.equals("MD")) {
			return 0.0;
		}

		String lemma = arg.getLemmas(role).get(i).toLowerCase();
		String word = arg.getWords(role).get(i).toLowerCase();

		return LexiconManager.getSentiment(word, lemma, pos, threshold);
	}

	private static double getSentiment(String word, String lemma, String pos, boolean threshold) {
		double value = 0.0;
		for (int i = 0; i < Settings.lexiconOrder.length; i++) {
			switch (Settings.lexiconOrder[i]) {
			case "uni":
				value = IBMUnigrams.getValue(word);
				if (value == 0.0) {
					value = IBMUnigrams.getValue(lemma);
				}
				break;
			case "opinion":
				value = OpinionLexicon.getValue(word);
				if (value == 0.0) {
					value = OpinionLexicon.getValue(lemma);
				}
				break;
			case "subj":
				value = SubjectivityClue.getValue(word, pos);
				if (value == 0.0) {
					SubjectivityClue.getValue(lemma, pos);
				}
				break;
			}

			if (!threshold
					|| (value > Settings.positiveSentimentThreshold || value < Settings.negativeSentimentThreshold)) {
				return value;
			}
		}
		return 0.0;
	}
}
