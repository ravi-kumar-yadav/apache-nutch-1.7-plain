package org.apache.nutch.crawl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;


import org.apache.hadoop.io.WritableComparable;


public class URLDetails implements WritableComparable<URLDetails>{

	private String url;						
	//**
	private HashSet<String> parents;								//** Parents ???
	
	//** Anchor Text for current URL and in "posUrlDetailBase.anchorTextWords" as a pool of all AnchorTexts
	private HashSet<String> anchorTextWords;						//** Anchor Text Words Feature values
	
	//** URL Tokens for current URL and in "posUrlDetailBase.urlTokens" as a pool of all URLs
	private HashSet<String> urlTokens;								//** Just URL Tokens ???
	
	//** URL Tokens for parent and in "posUrlDetailBase.parentURLTokens" as a pool of all Parent URL Tokens
	private HashSet<String> parentURLTokens;						//** Parent URL Tokens Feature values
	
	//** Average of parentScores is used for "scores[POS_AVG_PARENT_SCORE_INDEX]" 
	private ArrayList<Float> parentScores;							//** Parent Score	Feature values

	// "out" is not that important and just used for temporary storage
	private String out = null;										//** out ???			
	// INFO SCORES used 	
	// "finalScore" stores value obtained from Classifier acting on its own Feature Vector
	private Float finalScore;										//** finalScore ???
	private float[] scores= new float[10];							//** scores ???
	
	
	public static final int POS_URL_TOKEN_SCORE_INDEX = 1;
	public static final int POS_PARENT_URL_TOKEN_SCORE_INDEX = 2;
	public static final int POS_ANCHOR_WORD_SCORE_INDEX = 3;
	public static final int NEG_URL_TOKEN_SCORE_INDEX = 4;
	public static final int NEG_PARENT_URL_TOKEN_SCORE_INDEX = 5;
	public static final int NEG_ANCHOR_WORD_SCORE_INDEX = 6;
	public static final int POS_AVG_PARENT_SCORE_INDEX = 7;			//** Why Two static variables for same feature "Parent Score" ???
	public static final int NEG_AVG_PARENT_SCORE_INDEX = 7;			//** ???
	
	
	public static URLDetails posUrlDetailBase;
	public static URLDetails negUrlDetailBase;
	// Weights to be learnt by the classifier
	//FIXME to be learnt and modified by classifier
	public static Float PARENT_SCORE_WEIGHT = 0.2f;					//** As there are 6 Features, we should have six weights initialized to 1/6
	public static Float ANCHORTEXT_WEIGHT= 0.2f;					//** These weights can be later changed to whatever we have learnt from train data	???
	public static Float URL_TOKEN_WEIGHT= 0.2f;						//** 
	public static Float PARENT_URL_TOKEN_WEIGHT= 0.2f;
	public static Float NO_OF_PARENTS_WEIGHT= 0.2f;
	

	static {
		// TODO remove this block in final code
		// TODO
		// TODO
		posUrlDetailBase = new URLDetails();
		negUrlDetailBase = new URLDetails();
		/*posUrlDetailBase.addParent("http://en.wikipedia.org/wiki/Air_India");
		posUrlDetailBase
				.addParent("http://en.wikipedia.org/wiki/Category:Tourism_in_India");
		posUrlDetailBase.addParent("http://en.wikipedia.org/wiki/Jet_Airways");
		posUrlDetailBase.addParentScore(1.0f);
		posUrlDetailBase.addParentScore(1.0f);
		posUrlDetailBase.addParentScore(1.0f);
		posUrlDetailBase
				.addParentURLTokens("http://en.wikipedia.org/wiki/Air_India");
		posUrlDetailBase
				.addParentURLTokens("http://en.wikipedia.org/wiki/Category:Tourism_in_India");
		posUrlDetailBase
				.addParentURLTokens("http://en.wikipedia.org/wiki/Jet_Airways");
		posUrlDetailBase.setAnchorTextWords(new HashSet<String>(
				posUrlDetailBase.parentURLTokens));
		posUrlDetailBase.setUrl("http://en.wikipedia.org/wiki/List_of_air_forces");
		posUrlDetailBase
				.setURLTokens("http://en.wikipedia.org/wiki/List_of_air_forces");*/
		System.out.println(posUrlDetailBase);
	}
	
	
	public URLDetails() {
		super();
		parents = new HashSet<String>();
		anchorTextWords = new HashSet<String>();
		urlTokens = new HashSet<String>();
		parentURLTokens = new HashSet<String>();
		parentScores = new ArrayList<Float>();
		
		finalScore = 0.0f;

		scores[POS_URL_TOKEN_SCORE_INDEX] = 0.0f;
		scores[POS_PARENT_URL_TOKEN_SCORE_INDEX] = 0.0f;
		scores[POS_AVG_PARENT_SCORE_INDEX] = 0.0f;
		//scores[POS_AVG_PARENT_SCORE_INDEX] = 0.0f;				//** Commented by Ravi as its same to previous entry 

		scores[NEG_URL_TOKEN_SCORE_INDEX] = 0.0f;
		scores[NEG_PARENT_URL_TOKEN_SCORE_INDEX] = 0.0f;
		scores[NEG_AVG_PARENT_SCORE_INDEX] = 0.0f;					//** Why NEG_AVG_PARENT_SCORE_INDEX has been used when we are using POS_AVG_PARENT_SCORE_INDEX ???
		//scores[NEG_AVG_PARENT_SCORE_INDEX] = 0.0f;				//** Commented by Ravi as its same to previous entry
		
		
		/*
		public static final int POS_ANCHOR_WORD_SCORE_INDEX = 3;	//** "scores" ArrayList miss entries for these two features  
		public static final int NEG_ANCHOR_WORD_SCORE_INDEX = 6;	//**
		*/
	}
	
	
	public URLDetails(String url) {
		this();
		this.url = url;
	}
	
	
	public URLDetails(HashSet<String> parents, HashSet<String> anchorTextWords,
			String url) {
		//super();																	//** Commented by Me.
		this();																		//** Added after commented above line. ???s
		this.parents = parents;
		this.anchorTextWords = anchorTextWords;
		this.url = url;
	}
	
	
	public ArrayList<Float> getParentScores() {
		return parentScores;
	}

	
	public void setParentScores(ArrayList<Float> parentScores) {
		this.parentScores = parentScores;
	}

	
	public HashSet<String> getParents() {
		return parents;
	}

	
	public void setParents(HashSet<String> parents) {
		this.parents = parents;
	}

	
	public HashSet<String> getAnchorTextWords() {
		return anchorTextWords;
	}

	
	public void setAnchorTextWords(HashSet<String> anchorTextWords) {
		this.anchorTextWords = anchorTextWords;
	}

	
	public HashSet<String> getUrlTokens() {
		return urlTokens;
	}

	
	public void setUrlTokens(HashSet<String> urlTokens) {
		this.urlTokens = urlTokens;
	}

	
	public HashSet<String> getParentURLTokens() {
		return parentURLTokens;
	}

	
	public void setParentURLTokens(HashSet<String> parentURLTokens) {
		this.parentURLTokens = parentURLTokens;
	}

	
	public String getOut() {
		return out;
	}

	
	public void setOut(String out) {
		this.out = out;
	}

	
	public Float getFinalScore() {
		return finalScore;
	}

	
	public void setFinalScore(Float finalScore) {
		this.finalScore = finalScore;
	}

	
	public Float getNegAnchorTextWordsScore() {
		return scores[NEG_ANCHOR_WORD_SCORE_INDEX];
	}

	
	public void setNegAnchorTextWordsScore(Float anchorTextWordsScore) {
		this.scores[NEG_ANCHOR_WORD_SCORE_INDEX] = anchorTextWordsScore;
	}

	
	public Float getNegAverageParentScore() {
		return scores[NEG_AVG_PARENT_SCORE_INDEX];
	}

	
	public void setNegAverageParentScore(Float averageParentScore) {
		this.scores[NEG_AVG_PARENT_SCORE_INDEX] = averageParentScore;
	}

	
	public Float getNegUrlTokensScore() {
		return scores[NEG_URL_TOKEN_SCORE_INDEX];
	}

	
	public void setNegUrlTokensScore(Float urlTokensScore) {
		this.scores[NEG_URL_TOKEN_SCORE_INDEX] = urlTokensScore;
	}

	
	public Float getNegParentUrlTokensScore() {
		return scores[NEG_PARENT_URL_TOKEN_SCORE_INDEX];
	}

	
	public void setNegParentUrlTokensScore(Float parentUrlTokensScore) {
		this.scores[NEG_PARENT_URL_TOKEN_SCORE_INDEX] = parentUrlTokensScore;
	}
	
	
	public Float getPosAnchorTextWordsScore() {
		return scores[POS_ANCHOR_WORD_SCORE_INDEX];
	}

	
	public void setPosAnchorTextWordsScore(Float anchorTextWordsScore) {
		this.scores[POS_ANCHOR_WORD_SCORE_INDEX] = anchorTextWordsScore;
	}

	
	public Float getPosAverageParentScore() {
		return scores[POS_AVG_PARENT_SCORE_INDEX];
	}

	
	public void setPosAverageParentScore(Float averageParentScore) {
		this.scores[POS_AVG_PARENT_SCORE_INDEX] = averageParentScore;
	}

	
	public Float getPosUrlTokensScore() {
		return scores[POS_URL_TOKEN_SCORE_INDEX];
	}

	
	public void setPosUrlTokensScore(Float urlTokensScore) {
		this.scores[POS_URL_TOKEN_SCORE_INDEX] = urlTokensScore;
	}

	
	public Float getPosParentUrlTokensScore() {
		return scores[POS_PARENT_URL_TOKEN_SCORE_INDEX];
	}

	
	public void setPosParentUrlTokensScore(Float parentUrlTokensScore) {
		this.scores[POS_PARENT_URL_TOKEN_SCORE_INDEX] = parentUrlTokensScore;
	}
	
	
	public String getUrl() {
		return url;
	}

	
	public void setUrl(String url) {
		this.url = url;
	}

	
	public void addParent(String par) {
		parents.add(par);
	}

	
	public void addParentScore(Float score) {
		parentScores.add(score);
	}

	
	public static void addParentAnchorTextsToStatic(HashSet<String> anchorTexts) {
		posUrlDetailBase.anchorTextWords.addAll(anchorTexts);
	}

	
	public static void addParentURLToStatic(String url) {
		posUrlDetailBase.addParent(url);
		posUrlDetailBase.addParentURLTokens(url);
	}

	public static void addParentScoresToStatic(float score) {
		posUrlDetailBase.addParentScore(score);
	}
	
	public static void addURLDetailsToPosStatic(URLDetails details) {
		posUrlDetailBase.parents.addAll(details.getParents());
		posUrlDetailBase.anchorTextWords.addAll(details.getAnchorTextWords());
		posUrlDetailBase.parentURLTokens.addAll(details.getParentURLTokens());
		posUrlDetailBase.urlTokens.addAll(details.getUrlTokens());
//		urlDetailBase.setScores();
	}
	
	public static void addURLDetailsToNegStatic(URLDetails details) {
		negUrlDetailBase.parents.addAll(details.getParents());
		negUrlDetailBase.anchorTextWords.addAll(details.getAnchorTextWords());
		negUrlDetailBase.parentURLTokens.addAll(details.getParentURLTokens());
		negUrlDetailBase.urlTokens.addAll(details.getUrlTokens());
//		urlDetailBase.setScores();
	}

	
	@Override
	public boolean equals(Object obj) {
		URLDetails details = (URLDetails) obj;
		return url.equals(details.url);
	}

	@Override
	public int hashCode() {
		return url.hashCode();
	}

	@Override
	public String toString() {
		
		//** Ravi - Commented following  
		/*out = url + " " + urlTokens + " " + parents.size() + " " + parentScores
				+ " " + parentURLTokens + " " + anchorTextWords + " "
				+ scores[POS_AVG_PARENT_SCORE_INDEX] + " " + scores[POS_ANCHOR_WORD_SCORE_INDEX] + " "
				+ scores[POS_PARENT_URL_TOKEN_SCORE_INDEX] + " " + scores[POS_URL_TOKEN_SCORE_INDEX] + " " + finalScore;	*/
		
		
		//** Why "out" ??? As it is also being used in "void readFields(DataInput arg0)" which is a Overridden Method
		out = url + " " + urlTokens + " " + parents.size() + " " + parentScores
				+ " " + parentURLTokens + " " + anchorTextWords + " "
				+ scores[POS_AVG_PARENT_SCORE_INDEX] + " " + scores[POS_ANCHOR_WORD_SCORE_INDEX] + " "
				+ scores[POS_PARENT_URL_TOKEN_SCORE_INDEX] + " " + scores[POS_URL_TOKEN_SCORE_INDEX] + " " + finalScore;
		
		return out;
	}
	
	// Split the "URL" (by no alphanumeric elements) and save it into urlTokens (private HashSet<String>) 
	public void setURLTokens(String url) {
		url = url.toLowerCase();
		String arr[] = url.split("[^A-Za-z0-9]+");
		for (String token : arr) {
			token = token.trim();
			if (token.equals("")) {
				continue;
			}
			if (token.equalsIgnoreCase("http") || token.equalsIgnoreCase("www")||token.equalsIgnoreCase("en")||token.equalsIgnoreCase("wikipedia")||token.equalsIgnoreCase("org")||token.equalsIgnoreCase("portal")||token.equalsIgnoreCase("category")||token.equalsIgnoreCase("categories")||token.equalsIgnoreCase("wiki")) {
				continue;
			}
			urlTokens.add(token);
		}

	}
	
	public static HashSet<String> getTokensForUrl(String url) {
		HashSet<String> set = new HashSet<String>();
		url = url.toLowerCase();
		String arr[] = url.split("[^A-Za-z0-9]+");
		for (String token : arr) {
			token = token.trim();
			if (token.equals("")) {
				continue;
			}
			if (token.equalsIgnoreCase("http") || token.equalsIgnoreCase("www")||token.equalsIgnoreCase("en")||token.equalsIgnoreCase("wikipedia")||token.equalsIgnoreCase("org")||token.equalsIgnoreCase("portal")||token.equalsIgnoreCase("category")||token.equalsIgnoreCase("categories")||token.equalsIgnoreCase("wiki")) {
				continue;
			}
			set.add(token);
		}
		
		return set;

	}

	public void addParentURLTokens(String url) {
		url = url.toLowerCase();
		String arr[] = url.split("[^A-Za-z0-9]+");
		for (String token : arr) {
			token = token.trim();
			if (token.equals("")) {
				continue;
			}
			if (token.equalsIgnoreCase("http") || token.equalsIgnoreCase("www")||token.equalsIgnoreCase("en")||token.equalsIgnoreCase("wikipedia")||token.equalsIgnoreCase("org")||token.equalsIgnoreCase("portal")||token.equalsIgnoreCase("category")||token.equalsIgnoreCase("categories")||token.equalsIgnoreCase("wiki")) {
				continue;
			}
			parentURLTokens.add(token);
		}

	}

	
	//** Computes the values for "score" for all features for given URL
	public void setScores() {
		this.scores[POS_ANCHOR_WORD_SCORE_INDEX] = getScore(this.anchorTextWords, posUrlDetailBase.anchorTextWords);
		this.scores[NEG_ANCHOR_WORD_SCORE_INDEX] = getScore(this.anchorTextWords, negUrlDetailBase.anchorTextWords);
		
		this.scores[POS_PARENT_URL_TOKEN_SCORE_INDEX] = getScore(parentURLTokens, posUrlDetailBase.parentURLTokens);
		this.scores[NEG_PARENT_URL_TOKEN_SCORE_INDEX] = getScore(parentURLTokens, negUrlDetailBase.parentURLTokens);
		
		this.scores[POS_URL_TOKEN_SCORE_INDEX] = getScore(urlTokens, posUrlDetailBase.urlTokens);
		this.scores[NEG_URL_TOKEN_SCORE_INDEX] = getScore(urlTokens, negUrlDetailBase.urlTokens);
		
		//FIXME check how to differenttaite between +ve n -ve parent scores
		//** What about below values, which one to keep ???
		// But "Float getScore(ArrayList<Float>)" just returns the average(List)
		this.scores[POS_AVG_PARENT_SCORE_INDEX] = getScore(parentScores);
		this.scores[NEG_AVG_PARENT_SCORE_INDEX] = getScore(parentScores);
	}

	/**
	 * @param set
	 * @param base
	 * @return ratio of words in set present in base to total words in set
	 */
	public Float getScore(HashSet<String> set, HashSet<String> base) {
		Float f = 0.0f;
		if (set == null || base == null || set.size()==0) {
			return 0.0f;
		}
		int count = 0;
		for (String string : set) {
			if (base.contains(string)) {
				count++;
			}
		}

		f = (count * 1.0f) / set.size();

		return f;
	}

	/**
	 * @param set
	 * @return average of collection
	 */
	public Float getScore(ArrayList<Float> set) {
		Float f = 0.0f;
		if (set == null|| set.size()==0) {
			return 0.0f;
		}
		for (Float fl : set) {
			f += fl;
		}
		f = f / set.size();
		return f;
	}


	// Computes value for "finalScore" by firing Classifier using its own Feature Vector  
	public float calculateFinalScore(Classifier classifier) {
		/*
		 * //output = new FileWriter(outfile);
			//BufferedWriter writer = new BufferedWriter(output);
			//writer.write(getFeatureVector());
			//output.close();
		 * 	
		 */
		finalScore= classifier.classify(getFeatureVector());
		//finalScore = PARENT_SCORE_WEIGHT*scores[POS_AVG_PARENT_SCORE_INDEX]+ANCHORTEXT_WEIGHT*scores[POS_AVG_PARENT_SCORE_INDEX]+PARENT_URL_TOKEN_WEIGHT*scores[POS_PARENT_URL_TOKEN_SCORE_INDEX]+URL_TOKEN_WEIGHT*scores[POS_URL_TOKEN_SCORE_INDEX]+NO_OF_PARENTS_WEIGHT*parents.size();
		
		return finalScore;
	}
	
	public String getFeatureVector() {
		setScores();
		String delim = " ";
		StringBuffer buffer = new StringBuffer();
		buffer.append(POS_URL_TOKEN_SCORE_INDEX+":"+scores[POS_URL_TOKEN_SCORE_INDEX]);
		buffer.append(delim);
		buffer.append(POS_PARENT_URL_TOKEN_SCORE_INDEX+":"+scores[POS_PARENT_URL_TOKEN_SCORE_INDEX]);
		buffer.append(delim);
		buffer.append(POS_ANCHOR_WORD_SCORE_INDEX+":"+scores[POS_ANCHOR_WORD_SCORE_INDEX]);
		buffer.append(delim);
//		buffer.append(NO_POS_PARENT_INDEX+":"+scores[NO_POS_PARENT_INDEX]);
//		buffer.append(delim);
		buffer.append(NEG_URL_TOKEN_SCORE_INDEX+":"+scores[NEG_URL_TOKEN_SCORE_INDEX]);
		buffer.append(delim);
//		buffer.append(NEG_AVG_PARENT_SCORE_INDEX+":"+scores[NEG_AVG_PARENT_SCORE_INDEX]);
//		buffer.append(delim);
		buffer.append(NEG_PARENT_URL_TOKEN_SCORE_INDEX+":"+scores[NEG_PARENT_URL_TOKEN_SCORE_INDEX]);
		buffer.append(delim);
		buffer.append(NEG_ANCHOR_WORD_SCORE_INDEX+":"+scores[NEG_ANCHOR_WORD_SCORE_INDEX]);
		buffer.append(delim);
		
		buffer.append(POS_AVG_PARENT_SCORE_INDEX+":"+scores[POS_AVG_PARENT_SCORE_INDEX]);
//		buffer.append(delim);
//		buffer.append(NO_NEG_PARENT_INDEX+":"+scores[NO_NEG_PARENT_INDEX]);
		
		
		return buffer.toString();
	}

	
	
	
	
	/**
	 *  Following are the Overriden methods for WritableComparable Interface 
	 */
	@Override
	public void readFields(DataInput arg0) throws IOException {
		out = arg0.readLine();
		System.out.println("I am in read:" + out);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("I am in Write ==============" + this.toString());
		out.writeUTF(this.toString());
		out.writeUTF("\n");
	}

	
	//** What is logic behind "int compareTo(URLDetails obj)" ???
	@Override
	public int compareTo(URLDetails obj) {
		if(finalScore == null) {
			if(obj.finalScore!=null) {
				return -1;
			}else {
				return 0;
			}
		}
		return (-1*finalScore.compareTo(obj.finalScore));
	}
	
}
