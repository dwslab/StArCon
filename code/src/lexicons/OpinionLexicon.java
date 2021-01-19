package lexicons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import utils.Settings;

public class OpinionLexicon {
	private static final Map<String, Boolean> lexicon;

	static {

		lexicon = new HashMap<String, Boolean>();
		read(Settings.NEGATIVES, false);
		read(Settings.POSITIVES, true);
	}

	public static double getValue(String word) {
		if (lexicon.keySet().contains(word)) {
			double value = lexicon.get(word) ? 1.0 : -1.0;
			return value;
		}
		return 0.0;
	}

	private static void read(String path, boolean value) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(path)));
			while (br.ready()) {
				String line = br.readLine().trim();
				if (line.startsWith(";") || line.isEmpty()) {
					continue;
				} else {
					lexicon.put(line, value);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
