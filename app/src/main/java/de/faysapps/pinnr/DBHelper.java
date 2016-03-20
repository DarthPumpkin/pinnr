package de.faysapps.pinnr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import de.faysapps.trie.Trie;

public class DBHelper {

	public final static HashMap<Character, String> STANDARD_MAP;
	// public final static Filter GERMAN = Filter.GERMAN;
	// public final static Filter VOCAL = Filter.VOCAL;

	String code;
	// private ArrayList<String> allWords;
	private Trie allWords;
	private Map<Character, String> map;

	// private static enum Filter {
	// GERMAN, VOCAL
	// };

	static {
		STANDARD_MAP = new HashMap<Character, String>();
		STANDARD_MAP.put('1', "1");
		STANDARD_MAP.put('2', "abc");
		STANDARD_MAP.put('3', "def");
		STANDARD_MAP.put('4', "ghi");
		STANDARD_MAP.put('5', "jkl");
		STANDARD_MAP.put('6', "mno");
		STANDARD_MAP.put('7', "pqrs");
		STANDARD_MAP.put('8', "tuv");
		STANDARD_MAP.put('9', "wxyz");
		STANDARD_MAP.put('0', "0");
	}

	public DBHelper() {
		map = STANDARD_MAP;
	}

	public void setCode(String code) {
		this.code = code;
		allWords = null;
	}

	/**
	 * change the mapping used to translate the code into words. To restore
	 * default (the mobile phone keyboard), use {@link #STANDARD_MAP} as
	 * parameter
	 * 
	 * @param map
	 *            the map to be used
	 */
	public void setMap(Map<Character, String> map) {
		this.map = map;
	}

	/**
	 * generates all combinations of words creatable by replacing each digit in
	 * the code by its respective letter on a mobile phone keyboard. You may
	 * also adjust the mapping to your needs by using setMap. Note that you need
	 * to set a code before calling this method, otherwise an exception will be
	 * thrown. <br>
	 * This method is blocking!
	 * 
	 * @return array of all possible words
	 * @throws Exception
	 *             if there has no code been set
	 */
	public Set<String> generateAllWords() throws Exception {
		if (code == null)
			throw new Exception("Could not generate words: No code specified");
		if (allWords != null)
			return new CopyOnWriteArraySet<String>(allWords.getWords(""));
		Set<String> temp = new CopyOnWriteArraySet<String>();
		temp.add("");
		for (int i = 0; i < code.length(); i++) {
			String letters = map.get(code.charAt(i));
			Set<String> tempNew = new CopyOnWriteArraySet<String>();
			for (int j = 0; j < letters.length(); j++) {
				for (String s : temp) {
					tempNew.add(s + letters.charAt(j));
				}
			}
			temp = tempNew;
		}

		/*
		 * sum up all chars in the map and use as charset for the trie
		 */
		StringBuilder validChars = new StringBuilder("");
		for (char key : map.keySet()) {
			validChars.append(map.get(key));
		}
		this.allWords = new Trie(validChars.toString().toCharArray());
		/*
		 * insert the words into the trie while concurrently removing them from
		 * the set to save memory
		 */
		for (String word : temp) {
			this.allWords.addWord(word);
			temp.remove(word);
		}
		return temp;
	}

	/**
	 * Generates words that contain at least one vocal. <br>
	 * This method is blocking and can be very time consuming, depending on the
	 * code's length and your specified mapping.
	 * 
	 * @return array of filtered words
	 * @throws Exception
	 *             if words have not been generated yet AND there is no code
	 *             specified
	 */
	public String[] generateVocalWords() throws Exception {
		if (allWords == null)
			generateAllWords();
		ArrayList<String> filteredWordsList = new ArrayList<String>();
		// UGLY, needs to be improved
		for (Object obj : allWords.getWords("")) {
			String word = (String) obj;
			if (word.contains("a") || word.contains("e") || word.contains("i")
					|| word.contains("o") || word.contains("u")
					|| word.contains("y")) {
				filteredWordsList.add(word);
			}
		}
		String[] filteredWords = new String[filteredWordsList.size()];
		for (String word : filteredWordsList) {
			filteredWords[filteredWordsList.indexOf(word)] = word;
		}
		return filteredWords;
	}

	/**
	 * uses the given List<String> as list of valid words
	 * 
	 * @param validWords
	 *            list of valid words
	 * @return array of filtered words
	 * @throws Exception
	 */
	public String[] generateFilteredWords(List<String> validWords)
			throws Exception {
		if (allWords == null)
			generateAllWords();
		ArrayList<String> filteredWordsList = new ArrayList<String>();
		for (String validWord : validWords) {
			// if (allWords.contains(validWord)) {
			// filteredWordsList.add(validWord);
			// }
			try {
				if (validWord.length() != code.length()
						|| allWords.getWords(validWord).size() == 0) {
					continue;
				} else {
					filteredWordsList.add(validWord);
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new RuntimeException("failed to search for " + validWord
						+ "(#" + validWords.indexOf(validWord) + ")");
			}

		}
		String[] filteredWords = new String[filteredWordsList.size()];
		for (String word : filteredWordsList) {
			filteredWords[filteredWordsList.indexOf(word)] = word;
		}
		return filteredWords;
	}

}
