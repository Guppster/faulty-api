package TokenContainer;

import init.NLP.POSTager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import SCEHashMap.HashMapSearch;
import Stemmer.StemmToken;

public class SentenceRepresentation{
	public static StemmToken ST = new StemmToken();
	private POSTager Tager;
	private String Sentence;
	private List<Token> Tokens = new ArrayList<Token>();
	private JSONObject preTokens;
	private String[] punctuation = { ".", ",", "!", ";", "?", ":" };
	private HashMapSearch hms;
	List<Token> Verbs = new ArrayList<Token>();
	List<Token> Nouns = new ArrayList<Token>();

	// private List<String> FunctionsList = null;
	// private List<Token> Functions = null;

	public SentenceRepresentation(String input){
		Sentence = input;
	}

	public void setTagger(POSTager Tager){
		this.Tager = Tager;
	}

	public void Tag(){
		Token T = null;
		try {
			preTokens = Tager.POSTag(Sentence);
			JSONArray intermediateTags = preTokens.getJSONArray("tags");
			JSONArray intermediateToks = preTokens.getJSONArray("tokens");
			for (int i = 0; i < intermediateTags.length(); i++) {
				T = new Token(intermediateToks.getString(i), intermediateTags.getString(i), i);
				Tokens.add(T);
			}
		} catch (JSONException jne) {
			System.out.println(jne.getMessage());
		}
		// Remove Last
		int LastTokenLength = T.getToken().length();
		if (Arrays.asList(punctuation).contains(T.getToken().substring(LastTokenLength - 1)))
			T.setToken(T.getToken().substring(0, LastTokenLength - 1));
	}

	/**
	 * print() prints in a formatted and user friendly manner all Tokens alongside their respective Tags that are found in the
	 * Tokens(List<Token>) field of the variable SentenceRepresentation only if Tokens is non-Empty List
	 */
	public void print(){
		if (Tokens.isEmpty()) {
			return;
		} else {
			System.out.println("				*******BEGIN*******				");
			for (Token t : Tokens) {
				System.out.format("%40s | %5s \n", t.getToken(), t.getTag());
			}
			System.out.println("				########END########				");
		}
	}

	/**
	 * removeNoiseBasedTokens is a function which receives as input an Array of String TestInput and removes from the List<Token> Tokens of
	 * the object SentenceRepresentation all such Tokens which contain a String that is also found in the Array of String TestInput.
	 * 
	 * @param TestInput
	 */
	public void removeNoiseBasedToken(String[] TestInput){
		List<Token> toRemove = new ArrayList<Token>();
		Set<String> Unwanted = new HashSet<String>();
		Unwanted.addAll(Arrays.asList(TestInput));
		Iterator<Token> T = Tokens.iterator();
		while (T.hasNext()) {
			Token t = T.next();
			if (Unwanted.contains(t.getToken().toLowerCase()))
				toRemove.add(t);
		}
		Tokens.removeAll(toRemove);
	}

	/**
	 * removeNoiseBasedTag is a function which receives as input an Array of String TestInput and removes from the List<Token> Tokens of the
	 * object SentenceRepresentation all such Tokens which contain a Tag that is also found in the Array of String TestInput.
	 * 
	 * @param TestInput
	 */
	public void removeNoiseBasedTag(String[] TestInput){
		List<Token> toRemove = new ArrayList<Token>();
		Iterator<Token> T = Tokens.iterator();
		while (T.hasNext()) {
			Token t = T.next();
			if (Arrays.asList(TestInput).contains(t.getTag())) {
				toRemove.add(t);
			}
		}
		Tokens.removeAll(toRemove);
	}

	/**
	 * removeDuplicatesBasedToken is a function which alters the Tokens (List<Token>) field of the object SentenceRepresentation by removing
	 * all Tokens whose String value (obtained through the .getToken() method appears more than once in any given dataset.
	 */
	public void removeDuplicatesBasedToken(){
		Set<String> Unique = new HashSet<String>();
		List<Token> toRemove = new ArrayList<Token>();
		for (Token t : Tokens) {
			if (Unique.contains(t.getToken().toLowerCase())) {
				toRemove.add(t);
			} else {
				Unique.add(t.getToken().toLowerCase());
			}
		}
		Tokens.removeAll(toRemove);
	}

	public void select(String[] VerbTags, String[] NounTags){
		Iterator<Token> T = Tokens.iterator();
		while (T.hasNext()) {
			Token t = T.next();
			if (Arrays.asList(VerbTags).contains(t.getTag())) {
				Verbs.add(t);
			}
			if (Arrays.asList(NounTags).contains(t.getTag())) {
				Nouns.add(t);
			}
		}
		Tokens.removeAll(Verbs);
		Tokens.removeAll(Nouns);
		Tokens = Nouns;
	}

	/**
	 * RemoveNonSCE() Iterates through the List<Token> Tokens and keeps only those which hold a String (accessible through Token.getToken())
	 * which returns true when passed as argument in HashMapSearch.hashMapSearch(String s)
	 */
	public void removeNonSCE(){
		List<Token> toRemove = new ArrayList<Token>();
		List<Token> toKeep = new ArrayList<Token>();
		Iterator<Token> T = Tokens.iterator();
		while (T.hasNext()) {
			Token t = T.next();
			if (hms.hashMapSearch(t.getToken()) || hms.hashMapSearch(ST.StemIt(t.getToken()))) {
				// System.out.println(t.getToken());
				toKeep.add(t);
			}
			if (!hms.hashMapSearch(t.getToken()) && !hms.hashMapSearch(ST.StemIt(t.getToken()))) {
				toRemove.add(t);
			}
		}

		/* following code subject to DELETION for (Token t : Tokens) { toRemove.add(t); } toRemove.removeAll(toKeep);
		 * Tokens.removeAll(toRemove); */
		Tokens = toKeep;
	}

	public void setHashMap(HashMapSearch hms){
		this.hms = hms;
	}

	public void merge(){
		List<Token> merged = new ArrayList<Token>();
		Iterator<Token> VerbT = Verbs.iterator();
		Iterator<Token> NounT = Nouns.iterator();
		Token Verb;
		Token Noun;
		if (Verbs.isEmpty()) {
			merged = Nouns;
			Tokens = merged;
			return;
		} else {
			Verb = VerbT.next();
		}
		if (Nouns.isEmpty()) {
			merged = Verbs;
			Tokens = merged;
			return;
		} else {
			Noun = NounT.next();
		}

		while (VerbT.hasNext() || NounT.hasNext()) {
			if (Verb.getPosition() < Noun.getPosition()) {

				merged.add(Verb);
				if (VerbT.hasNext()) {
					Verb = VerbT.next();
				} else {
					merged.add(Noun);
					while (NounT.hasNext()) {
						Noun = NounT.next();
						merged.add(Noun);
					}
				}
			} else {
				merged.add(Noun);
				if (NounT.hasNext()) {
					Noun = NounT.next();
				} else {
					merged.add(Verb);
					while (VerbT.hasNext()) {
						Verb = VerbT.next();
						merged.add(Verb);
					}
				}
			}
		}
		Tokens = merged;
		return;
	}

	public List<String> getTokens(){
		List<String> Bucket = new ArrayList<String>();
		// TODO write the create buckets method;
		for (Token t : Tokens) {
			Bucket.add(t.getToken());
		}
		return Bucket;
	}

	// public void setFunctions(List<String> Functions){
	// this.FunctionsList = Functions;
	// }

	// public void SeparateFunctions(){
	// List<Token> ToSeparate = new ArrayList<Token>();
	// // TODO write the separate Functions method;
	// return;
	// }

	public int countSCE(){
		return Tokens.size();
	}
}
