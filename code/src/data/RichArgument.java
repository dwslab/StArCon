package data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.stanford.nlp.ling.IndexedWord;
import lexicons.Thesaurus;
import utils.Methods;
import utils.Settings;

public class RichArgument extends NLPAnnotatedArgument {

	boolean outputActivated = false;
	private Set<Integer> claimTargetIndexes, topicTargetIndexes;
	private Set<String> targetLemmas;
	private boolean opposingTargets = false;
	private Set<Integer> claimNegationIndexes, topicNegationIndexes;
	private Set<Integer> claimNegated, topicNegated;
	private boolean claimTargetReduced, topicTargetReduced;
	private int claimTargetEffector, topicTargetEffector;
	private Map<Integer, Set<Integer>> claimEffects, topicEffects; // Do not contain effects on targets!
	private Map<Integer, Set<Integer>> claimHasSubject, topicHasSubject;

	public boolean opposingTargets() {
		return opposingTargets;
	}

	public RichArgument(NLPAnnotatedArgument arg) {
		super(arg);

		claimTargetIndexes = new TreeSet<Integer>();
		topicTargetIndexes = new TreeSet<Integer>();
		targetLemmas = new TreeSet<String>();
		claimEffects = new HashMap<>();
		topicEffects = new HashMap<>();
		claimHasSubject = new HashMap<>();
		topicHasSubject = new HashMap<>();

		claimNegationIndexes = new TreeSet<>();
		topicNegationIndexes = new TreeSet<>();
		claimNegated = new TreeSet<>();
		topicNegated = new TreeSet<>();
		claimTargetReduced = false;
		topicTargetReduced = false;
		claimTargetEffector = -1;
		topicTargetEffector = -1;

		findTargets();
		if (targetLemmas.isEmpty()) {
			abbrevationCheck();
		}
		if (targetLemmas.isEmpty()) {
			thesaurus();
		}

		cleanSentimentAndEffect();

		effectOnTarget(Role.CLAIM);
		effectOnTarget(Role.TOPIC);

		cleanSentimentAndEffect();

		negationOutsideTarget(Role.CLAIM);
		negationOutsideTarget(Role.TOPIC);

		cleanSentimentAndEffect();

		sentimentRecursively(Role.CLAIM);
		sentimentRecursively(Role.TOPIC);

		findSubjects(Role.CLAIM);
		findSubjects(Role.TOPIC);

		findEffects(Role.CLAIM);
		findEffects(Role.TOPIC);
	}

	private void findSubjects(Role role) {
		for (int i = 0; i < getWords(role).size(); i++) {
			Set<Integer> indexes = getChildWithRelation(i, "nsubj", role);
			indexes.addAll(getChildWithRelation(i, "csubj", role));
			if (!indexes.isEmpty()) {
				if (role == Role.CLAIM) {
					claimHasSubject.put(i, indexes);
				} else {
					topicHasSubject.put(i, indexes);
				}
			}
		}
	}

	private void cleanSentimentAndEffect() {
		for (int i = 0; i < claimWords.size(); i++) {
			if (claimTargetIndexes.contains(i) || claimNegationIndexes.contains(i)
					|| claimPosTags.get(i).equals("MD")) {
				claimSentiment[i] = 0.0;
				claimEffect[i] = 0.0;
			}
		}

		for (int i = 0; i < topicWords.size(); i++) {
			if (topicTargetIndexes.contains(i) || topicNegationIndexes.contains(i)
					|| topicPosTags.get(i).equals("MD")) {
				topicSentiment[i] = 0.0;
				topicEffect[i] = 0.0;
			}
		}
	}

	private void abbrevationCheck() {
		for (int i = 0; i < claimWords.size(); i++) {
			String word = claimWords.get(i);
			if (word.length() > 1 && word.equals(word.toUpperCase())) {
				for (int j = 0; j < topicWords.size() - word.length() + 1; j++) {
					boolean flag = true;
					for (int k = 0; k < word.length(); k++) {
						if (topicWords.get(j + k).charAt(0) != word.charAt(k)) {
							flag = false;
							break;
						}
					}
					if (flag) {
						for (int k = 0; k < word.length(); k++) {
							addToTarget(i, j + k);
						}
					}
				}
			}
		}
	}

	private void addToTarget(int i, int j) {
		claimTargetIndexes.add(i);
		topicTargetIndexes.add(j);
		targetLemmas.add(claimLemmas.get(i).toLowerCase());
		targetLemmas.add(topicLemmas.get(j).toLowerCase());
	}

	private boolean thesaurus(int i, int j) {
		if (claimPosTags.get(i).startsWith("N") && topicPosTags.get(j).startsWith("N")) {
			if (Thesaurus.isSynonymous(claimLemmas.get(i).toLowerCase(), topicLemmas.get(j).toLowerCase())) {
				addToTarget(i, j);
				return true;
			} else if (Thesaurus.isAntonymous(claimLemmas.get(i).toLowerCase(), topicLemmas.get(j).toLowerCase())) {
				addToTarget(i, j);
				opposingTargets = true;
				return true;
			}
		}
		return false;
	}

	private void thesaurus() {
		for (int i = 0; i < claimLemmas.size(); i++) {
			for (int j = 0; j < topicLemmas.size(); j++) {
				thesaurus(i, j);
			}
		}
	}

	private void findTargets() {
		for (int i = 0; i < claimLemmas.size(); i++) {
			if (!claimPosTags.get(i).startsWith("N")) {
				continue;
			}
			for (int j = 0; j < topicLemmas.size(); j++) {
				if (!topicPosTags.get(j).startsWith("N")) {
					continue;
				}

				if (claimWords.get(i).toLowerCase().equals(topicWords.get(j).toLowerCase())) {
					addToTarget(i, j);
				} else if (claimLemmas.get(i).toLowerCase().equals(topicLemmas.get(j).toLowerCase())) {
					addToTarget(i, j);
				}
			}
		}
	}

	/** Check if there is negation outside the target */
	private void negationOutsideTarget(Role role) {

		for (int i = 0; i < getWords(role).size(); i++) {
			if (getTargetIndexes(role).contains(i)) {
				continue;
			}

			IndexedWord v = getDependencies(role).getNodeByIndex(i + 1);
			IndexedWord p = getDependencies(role).getParent(v);
			if (p == null) {
				continue;
			}
			if (getDependencies(role).reln(p, v).toString().equals("neg")) {
				if (role == Role.CLAIM) {
					claimNegationIndexes.add(i);
					claimNegated.add(p.index() - 1);
				} else {
					topicNegationIndexes.add(i);
					topicNegated.add(p.index() - 1);
				}

			} else if (Settings.negativePrepositions.contains(getWords(role).get(i).toLowerCase())) {
				Set<Integer> indexes = new TreeSet<>();
				indexes = getChildWithRelation(i, "pobj", role);
				if (indexes.isEmpty()) {
					v = getDependencies(role).getNodeByIndex(i + 1);
					p = getDependencies(role).getParent(v);
					if (p == null) {
						continue;
					}
					indexes.add(p.index() - 1);
				}
				if (role == Role.CLAIM) {
					claimNegationIndexes.add(i);
					claimNegated.addAll(indexes);
				} else {
					topicNegationIndexes.add(i);
					topicNegated.addAll(indexes);
				}
			}

		}
	}

	private void findEffects(Role role) {
		for (int i = 0; i < getWords(role).size(); i++) {
			Set<Integer> indexes = new TreeSet<>();

			indexes = getChildWithRelation(i, "dobj", role);
			boolean enough = false;
			for (int index : indexes) {
				if (getSentiment(role, index) != 0.0) {
					enough = true;
				}
			}

			if (!enough) {
				indexes = getChildWithRelationPrefix(i, "nmod", role);
				for (int index : indexes) {
					if (getSentiment(role, index) != 0.0) {
						enough = true;
					}
				}
			}

			if (!enough) {
				indexes = getChildWithRelationPrefix(i, "cobj", role);
				for (int index : indexes) {
					if (getSentiment(role, index) != 0.0) {
						enough = true;
					}
				}
			}

			if (!enough) {
				indexes = getChildWithRelation(i, "nsubjpass", role);
				for (int index : indexes) {
					if (getSentiment(role, index) != 0.0) {
						enough = true;
					}
				}
			}

			if (!enough) {
				indexes = getChildWithRelation(i, "csubjpass", role);
				for (int index : indexes) {
					if (getSentiment(role, index) != 0.0) {
						enough = true;
					}
				}
			}

			if (!enough) {
				indexes = getChildWithRelation(i, "xcomp", role);
				for (int index : indexes) {
					if (getSentiment(role, index) != 0.0) {
						enough = true;
					}
				}
			}

			if (!enough) {
				indexes = new TreeSet<>();
				for (int k : getChildWithRelation(i, "prep", role)) {
					indexes.addAll(getChildWithRelation(k, "pobj", role));
				}
				for (int index : indexes) {
					if (getSentiment(role, index) != 0.0) {
						enough = true;
					}
				}
			}

			if (!indexes.isEmpty()) {
				if (role == Role.CLAIM) {
					claimEffects.put(i, indexes);
				} else {
					topicEffects.put(i, indexes);
				}
			}
		}
	}

	public void effectOnTarget(Role role) {
		for (int i = 0; i < getWords(role).size(); i++) {
			if (getNegationIndexes(role).contains(i) || getTargetIndexes(role).contains(i)) {
				continue;
			}

			Set<Integer> indexes = new TreeSet<>();
			double effect = 0;

			effect = getEffect(role, i);
			if (outputActivated) {
				System.out.println("Effect word: " + getWords(role).get(i) + ", " + effect);
			}
			indexes.addAll(getChildWithRelation(i, "dobj", role));
			indexes.addAll(getChildWithRelation(i, "nsubjpass", role));
			indexes.addAll(getChildWithRelationPrefix(i, "cobj", role));
			indexes.addAll(getChildWithRelation(i, "csubjpass", role));
			indexes.addAll(getChildWithRelationPrefix(i, "nmod", role));
			Set<Integer> prepIndexes = getChildWithRelation(i, "prep", role);
			for (int prepIndex : prepIndexes) {
				indexes.addAll(getChildWithRelation(prepIndex, "pobj", role));
			}
			indexes.addAll(getChildWithRelation(i, "xcomp", role));

			if (effect != 0.0) {
				for (int index : indexes) {
					if (effect > 0) {
						if (role == Role.CLAIM) {
							if (getTargetIndexes(Role.CLAIM).contains(index)) {
								getTargetIndexes(Role.CLAIM).add(i);
								if(claimTargetEffector == -1) {
									claimTargetEffector = i;
								}
							}
						} else {
							if (getTargetIndexes(Role.TOPIC).contains(index)) {
								getTargetIndexes(Role.TOPIC).add(i);
								if(topicTargetEffector == -1) {
									topicTargetEffector = i;
								}
							}
						}
					} else {
						if (role == Role.CLAIM) {
							if (getTargetIndexes(Role.CLAIM).contains(index)) {
								claimTargetReduced = true;
								getTargetIndexes(Role.CLAIM).add(i);
								claimTargetEffector = i;
							}
						} else {
							if (getTargetIndexes(Role.TOPIC).contains(index)) {
								topicTargetReduced = true;
								getTargetIndexes(Role.TOPIC).add(i);
								topicTargetEffector = i;
							}
						}
					}
				}
			}
		}
	}

	private void sentimentRecursively(Role role) {
		boolean change = false;

		for (int i = 0; i < getWords(role).size(); i++) {
			if (getTargetIndexes(role).contains(i) || getNegationIndexes(role).contains(i)) {
				continue;
			}

			Set<Integer> indexes = getChildWithRelation(i, "amod", role);
			indexes.addAll(getChildWithRelation(i, "advmod", role));
			indexes.addAll(getChildWithRelation(i, "nn", role));
			
			double max = 0.0;

			for (int index : indexes) {
				if (Methods.abs(getSentiment(role, index)) > Methods.abs(max)) {
					max = getSentiment(role, index);
				}
			}

			if (max != 0.0 && getSentiment(role, i) != max) {
				change = true;
				if (role == Role.CLAIM) {
					claimSentiment[i] = max;
				} else {
					topicSentiment[i] = max;
				}
			}
		}

		if (change) {
			sentimentRecursively(role);
		}
	}

	public Set<Integer> getClaimTargetIndexes() {
		return claimTargetIndexes;
	}

	public Set<Integer> getTopicTargetIndexes() {
		return topicTargetIndexes;
	}

	public Set<String> getTargetLemmas() {
		return targetLemmas;
	}

	public Set<Integer> getTargetIndexes(Role role) {
		if (role == Role.CLAIM) {
			return claimTargetIndexes;
		} else {
			return topicTargetIndexes;
		}
	}

	public boolean isNegated(Role role, int i) {
		if (role == Role.CLAIM) {
			return claimNegated.contains(i);
		} else {
			return topicNegated.contains(i);
		}
	}

	public Set<Integer> getNegationIndexes(Role role) {
		if (role == Role.CLAIM) {
			return claimNegationIndexes;
		} else {
			return topicNegationIndexes;
		}
	}

	public Map<Integer, Set<Integer>> getHasSubject(Role role) {
		if (role == Role.CLAIM) {
			return claimHasSubject;
		} else {
			return topicHasSubject;
		}
	}

	public Map<Integer, Set<Integer>> getEffects(Role role) {
		if (role == Role.CLAIM) {
			return claimEffects;
		} else {
			return topicEffects;
		}
	}
	
	public boolean targetReduced(Role role) {
		if (role == Role.CLAIM) {
			return claimTargetReduced;
		} else {
			return topicTargetReduced;
		}
	}
	
	public int getTargetEffector(Role role) {
		if (role == Role.CLAIM) {
			return claimTargetEffector;
		} else {
			return topicTargetEffector;
		}
	}
}
