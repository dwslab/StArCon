package data;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.Pair;
import lexicons.LexiconManager;

public class NLPAnnotatedArgument extends Argument {

	private static final StanfordCoreNLP PIPELINE;

	static {
		Properties props = new Properties();
		props.put("language", "english");
		props.setProperty("annotators", "tokenize, ssplit, pos, depparse, lemma");
		props.put("depparse.model", "edu/stanford/nlp/models/parser/nndep/english_SD.gz");

		PIPELINE = new StanfordCoreNLP(props);
	}
	
	protected List<String> claimWords, claimLemmas, claimPosTags, topicWords, topicLemmas, topicPosTags;
	protected double claimSentiment[], topicSentiment[], claimEffect[], topicEffect[];
	protected SemanticGraph claimDependencies, topicDependencies;

	public NLPAnnotatedArgument(NLPAnnotatedArgument arg) {
		super(arg);

		claimWords = arg.claimWords;
		claimLemmas = arg.claimLemmas;
		claimPosTags = arg.claimPosTags;
		claimSentiment = arg.claimSentiment;
		claimEffect = arg.claimEffect;

		claimDependencies = arg.claimDependencies;

		topicWords = arg.topicWords;
		topicLemmas = arg.topicLemmas;
		topicPosTags = arg.topicPosTags;
		topicSentiment = arg.topicSentiment;
		topicEffect = arg.topicEffect;

		topicDependencies = arg.topicDependencies;
	}

	public NLPAnnotatedArgument(Argument arg) {
		super(arg);

		CoreDocument doc = new CoreDocument(claim.replaceAll("\\.", "").replaceAll("\\?", ""));
		PIPELINE.annotate(doc);

		claimWords = getWords(doc);
		claimLemmas = getLemmas(doc);
		claimPosTags = getPosTags(doc);

		claimDependencies = getDependencyGraphs(doc).get(0);

		doc = new CoreDocument(topic.replaceAll("\\.", "").replaceAll("\\?", ""));
		PIPELINE.annotate(doc);

		topicWords = getWords(doc);
		topicLemmas = getLemmas(doc);
		topicPosTags = getPosTags(doc);

		topicDependencies = getDependencyGraphs(doc).get(0);

		sentimentAndEffect();
	}

	private void sentimentAndEffect() {
		claimSentiment = new double[claimWords.size()];
		topicSentiment = new double[topicWords.size()];
		claimEffect = new double[claimWords.size()];
		topicEffect = new double[topicWords.size()];

		for (int i = 0; i < claimWords.size(); i++) {
			if (i < claimWords.size() - 1) {
				claimSentiment[i] = LexiconManager.getBigramValue(claimWords.get(i), claimWords.get(i + 1),
						claimLemmas.get(i), claimLemmas.get(i + 1), true);
				claimSentiment[i+1] = claimSentiment[i];
			}
			claimEffect[i] = LexiconManager.getEffect(claimLemmas.get(i));
			if(claimSentiment[i] == 0.0) {
				claimSentiment[i] = LexiconManager.getSentiment(Role.CLAIM, this, i, true);
			}
		}
		
		for (int i = 0; i < topicWords.size(); i++) {
			if (i < topicWords.size() - 1) {
				topicSentiment[i] = LexiconManager.getBigramValue(topicWords.get(i), topicWords.get(i + 1),
						topicLemmas.get(i), topicLemmas.get(i + 1), true);
				topicSentiment[i+1] = topicSentiment[i];
			}
			topicEffect[i] = LexiconManager.getEffect(topicLemmas.get(i));
			if(topicSentiment[i] == 0.0) {
				topicSentiment[i] = LexiconManager.getSentiment(Role.TOPIC, this, i, true);
			}
		}
	}

	private static List<String> getWords(CoreDocument document) {
		List<CoreLabel> tokens = document.tokens();
		List<String> units = new ArrayList<String>();

		for (CoreLabel token : tokens) {
			units.add(token.originalText());
		}

		return units;
	}

	private static List<String> getLemmas(CoreDocument document) {

		List<CoreLabel> tokens = document.tokens();
		List<String> units = new ArrayList<String>();

		for (CoreLabel token : tokens) {
			units.add(token.lemma().toLowerCase());
		}

		return units;
	}

	private static List<String> getPosTags(CoreDocument document) {

		List<String> posTags = new ArrayList<>();
		for (int i = 0; i < document.sentences().size(); i++) {
			CoreSentence sentence = document.sentences().get(i);
			posTags.addAll(sentence.posTags());
		}

		return posTags;
	}

	public boolean isNegatedInClaim(int index) {
		return claimDependencies.childRelns(claimDependencies.getNodeByIndex(index + 1)).toString().contains("neg");
	}

	public boolean isNegatedInTopic(int index) {
		return topicDependencies.childRelns(topicDependencies.getNodeByIndex(index + 1)).toString().contains("neg");
	}

	public Set<Integer> getChildWithRelation(int index, String relation, Role role) {
		Set<Integer> set = new TreeSet<>();
		SemanticGraph sg = getDependencies(role);
		List<Pair<GrammaticalRelation, IndexedWord>> list = sg.childPairs(sg.getNodeByIndex(index + 1));
		for (Pair<GrammaticalRelation, IndexedWord> pair : list) {
			if (pair.first().toString().equals(relation)) {
				set.add(pair.second().index() - 1);
			}
		}
		return set;
	}

	public Set<Integer> getChildWithRelationPrefix(int index, String relation, Role role) {
		Set<Integer> set = new TreeSet<>();
		SemanticGraph sg = getDependencies(role);
		List<Pair<GrammaticalRelation, IndexedWord>> list = sg.childPairs(sg.getNodeByIndex(index + 1));
		for (Pair<GrammaticalRelation, IndexedWord> pair : list) {
			if (pair.first().toString().startsWith(relation)) {
				set.add(pair.second().index() - 1);
			}
		}
		return set;
	}

	public int rootIndex(Role role) {
		IndexedWord root = getDependencies(role).getFirstRoot();
		return root.index() - 1;
	}

	public static List<SemanticGraph> getDependencyGraphs(CoreDocument doc) {
		ArrayList<SemanticGraph> trees = new ArrayList<>();
		for (int i = 0; i < doc.sentences().size(); i++) {
			CoreSentence sentence = doc.sentences().get(i);
			trees.add(sentence.dependencyParse());
		}
		return trees;
	}

	public List<String> getClaimWords() {
		return claimWords;
	}

	public List<String> getClaimLemmas() {
		return claimLemmas;
	}

	public List<String> getClaimPosTags() {
		return claimPosTags;
	}

	public List<String> getTopicWords() {
		return topicWords;
	}

	public List<String> getTopicLemmas() {
		return topicLemmas;
	}

	public List<String> getTopicPosTags() {
		return topicPosTags;
	}

	public SemanticGraph getClaimDependencies() {
		return claimDependencies;
	}

	public SemanticGraph getTopicDependencies() {
		return topicDependencies;
	}

	public List<String> getWords(Role role) {
		if (role == Role.CLAIM) {
			return claimWords;
		} else {
			return topicWords;
		}
	}

	public List<String> getLemmas(Role role) {
		if (role == Role.CLAIM) {
			return claimLemmas;
		} else {
			return topicLemmas;
		}
	}

	public List<String> getPosTags(Role role) {
		if (role == Role.CLAIM) {
			return claimPosTags;
		} else {
			return topicPosTags;
		}
	}

	public SemanticGraph getDependencies(Role role) {
		if (role == Role.CLAIM) {
			return claimDependencies;
		} else {
			return topicDependencies;
		}
	}

	public String getString(Role role) {
		if (role == Role.CLAIM) {
			return claim;
		} else {
			return topic;
		}
	}
	
	public double getSentiment(Role role, int i) {
		if (role == Role.CLAIM) {
			return claimSentiment[i];
		} else {
			return topicSentiment[i];
		}
	}
	
	public double getEffect(Role role, int i) {
		if (role == Role.CLAIM) {
			return claimEffect[i];
		} else {
			return topicEffect[i];
		}
	}
}
