/**
 * 
 */
package org.apache.nutch.crawl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.util.NutchConfiguration;

/**
 * @author Ravi
 * 
 */
public class BaseSetLoader {

	/**
	 * @param args
	 */
	public static void updatePosBaseSetUsingResource(Configuration configuration)
			throws Exception {
		if (configuration == null) {
			configuration = NutchConfiguration.create();
		}
		BufferedReader reader = new BufferedReader(new FileReader(
				configuration.get("positive_set", "resources/positiveSet.txt")));
		String line = null;

		while ((line = reader.readLine()) != null) {
			line = line.trim();
			URLDetails details = new URLDetails(line);
			details.setURLTokens(line);
			details.getParentURLTokens().addAll(details.getUrlTokens());
			details.getAnchorTextWords().addAll(details.getUrlTokens());
			details.addParentScore(1.0f);
			URLDetails.addURLDetailsToPosStatic(details);
			// System.out.println(details);
		}

		reader.close();
	}

	public static void updateNegBaseSetUsingResource(Configuration configuration)
			throws Exception {
		if (configuration == null) {
			configuration = NutchConfiguration.create();
		}
		BufferedReader reader = new BufferedReader(new FileReader(
				configuration.get("negative_set", "resources/negativeSet.txt")));
		String line = null;

		while ((line = reader.readLine()) != null) {
			line = line.trim();
			URLDetails details = new URLDetails(line);
			details.setURLTokens(line);
			details.getParentURLTokens().addAll(details.getUrlTokens());
			details.getAnchorTextWords().addAll(details.getUrlTokens());
			details.addParentScore(1.0f);
			URLDetails.addURLDetailsToNegStatic(details);
			System.out.println(details);
		}

		reader.close();
	}

	
	/**	 * 
	 * @param configuration
	 * @return Set<URLDetails>
	 * @throws Exception
	 * 
	 * @working It reads "resources/positiveSet.txt" file and read each line. Hence populate one object of URLDetails from that line.
	 * 			These "URLDetails" objects are then merged to incremental class variable "posUrlDetailBase"
	 * 			These "URLDetails" objects are also added to Set<URLDetails> to be returned
	 * 			"finalScore" is hardcoded to '-1f' ??? So I changed it to '1f'
	 */	
	public static Set<URLDetails> updatePosBaseSetUsingDepth(Configuration configuration)
			throws Exception {
		Set<URLDetails> set = new HashSet<URLDetails>();
		if (configuration == null) {
			configuration = NutchConfiguration.create();
		}
		BufferedReader reader = new BufferedReader(new FileReader(
				configuration.get("positive_set", "resources/positiveSet.txt")));
		String line = null;

		//**  Sample Line form "resources/positiveSet.txt"
		//	http://en.wikipedia.org/wiki/Kongur_wetland [wetland, kongur] 1 [1.037037] 		[of, wetlands, india] 	[wetland, kongur] 	1.037037 1.037037 1.0 1.0 1.0
		//				url								[urlTokens]			[Parent Scores]	[Parent URL Tokens]		[Anchor TextWords]
		while ((line = reader.readLine()) != null) {
			line = line.toLowerCase();
			line = line.trim();
			int ind = line.indexOf(" ");
			String url = line.substring(0, ind);				//** Extract the URL from line
			
			// This sets the "url" variable in "details" object
			URLDetails details = new URLDetails(url);			//** Create URLDetails object from URL
			line = line.substring(ind + 1);
			line = line.trim();
			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			String s = line.substring(0, ind);					//** Extract string between First Pair of [] brackets, which is list of tokens
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			String arr[] = s.split(",");
			//***********
			//System.out.println("\n\n\nURL Tokens : "+s);
			//***********	
			
			//** Adds "url" to "urlTokens" instance variable (after splitting, lower-casing and removing any stopword)
			details.setURLTokens(url);							//** Split the "URL" (by no alphanumeric elements) and save it into urlTokens (private HashSet<String>) 
			
			line = line.substring(ind);
			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			s = line.substring(0, ind);							//** Extract string between Second Pair of [] brackets, which is Float Value
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			arr = s.split(",");
			//System.out.println("\n\n\nParent Score : "+s);
			
			//** Adds all comma separated "float" values to instance variable ArrayList<Float> parentScores 
			details.getParentScores().addAll(arrayAsList(arr));		//	private ArrayList<Float> parentScores	
			
			line = line.substring(ind);
			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			s = line.substring(0, ind);							//** Extract string between Third Pair of [] brackets, which is list of tokens
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			arr = s.split(",");
			//System.out.println("\n\n\nParent URL Tokens : "+s);
			
			//** Adds all comma separated "string" values to instance variable HashSet<String> parentURLTokens;
			details.getParentURLTokens().addAll(arrayAsSet(arr));	// private HashSet<String> parentURLTokens;
			line = line.substring(ind);
			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			s = line.substring(0, ind);							//** Extract string between Fourth Pair of [] brackets, which is list of tokens
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			arr = s.split(",");
			//System.out.println("\n\n\nAnchor Tokens Text : "+s);
			
			//** Adds all comma separated "string" values to instance variable HashSet<String> anchorTextWords;
			details.getAnchorTextWords().addAll(arrayAsSet(arr));	// private HashSet<String> anchorTextWords;
			
			
			// Adds parents ??? , anchorTextWords, parentURLTokens, urlTokens from "details" to respective instance variable list of "posUrlDetailBase"
			URLDetails.addURLDetailsToPosStatic(details);
			//details.setFinalScore(-1.0f);				//??? Why '-1f' for positive example
			details.setFinalScore(1.0f);
			
			set.add(details);
//			System.out.println(details);
		}

		reader.close();
		return set;
	}

	public static StringBuffer updateNegBaseSetUsingDepth(Configuration configuration)
			throws Exception {
		StringBuffer buffer = new StringBuffer();
		if (configuration == null) {
			configuration = NutchConfiguration.create();
		}
		BufferedReader reader = new BufferedReader(new FileReader(
				configuration.get("negative_set", "resources/negativeSet.txt")));
		String line = null;

		while ((line = reader.readLine()) != null) {
			line = line.toLowerCase();
			line = line.trim();
			int ind = line.indexOf(" ");
			String url = line.substring(0, ind);
			URLDetails details = new URLDetails(url);
			line = line.substring(ind + 1);
			line = line.trim();
			//** Following three lines were commented to read the train.txt file correctly
//			ind = line.indexOf("]") + 1;
//			line = line.substring(ind);
//			line = line.trim();
			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			String s = line.substring(0, ind);
			//***********
			//System.out.println("\n\n\nURL Tokens : "+s);
			//***********	
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			String arr[] = s.split(",");
			details.setURLTokens(url);
			
						
			line = line.substring(ind);
			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			s = line.substring(0, ind);
			//***********
			//System.out.println("\n\n\nParent Score : "+s);
			//***********	
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			arr = s.split(",");
			details.getParentScores().addAll(arrayAsList(arr));
			
			
			
			
			line = line.substring(ind);
			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			s = line.substring(0, ind);
			//***********
			//System.out.println("\n\n\nParent URL Tokens : "+s);
			//***********	
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			arr = s.split(",");
			details.getParentURLTokens().addAll(arrayAsSet(arr));
			
			
			
			line = line.substring(ind);
			ind = line.indexOf("[");
			line = line.substring(ind);
			ind = line.indexOf("]") + 1;
			// System.out.println("+++"+line);
			s = line.substring(0, ind);
			//***********
			//System.out.println("\n\n\nAnchor Text Words : "+s);
			//***********	
			s = s.trim();
			s = s.replaceAll("[\\[\\]]", "");
			s = s.replaceAll(" ", "");
			arr = s.split(",");
			details.getAnchorTextWords().addAll(arrayAsSet(arr));
			
			URLDetails.addURLDetailsToNegStatic(details);
			details.setFinalScore(-1.0f);
			buffer.append("-1 "+details.getFeatureVector());
			buffer.append("\n");
		}

		reader.close();
		return buffer;
	}

	public static ArrayList<Float> arrayAsList(String[] arr) {
		ArrayList<Float> list = new ArrayList<Float>();
		for (String string : arr) {
			string = string.trim();
			try {
				Float f = Float.parseFloat(string);
				list.add(f);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	public static HashSet<String> arrayAsSet(String[] arr) {
		HashSet<String> list = new HashSet<String>();
		for (String string : arr) {
			if("wiki".equalsIgnoreCase(string)) {
				continue;
			}
			string = string.trim();
			list.add(string);
		}

		return list;
	}
}
