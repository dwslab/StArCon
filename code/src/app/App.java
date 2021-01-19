package app;

import java.util.Map;
import java.util.Set;

import data.Argument.Role;
import data.ArgumentManager;
import data.RichArgument;
import utils.Methods;
import utils.Settings;
import utils.Stance;

public class App {

	private RichArgument arg;
	private boolean outputActivated;

	public App(RichArgument argument, boolean outputActivated) {
		this.arg = argument;
		this.outputActivated = outputActivated;
	}

	public Stance predict() {
		if (outputActivated) {
			System.out.println(arg);
			if (arg.getTargetLemmas().size() > 0) {
				System.out.println("Targets: " + arg.getTargetLemmas());
			}
		}

		int r = arg.opposingTargets() ? -1 : 1;
		if (outputActivated) {
			System.out.println(" - R = " + r);
			System.out.println("Topic:");
		}

		/** Calculating Topic Stance */
		boolean effected = false;
		boolean negated = false;

		if (arg.getTargetEffector(Role.TOPIC) != -1) {
			effected = true;
		}

		int topicStance = 1;
		if (arg.targetReduced(Role.TOPIC)) {
			topicStance *= -1;
		}

		for (int i : arg.getTopicTargetIndexes()) {
			if (arg.isNegated(Role.TOPIC, i)) {
				topicStance *= -1;
				negated = true;
			}
		}

		if (outputActivated) {
			System.out.print(" - S = " + topicStance);
			if (effected) {
				System.out.print(
						" (effected by " + arg.getWords(Role.TOPIC).get(arg.getTargetEffector(Role.TOPIC)) + ")");
			}
			if (negated) {
				System.out.print(" (negated)");
			}
			System.out.println();
			System.out.println("Claim:");
		}

		/** Calculating Claim Stance */
		Map<Integer, Set<Integer>> hasSubject = arg.getHasSubject(Role.CLAIM);
		Map<Integer, Set<Integer>> effects = arg.getEffects(Role.CLAIM);

		double max = 0.0;
		int subj = -1;
		int pred = -1;
		int pred_val = 1;
		int obj = -1;
		int obj_val = 1;

		// SPO Triples
		if (max == 0.0) {
			for (int e : effects.keySet()) {
				double effect = arg.getEffect(Role.CLAIM, e);
				if (effect == 0.0) {
					continue;
				}
				if (hasSubject.containsKey(e)) {
					for (int s : hasSubject.get(e)) {
						if (arg.getTargetIndexes(Role.CLAIM).contains(s)) {
							for (int o : effects.get(e)) {
								double sentiment = arg.getSentiment(Role.CLAIM, o);
								if (Methods.abs(sentiment * effect) > Methods.abs(max)) {
									max = sentiment * effect;
									subj = s;
									pred = e;
									pred_val = Methods.sgn(effect);
									obj = o;
									obj_val = Methods.sgn(sentiment);
								}
							}
						}
					}
				}
			}
		}

		// TP Pairs
		if (max == 0.0) {
			for (int o : hasSubject.keySet()) {
				double sentiment = arg.getSentiment(Role.CLAIM, o);
				if (sentiment == 0.0) {
					continue;
				}
				for (int s : hasSubject.get(o)) {
					if (arg.getTargetIndexes(Role.CLAIM).contains(s)) {
						if (Methods.abs(sentiment) > Methods.abs(max)) {
							max = sentiment;
							subj = s;
							pred = o;
							pred_val = Methods.sgn(sentiment);
						}
					}
				}
			}
		}

		// PO pairs (high priority = predicates having a subject)
		if (max == 0.0) {
			for (int e : effects.keySet()) {
				double effect = arg.getEffect(Role.CLAIM, e);
				if (effect == 0.0) {
					continue;
				}
				if (hasSubject.containsKey(e)) {
					for (int o : effects.get(e)) {
						double sentiment = arg.getSentiment(Role.CLAIM, o);
						if (Methods.abs(sentiment * effect) > Methods.abs(max)) {
							max = sentiment * effect;
							pred = e;
							pred_val = Methods.sgn(effect);
							obj = o;
							obj_val = Methods.sgn(sentiment);
						}
					}

				}
			}
		}

		// PO Pairs (low priority)
		if (max == 0.0) {
			for (int e : effects.keySet()) {
				double effect = arg.getEffect(Role.CLAIM, e);
				if (effect == 0.0) {
					continue;
				}
				for (int o : effects.get(e)) {
					double sentiment = arg.getSentiment(Role.CLAIM, o);
					if (Methods.abs(sentiment * effect) > Methods.abs(max)) {
						max = sentiment * effect;
						pred = e;
						pred_val = Methods.sgn(effect);
						obj = o;
						obj_val = Methods.sgn(sentiment);
					}
				}
			}
		}

		// Heuristic
		int heuristic = 0;
		if (max == 0.0) {
			for (int i = 0; i < arg.getWords(Role.CLAIM).size(); i++) {
				if (arg.isNegated(Role.CLAIM, i)) {
					max += -arg.getSentiment(Role.CLAIM, i);
				} else {
					max += arg.getSentiment(Role.CLAIM, i);
				}
			}
			for (int i : arg.getClaimTargetIndexes()) {
				if (arg.isNegated(Role.CLAIM, i)) {
					max *= -1.0;
					if (outputActivated) {
						System.out.println("Claim Target Negated.");
					}
				}
			}
			if (max == 0.0) {
				max = 1.0;
			}
			heuristic = Methods.sgn(max);
		}

		effected = false;
		boolean s_negated = false;

		int s = 1;
		if (arg.getTargetEffector(Role.CLAIM) != -1) {
			effected = true;
		}

		if (arg.targetReduced(Role.CLAIM)) {
			s *= -1;
		}

		if (arg.isNegated(Role.CLAIM, subj)) {
			s *= -1;
			s_negated = true;
		}

		boolean p_negated = false;
		if (arg.isNegated(Role.CLAIM, pred)) {
			p_negated = true;
			pred_val *= -1;
		}

		boolean o_negated = false;
		if (arg.isNegated(Role.CLAIM, obj)) {
			o_negated = true;
			obj_val *= -1;
		}

		if (outputActivated) {
			System.out.print(" - S = " + s);
			if (effected) {
				System.out.print(
						" (effected by " + arg.getWords(Role.CLAIM).get(arg.getTargetEffector(Role.CLAIM)) + ")");
			}
			if (s_negated) {
				System.out.print(" (negated)");
			}
			System.out.println();
			if (pred >= 0) {
				System.out.print(" - P = " + pred_val + " (" + arg.getClaimWords().get(pred) + ")");
				if (p_negated) {
					System.out.print(" (negated)");
				}
				System.out.println();
			}
			if (obj >= 0) {
				System.out.print(" - O = " + obj_val + " (" + arg.getClaimWords().get(obj) + ")");
				if (o_negated) {
					System.out.print(" (negated)");
				}
				System.out.println();
			}

			if (pred < 0 && obj < 0) {
				System.out.println(" - Heurstic = " + heuristic);
			}
		}

		double stance;
		if (heuristic == 0) {
			stance = topicStance * s * pred_val * obj_val * r;
		} else {
			stance = topicStance * s * heuristic * r;
		}

		if (stance < 0) {
			return Stance.CON;
		} else {
			return Stance.PRO;
		}
	}

	public static int evaluate() {
		int truePro = 0;
		int trueCon = 0;
		int falsePro = 0;
		int falseCon = 0;

		for (RichArgument arg : ArgumentManager.getRichArguments()) {
			if(arg.getStance() == Stance.UNKNOWN) {
				System.err.println("An evaluation is only possible when the stances are known. Please add a third column containing the stance for each topic-claim pair.");
				System.exit(0);
			}
			
			Stance prediction = new App(arg, false).predict();
			if (prediction == Stance.CON) {
				if (arg.getStance() == Stance.CON) {
					truePro++;
				} else {
					falsePro++;
				}
			} else if (prediction == Stance.PRO) {
				if (arg.getStance() == Stance.PRO) {
					trueCon++;
				} else {
					falseCon++;
				}
			}
		}
		System.out.println();

		int total = truePro + trueCon + falsePro + falseCon;

		double precisionPro = (double) truePro / (double) (truePro + falsePro);
		double recallPro = (double) truePro / (double) (truePro + falseCon);
		double precisionCon = (double) trueCon / (double) (trueCon + falseCon);
		double recallCon = (double) trueCon / (double) (trueCon + falsePro);
		double f1Pro = 2.0 * precisionPro * recallPro / (precisionPro + recallPro);
		double f1Con = 2.0 * precisionCon * recallCon / (precisionCon + recallCon);

		System.out.println("Correctly Classified: " + (truePro + trueCon) + " out of " + total);
		System.out.println("Con classified as Con: " + truePro);
		System.out.println("Pro classified as Pro: " + trueCon);
		System.out.println("Pro classified as Con: " + falseCon);
		System.out.println("Con classified as Pro: " + falsePro);
		System.out.println("Accuracy: " + ((double) (truePro + trueCon) / (double) total));
		System.out.println("F1 score (pro): " + f1Con + ", ");
		System.out.println("F1 score (con): " + f1Pro + ", ");
		System.out.println("F1 score (macro): " + ((f1Pro + f1Con) / 2.0) + ", ");

		return total;
	}

	public static void predictions() {
		for (RichArgument arg : ArgumentManager.getRichArguments()) {

			Stance prediction = new App(arg, true).predict();
			System.out.println("Prediction: " + prediction);
			System.out.println("--------------------------");
		}
	}

	public static void main(String[] args) {

		for (String arg : args) {
			String name = arg.split("=")[0].toLowerCase();
			String value = arg.split("=")[1];
			switch (name) {
			case "dataset":
				Settings.DATASET = value;
				break;
			case "predict":
				Settings.predict = Boolean.parseBoolean(value);
				break;
			case "evaluate":
				Settings.evaluate = Boolean.parseBoolean(value);
				break;
			case "ewn":
				Settings.EFFECT_WORDNET = Boolean.parseBoolean(value);
				break;
			case "effectlexicon":
				Settings.EFFECT_LEXICON = value;
				break;
			case "mpqa":
				Settings.MPQA = value;
				break;
			case "bigrams":
				Settings.BIGRAMS = value;
				break;
			case "unigrams":
				Settings.UNIGRAMS = value;
				break;
			case "positives":
				Settings.POSITIVES = value;
				break;
			case "negatives":
				Settings.NEGATIVES = value;
				break;
			case "wordnet":
				Settings.WORDNET = value;
				break;
			default:
				System.err.println("Unknown Parameter: " + name);
				System.exit(0);
			}
		}

		if (Settings.predict) {
			predictions();
		}
		if (Settings.evaluate) {
			evaluate();
		}
	}
}
