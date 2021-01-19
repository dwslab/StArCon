package lexicons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/** 
 * This class connects to the thesaurus.plus website to create an antonym/synonym lexicon which is locally stored for future use.
 */
public class Thesaurus {

	private final static String address = "https://thesaurus.plus/";
	private final static Map<String, Set<String>> synonyms, antonyms;

	static {
		synonyms = new HashMap<String, Set<String>>();
		antonyms = new HashMap<String, Set<String>>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("./resources/lexicons/syns.tsv")));
			
			while(br.ready()) {
				String line = br.readLine().trim();
				if(!line.isEmpty()) {
					String[] split = line.split("\t");
					Set<String> set = new TreeSet<>();
					for(int i = 1; i<split.length; i++) {
						set.add(split[i]);
					}
					synonyms.put(split[0], set);
				}
			}
			
			br.close();
			
			br = new BufferedReader(new FileReader(new File("./resources/lexicons/ants.tsv")));
			
			while(br.ready()) {
				String line = br.readLine().trim();
				if(!line.isEmpty()) {
					String[] split = line.split("\t");
					Set<String> set = new TreeSet<>();
					for(int i = 1; i<split.length; i++) {
						set.add(split[i]);
					}
					antonyms.put(split[0], set);
				}
			}
			
			br.close();
		} catch(FileNotFoundException e) {
			File folder = new File("./resources/lexicons/");
			folder.mkdirs();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Set<String> getSynonyms(String word) {
		if (synonyms.containsKey(word)) {
			return synonyms.get(word);
		} else {
			Set<String> set = get(word, 1);
			synonyms.put(word, set);

			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./resources/lexicons/syns.tsv"), true));
				bw.write(word);
				for (String w : set) {
					bw.write("\t" + w);
				}
				bw.newLine();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return set;
		}
	}

	public static Set<String> getAntonyms(String word) {
		if (antonyms.containsKey(word)) {
			return antonyms.get(word);
		} else {
			Set<String> set = get(word, 2);
			antonyms.put(word, set);

			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./resources/lexicons/ants.tsv"), true));
				bw.write(word);
				for (String w : set) {
					bw.write("\t" + w);
				}
				bw.newLine();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return set;
		}
	}

	public static boolean isSynonymous(String w1, String w2) {
		Set<String> s1 = getSynonyms(w1);
		Set<String> s2 = getSynonyms(w2);
		if (s1 == null || s2 == null) {
			return false;
		}
		if (s1.contains(w2) || s2.contains(w1)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isAntonymous(String w1, String w2) {
		Set<String> s1 = getAntonyms(w1);
		Set<String> s2 = getAntonyms(w2);
		if (s1 == null || s2 == null) {
			return false;
		}
		if (s1.contains(w2) || s2.contains(w1)) {
			return true;
		} else {
			return false;
		}
	}

	// Uses only overview page. 1 = synonyms, 2 = antonyms
	private static Set<String> get(String word, int j) {
		Set<String> set = null;
		try {
			URL url = new URL(address + "thesaurus/" + word);
			Scanner scanner = new Scanner(new InputStreamReader(url.openStream()));

			String site = scanner.nextLine();
			scanner.close();

			String split[] = site.split(
					"(<ul class=\"list paper\">)|(<div class=\"paper\"> <div class=\"paper_content paper_text\">)");

			set = new TreeSet<String>();

			String s[] = split[j].split("<a href=\"https://thesaurus.plus/thesaurus/");
			for (int i = 1; i < s.length; i++) {
				set.add(s[i].substring(0, s[i].indexOf("\"")));
			}

		} catch (FileNotFoundException e) {
			return new TreeSet<String>();
		} catch (IOException e) {
			System.err.println(word + " - " + j);
			System.err.println(e);
		}

		return set;
	}

}
