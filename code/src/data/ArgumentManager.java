package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import utils.Settings;

public class ArgumentManager {

	private static Set<RichArgument> richArguments;
	private static Set<NLPAnnotatedArgument> nlpAnnotatedArguments;
	private static Set<Argument> arguments;

	private static void readArguments() {
		arguments = new HashSet<Argument>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(Settings.DATASET)));
			while(br.ready()) {
				String line = br.readLine().trim();
				if(!line.isEmpty()) {
					String split[] = line.split("\t");
					Argument arg = null;
					if(split.length == 3) {
						arg = new Argument(split[0].trim(), split[1].trim(), split[2].trim());
					} else if(split.length == 2) {
						arg = new Argument(split[0].trim(), split[1].trim());
					} else {
						System.err.println("Unexpected Format in " + Settings.DATASET + ": " + line);
						System.exit(0);
					}
					arguments.add(arg);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static void nlpAnnotate() {
		nlpAnnotatedArguments = new HashSet<NLPAnnotatedArgument>();

		System.out.println("CoreNLP and Lexical Annotations ...");

		if (arguments == null) {
			readArguments();
		}

		for (Argument argument : arguments) {
			NLPAnnotatedArgument arg = new NLPAnnotatedArgument(argument);
			nlpAnnotatedArguments.add(arg);
		}

		System.out.println("CoreNLP and Lexical Annotations finished.");
	}

	private static void enrichArguments() {
		if (nlpAnnotatedArguments == null) {
			nlpAnnotate();
		}
		richArguments = new HashSet<RichArgument>();

		System.out.println("Analyzing Arguments ...");

		for (NLPAnnotatedArgument argument : nlpAnnotatedArguments) {
			RichArgument arg = new RichArgument(argument);
			richArguments.add(arg);
		}
		System.out.println("Analysis finished.");
	}

	public static Set<NLPAnnotatedArgument> getNLPAnnotatedArguments() {
		if (nlpAnnotatedArguments == null) {
			nlpAnnotate();
		}
		return nlpAnnotatedArguments;
	}

	public static Set<RichArgument> getRichArguments() {
		if (richArguments == null) {
			enrichArguments();
		}
		return richArguments;
	}
}
