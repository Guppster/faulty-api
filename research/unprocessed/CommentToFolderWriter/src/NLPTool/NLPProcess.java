package NLPTool;

import init.NLP.POSTager;
import init.NLP.SentenceDetection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import SCEHashMap.AVLEntry;
import SCEHashMap.HashMapSearch;
import TokenContainer.SentenceRepresentation;

public class NLPProcess{

	// public static final String ANSI_RESET = "\u001B[0m";
	// public static final String ANSI_PURPLE = "\u001B[35m";
	private static int TotalSCECount = 0;
	private String testParagraph = "";
	private POSTager postager;
	private SentenceDetection sd;
	// private Tokenizer toke;
	private AVLEntry avlEntry;
	private HashMapSearch hms;
	private int HashSize = 103;
	String[] mySentence = null;
	String[] myTokens = null;
	JSONObject myPOSTags = null;
	String[] punctuationArr = { ".", ",", "!", ";", "?", ":", "..." };
	String[] Punctuation = { ".", ",", "!", ";", "?", ":", "...", "*", ">", "<", "=" };
	String[] UndesiredTags = { "CD", "DT", "IN" };
	String[] VerbTags = { "VB", "VBD", "VBG", "VBN", "VBP", "VBZ" };
	String[] NounTags = { "NNS", "NN", "NNP", "NNPS" };

	String[] StopWords = { "\"", "~", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+", "`", "-", "=", "{", "}", "[", "]", "|",
			"\\", ":", ";", ",", "\'", ".", "<", ">", "/", "?", "/", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
			"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
			"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "",
			"&&", "||", "a's", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again",
			"against", "ain't", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am",
			"among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere",
			"apart", "appear", "appreciate", "appropriate", "are", "aren't", "around", "as", "aside", "ask", "asking", "associated", "at",
			"available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand",
			"behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but",
			"by", "c'mon", "c's", "came", "can", "can't", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes",
			"clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing",
			"contains", "corresponding", "could", "couldn't", "course", "currently", "definitely", "described", "despite", "did", "didn't",
			"different", "do", "does", "doesn't", "doing", "don't", "done", "down", "downwards", "during", "each", "edu", "eg", "eight",
			"either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody",
			"everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "fifth", "first", "five",
			"followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get",
			"gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadn't", "happens",
			"hardly", "has", "hasn't", "have", "haven't", "having", "he", "he's", "hello", "help", "hence", "her", "here", "here's",
			"hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how",
			"howbeit", "however", "i'd", "i'll", "i'm", "i've", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed",
			"indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isn't", "it", "it'd", "it'll",
			"it's", "its", "itself", "just", "keep", "keeps", "kept", "know", "known", "knows", "last", "lately", "later", "latter",
			"latterly", "least", "less", "lest", "let", "let's", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd",
			"mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much",
			"must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never",
			"nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel",
			"now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto",
			"or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own",
			"particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides",
			"que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively",
			"respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem",
			"seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several",
			"shall", "she", "should", "shouldn't", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime",
			"sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup",
			"sure", "t's", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "that's", "thats", "the",
			"their", "theirs", "them", "themselves", "then", "thence", "there", "there's", "thereafter", "thereby", "therefore", "therein",
			"theres", "thereupon", "these", "they", "they'd", "they'll", "they're", "they've", "think", "third", "this", "thorough",
			"thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward",
			"towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely",
			"until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via",
			"viz", "vs", "want", "wants", "was", "wasn't", "way", "we", "we'd", "we'll", "we're", "we've", "welcome", "well", "went",
			"were", "weren't", "what", "what's", "whatever", "when", "whence", "whenever", "where", "where's", "whereafter", "whereas",
			"whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "who's", "whoever", "whole",
			"whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "won't", "wonder", "would", "wouldn't", "yes",
			"yet", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves", "zero" };
	ArrayList<String> nounsList = new ArrayList<String>(); // nounsList toString() so that it stays unaffected
	HashMap<String, JSONObject> verbNounRelation = new HashMap<>();
	HashMap<String, String> verbNounForGraph = new HashMap<>();
	JSONObject resultsJSON = null;
	SentenceRepresentation SR;
	int plithos = 2;
	private List<List<String>> NGramms;
	// private List<Map<String, List<String>>> BucketList;
	// private RelationSearcher RS;
	// private List<String> Functions;
	private List<String> CommentBasket = new ArrayList<String>();

	// private List<Sring>

	public NLPProcess(String product){
		postager = new POSTager();
		sd = new SentenceDetection();
		// toke = new Tokenizer();
		avlEntry = new AVLEntry(product, HashSize);
		hms = new HashMapSearch(avlEntry.CreateAVLHashMap(), HashSize);
		NGramms = new ArrayList<List<String>>();
		// BucketList = new ArrayList<Map<String, List<String>>>();
		// RS = new RelationSearcher();
		// RS.FindRelations();
		// Functions = RS.getFunctionMethod();
	}

	public int getSCECount(){
		return TotalSCECount;
	}

	public void clearCommentBasket(){
		CommentBasket.clear();

	}

	public void Process(String input){
		// NGramms.clear();
		testParagraph = input;
		mySentence = sd.SentenceDetect(testParagraph);
		// List<List<String>> Buffer;
		for (int i = 0; i < mySentence.length; i++) {
			SR = new SentenceRepresentation(mySentence[i]);
			SR.setTagger(postager);
			SR.Tag();
			SR.setHashMap(hms);
			SR.removeDuplicatesBasedToken();
			SR.removeNonSCE();
			SR.removeNoiseBasedToken(StopWords);
			SR.removeNoiseBasedToken(Punctuation);
			SR.removeNoiseBasedTag(UndesiredTags);
			// SR.print();
			// SR.removeNoiseBasedTag(VerbTags);
			// SR.select(VerbTags, NounTags);
			// for (String s : SR.getTokens()) {
			// System.out.println(s);
			// }
			CommentBasket.addAll(SR.getTokens());
			// SR.setFunctions();
			// SR.SeparateFunctions();
			// SR.merge();
			// Buffer = SR.createNGramms(plithos);
			// BucketList.addAll(SR.createBuckets());
			// NGramms.addAll(SR.createNGramms(plithos));
			// if(!Buffer.isEmpty())
			// if(!Buffer.get(0).isEmpty())
			// System.out.println(Buffer.get(0).get(0));
			// ListNGramm
			TotalSCECount += SR.countSCE();
			// System.out.println("*********\n" + TotalSCECount + "\n############");
		}
	}

	public List<String> getCommentBasket(){
		return CommentBasket;
	}

	public void spam(){
		// NGramms = SR.createNGramms(plithos);
		// SR.print();
		// System.out.println("-------------------------------------NEXT BUG----------------------------------------");

		// //// int tagIndex=0;
		// ////
		// //// for (String element : tagList) {
		// //// if(tagIndex<tokensWithoutTags.length){
		// ////// if(element.equals("DT")){
		// ////// System.out.println("Article found!!");
		// ////// }
		// //// System.out.println("/fail");
		// //// if
		// ((element.equals("VB"))||(element.equals("VBD"))||(element.equals("VBG"))||(element.equals("VBN"))||(element.equals("VBP"))||(element.equals("VBZ"))){
		// //// System.out.println("Verb found: "+element + " with index" + tagIndex);
		// //// //find verbs' index
		// //// System.out.println("The verb is: "+tokensWithoutTags[tagIndex]);
		// ////
		// //// //STEP5 find NN around each verb (in a vicinity of 2 tokens)
		// //// //nouns have the following abbreviations: NN, NNP, NNPS, NNS
		// //// System.out.println("STEP5 Searching for NOUNS around the verb...");
		// ////
		// //// int neighbour = 2;
		// ////
		// //// //first clear Arraylist
		// //// nounsList.clear();
		// ////
		// //// for(int counter=1;counter<neighbour+1;counter++){
		// //// //check tagList elements in verb's vicinity
		// ////
		// //// if (tagIndex-counter >= 0 && tagIndex-counter<tagList.size()){
		// //// if
		// (tagList.get(tagIndex-counter).equals("NNS")||tagList.get(tagIndex-counter).equals("NN")||tagList.get(tagIndex-counter).equals("NNP")||tagList.get(tagIndex-counter).equals("NNPS")){
		// //// System.out.println("Noun is: "+tokensWithoutTags[tagIndex-counter]);
		// //// // if (Arrays.asList(SourceCodeEntities).contains(tokensWithoutTags[tagIndex-counter])){
		// ////
		// //// nounsList.add(tokensWithoutTags[tagIndex-counter]);
		// //// // }
		// ////
		// //// }
		// //// }
		// //// if(tagIndex+counter >= 0 && tagIndex+counter<tagList.size()){
		// //// if
		// (tagList.get(tagIndex+counter).equals("NNS")||tagList.get(tagIndex+counter).equals("NN")||tagList.get(tagIndex+counter).equals("NNP")||tagList.get(tagIndex+counter).equals("NNPS")){
		// //// System.out.println("Noun is: "+tokensWithoutTags[tagIndex+counter]);
		// //// // if (Arrays.asList(SourceCodeEntities).contains(tokensWithoutTags[tagIndex+counter])){
		// //// nounsList.add(tokensWithoutTags[tagIndex+counter]);
		// //// // }
		// ////
		// //// }
		// //// }
		// //// }
		// ////
		// //// //STEP6 put verb as a key in HashMap and string[] nouns as value
		// //// System.out.println("STEP6 Saving results...");
		// //// //verbNounRelation.put(tokensWithoutTags[tagIndex], nounsList);
		// //// //nounsList.addAll(nounsTemp);
		// //// System.out.println(nounsList.toString());
		// //// resultsJSON=new JSONObject();
		// //// resultsJSON.put("nouns", nounsList.toString());
		// //// resultsJSON.put("sentence", mySentence[i]);
		// ////
		// //// System.out.println(resultsJSON.toString());
		// //// verbNounRelation.put("VB"+i+"_"+tokensWithoutTags[tagIndex], resultsJSON);
		// //// verbNounForGraph.put("VB"+i+"_"+tokensWithoutTags[tagIndex], nounsList.toString());
		// ////
		// ////
		// //// }
		// //// }
		// ////
		// //// tagIndex++;
		// ////
		// //// }
		// //
	}

	public List<List<String>> getCommentNGramms(){
		return NGramms;
	}

}
