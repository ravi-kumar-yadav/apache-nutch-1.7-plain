/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nutch.crawl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.fetcher.Fetcher;
import org.apache.nutch.indexer.IndexingJob;
import org.apache.nutch.indexer.solr.SolrDeleteDuplicates;
import org.apache.nutch.parse.ParseSegment;
import org.apache.nutch.tools.CrawlDBScanner;
import org.apache.nutch.util.HadoopFSUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.SimpleDateFormat;
// Commons Logging imports

public class Crawl extends Configured implements Tool {
  
	public static ArrayList<URLDetails> urlSet = new ArrayList<URLDetails>();
	public static final Logger LOG = LoggerFactory.getLogger(Crawl.class);
	public static long TOPN = 1000;

  private static String getDate() {
    return new SimpleDateFormat("yyyyMMddHHmmss").format
      (new Date(System.currentTimeMillis()));
  }


  /* Perform complete crawling and indexing (to Solr) given a set of root urls and the -solr
     parameter respectively. More information and Usage parameters can be found below. */
  public static void main(String args[]) throws Exception {
    Configuration conf = NutchConfiguration.create();
    int res = ToolRunner.run(conf, new Crawl(), args);
    System.exit(res);
  }
  
  /*
   * Usage : bin/nutch crawl urls -dir crawlDir -solr http://10.208.36.48:8983/solr/mycollection -depth 1 -topN 10
   */
  @Override
  public int run(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Usage: Crawl <urlDir> -solr <solrURL> [-dir d] [-threads n] [-depth i] [-topN N]");
      return -1;
    }
    Path rootUrlDir = null;
    Path dir = new Path("crawl-" + getDate());
    int threads = getConf().getInt("fetcher.threads.fetch", 10);
    int depth = 5;
    long topN = Long.MAX_VALUE;
    String solrUrl = null;
    
    for (int i = 0; i < args.length; i++) {
      if ("-dir".equals(args[i])) {
        dir = new Path(args[i+1]);										//** Default is crawl-<getDate()> (dir)
        i++;
      } else if ("-threads".equals(args[i])) {
        threads = Integer.parseInt(args[i+1]);							//** Default is 10
        i++;
      } else if ("-depth".equals(args[i])) {
        depth = Integer.parseInt(args[i+1]);							//** Default is 5
        i++;
      } else if ("-topN".equals(args[i])) {
          topN = Integer.parseInt(args[i+1]);							//** Default is Long.MAX_VALUE
          i++;
      } else if ("-solr".equals(args[i])) {
        solrUrl = args[i + 1];											//** Default is null (solrUrl)
        i++;
      } else if (args[i] != null) {
        rootUrlDir = new Path(args[i]);									//** Seed_URL Dir ??? , Deafult is null (rootUrlDir)
      }
    }

    JobConf job = new NutchJob(getConf());

    if (solrUrl == null) {
      LOG.warn("solrUrl is not set, indexing will be skipped...");
    }
    else {
        // for simplicity assume that SOLR is used 
        // and pass its URL via conf 
        getConf().set("solr.server.url", solrUrl);
    }

    FileSystem fs = FileSystem.get(job);

    if (LOG.isInfoEnabled()) {													
      LOG.info("crawl started in: " + dir);										// Makes Log entries
      LOG.info("rootUrlDir = " + rootUrlDir);
      LOG.info("threads = " + threads);
      LOG.info("depth = " + depth);      
      LOG.info("solrUrl=" + solrUrl);
      if (topN != Long.MAX_VALUE)
        LOG.info("topN = " + topN);
    }

    
    
    /**
	 * @author Ravi
	 * **/
	//Initialize Base Set
	String trainingFile = getConf().get("training_file");				// "training_file" --> train.txt 
	String incTrainingFile = getConf().get("incTrainingFile");
	StringBuffer buffer = new StringBuffer();
	StringBuffer outBuffer = new StringBuffer();
	
	// Reads a file "resources/positiveSet.txt" 
	// Create These "URLDetails" objects from each line of this file
	// These object values are then merged into incremental class variable "posUrlDetailBase"
	// Set<URLDetails> is returned
	// Also sets "finalScore" in URLDetails object to be "1f"
	Set<URLDetails> set = BaseSetLoader.updatePosBaseSetUsingDepth(getConf());
	
	System.out.println("\n\nYehe Everything got Read.\n\n");
	
	// Works in similar fashion as mentioned above for Negative Examples "resources/negativeSet.txt"
	buffer = BaseSetLoader.updateNegBaseSetUsingDepth(getConf());
	
	
	// Append Positive Examples to the "buffer" already containing Negative Examples 
	for (URLDetails urlDetails : set) {
		buffer.append("1 "+urlDetails.getFeatureVector());
		buffer.append("\n");
	}
	
	
	// Method to write training data (containing Negative examples followed by Positive examples) present in "buffer",
	// in a file "train.txt" stored in config entry present by the name of "trainingFile"
	appendTrainingData(buffer.toString(),trainingFile);

	
	Classifier classifier = new Classifier(getConf());
	
	//** Train Classifier from train.txt(trainingFile)
	classifier.learn(trainingFile);
	//Initialize classifier
	/***
	 * changes end
	 * ***/
    
	
    Path crawlDb = new Path(dir + "/crawldb");									//** Creates internal folder for crawl in <dir> directory that we provide		
    Path linkDb = new Path(dir + "/linkdb");
    Path segments = new Path(dir + "/segments");
    Path indexes = new Path(dir + "/indexes");									// *Not created for swapnil's code
    Path index = new Path(dir + "/index");										// *Not created for swapnil's code
	Path prioritizeDb = new Path(dir + "/prioritizer");							// ** Added for our Project
	
    
    Path tmpDir = job.getLocalPath("crawl"+Path.SEPARATOR+getDate());
    System.out.println("\n\n********Injector Started");
    Injector injector = new Injector(getConf());
    System.out.println("\n\n********Genrator Started");
    Generator generator = new Generator(getConf());
    System.out.println("\n\n********Fetcher Started");
    Fetcher fetcher = new Fetcher(getConf());
    System.out.println("\n\n********Parse Segment Started");
    ParseSegment parseSegment = new ParseSegment(getConf());
    System.out.println("********CrawlDb Started");
    CrawlDb crawlDbTool = new CrawlDb(getConf());
    System.out.println("********LinkDb Started");
    LinkDb linkDbTool = new LinkDb(getConf());
    

    System.out.println("********Prioritizer Started");
    //** Additional Objects created
	Prioritizer prioritizer = new Prioritizer();
	System.out.println("********CrawlDBScanner Started");
	CrawlDBScanner cbds = new CrawlDBScanner(NutchConfiguration.create());
	System.out.println("********CrawlDatum Started");
	CrawlDatum cd = new CrawlDatum();
	    
      
	System.out.println("********Injector Injected");
    // initialize crawlDb
    injector.inject(crawlDb, rootUrlDir);										// *Injecting URLs to CrawlDB, Only Once for any Crawl Process
    																			//** Injector runs only once that too before initial round of run 
    int i;
    for (i = 0; i < depth; i++) {             // generate new segment			//** Each round means --> One Segment, Within each Segment multiple things happen
    	System.out.println("********Generator Generates");
      Path[] segs = generator.generate(crawlDb, segments, -1, topN, System.currentTimeMillis());		//** Generator job for each Segment
          
      if (segs == null) {
        LOG.info("Stopping at depth=" + i + " - no more URLs to fetch.");
        break;
      }
      
      System.out.println("********Fetcher Fetches");
      fetcher.fetch(segs[0], threads);  // fetch it								//** Fetcher Job
      if (!Fetcher.isParsing(job)) {
    	  System.out.println("********Parser Parsed");
        parseSegment.parse(segs[0]);    // parse it, if needed					//** Parsing Job
      }
      
      System.out.println("********CrawlDbTool Updated");
      crawlDbTool.update(crawlDb, segs, true, true); // update crawldb			//** Updating only CrawlDB
    //}
    
    
//    if (i > 0) {
      System.out.println("********LinkDbTool Inverted");
      linkDbTool.invert(linkDb, segments, true, true, false); // invert links	//** Not for each job, It just run once that too at-last

//      if (solrUrl != null) {
//        // index, dedup & merge
//        FileStatus[] fstats = fs.listStatus(segments, HadoopFSUtil.getPassDirectoriesFilter(fs));
//        
//        IndexingJob indexer = new IndexingJob(getConf());						//** If Solr-URL is given then do Indexing job 
//        indexer.index(crawlDb, linkDb, 
//                Arrays.asList(HadoopFSUtil.getPaths(fstats)));
//
//        SolrDeleteDuplicates dedup = new SolrDeleteDuplicates();
//        dedup.setConf(getConf());
//        dedup.dedup(solrUrl);
//      }
//      
//    } else {
//      LOG.warn("No URLs to fetch - check your seed list and URL filters.");
//    }
      
      
      buffer = new StringBuffer();
      outBuffer = new StringBuffer();
      
      // HashSet<String> urlinfo (Return Value) is always Empty (Not NULL)
      // Need to Resolve following Function Parameter ???
      // Why we need "wiki-type" RegEx ???
      System.out.println("HashSet<String> res = cbds.run2");
      // It Keeps all the URLs that passes RegEx and are tagged as db_unfetched in CrawlDb database
      HashSet<String> res = cbds.run2(new String[]{crawlDb.toString(),prioritizeDb.toString(),"http://en\\.wikipedia\\.org/wiki/.*","-s","db_unfetched","-text"});
//		Iterator<Text> iter = res.iterator();
      System.out.println("My Size is " + res.size());
      
      
      System.out.println("prioritizer.init");
      prioritizer.init(getConf(), linkDb);
      
      // Starts the Classifier at a given Port with a Model that is already learnt and stored in a file "/ClassifierModels/model"
      System.out.println("classifier.startClassifier");
      classifier.startClassifier();
      
      for (String text : res) {
    	// Fetches all the parents of given URL form crawlDb and populates the URLDetails object
    	// URLDetails object --> (Parent url tokens, Parent anchor text, Parent Score) for each parent, Url Tokens(one set)
		URLDetails urlDetails = prioritizer.getURLDetails(text, crawlDb.toString());
		if(urlDetails==null) {
			System.out.println("N0 information on link : "+text);
			continue;
		}
		
		// Compute Scores for Seven Features
		urlDetails.setScores();
		/*float score = */
		
		// Valid only for Ist round
		if(i!=0) {
			//FIXME call classifier here
			//get score and set it in url details
			//FIXME ask rahul about how to create a C++ server for SVM
			//classifier.classify(getConf().get("test_file"));
			
			// It Computes the score for given urlDetail after it fires urlDetail's Feature Vector
			// This score is then stored in "finalScore" instance variable
			urlDetails.calculateFinalScore(classifier);
		}else {
			// As we assume that all URL obtained from crawling seed-urls directly are Positive
			urlDetails.setFinalScore(1.0f);
		}
		
		
		if(urlSet.contains(urlDetails)) {
			urlSet.remove(urlDetails);
		}
		urlSet.add(urlDetails);
		
		if(urlDetails.getFinalScore()>=0) {
			
		}else {
			buffer.append("\n");
			buffer.append("-1 "+urlDetails.getFeatureVector());
			outBuffer.append("\n");
			outBuffer.append("-1 "+urlDetails.getFeatureVector()+"\t"+urlDetails.getUrl()+" - "+urlDetails.getAnchorTextWords()+" "+urlDetails.getFinalScore());
			
			//** Updating Negative Feature Pool
			URLDetails.negUrlDetailBase.getAnchorTextWords().addAll(urlDetails.getAnchorTextWords());
			URLDetails.negUrlDetailBase.getParentURLTokens().addAll(urlDetails.getParentURLTokens());
			URLDetails.negUrlDetailBase.getUrlTokens().addAll(urlDetails.getUrlTokens());
			
		}
//		if(urlSet.contains(urlDetails)) {
//			urlSet.remove(urlDetails);
//		}
//		urlSet.add(urlDetails);
		
//		crawlDbReader.readUrlDatum(crawlDb.toString(), text, getConf()).setScore(score);;
		System.out.println("I am lucky ---------- " + urlDetails);
      }
      
      //** Commented as It seems to be a mistake ???
      //classifier.classify("Shutdown");
      classifier.stopClassifier();
      
      Collections.sort(urlSet);
      
      for (int j = 0; j < TOPN && j<urlSet.size(); j++) {
		URLDetails details = urlSet.get(j);
		if (details.getFinalScore() >= 0) {
			buffer.append("\n");
			buffer.append("1 " + details.getFeatureVector());
			outBuffer.append("\n");
			outBuffer.append("1 "+details.getFeatureVector()+"\t"+details.getUrl()+" - "+details.getAnchorTextWords()+" "+details.getFinalScore());
			
			//** Updating Positive Feature Pool
			URLDetails.posUrlDetailBase.getAnchorTextWords().addAll(details.getAnchorTextWords());
			URLDetails.posUrlDetailBase.getParentURLTokens().addAll(details.getParentURLTokens());
			URLDetails.posUrlDetailBase.getUrlTokens().addAll(details.getUrlTokens());
		}else {
			break;
		}
      }
//		System.out.println("--------------------------------");
//		System.out.println(urlSet);
//		System.out.println("--------------------------------");
//		} else {
//			LOG.warn("No URLs to fetch - check your seed list and URL filters.");
//		}
		
		if(i==0) {
			//FIXME Rohit please add your code here				
			//learn on new set
			classifier.learn(trainingFile);
		}else {
			// Commented below lines to support Incremental Classifier in an Efficient way
//			//learn on new set
//			appendTrainingData(buffer.toString(),trainingFile);
//			classifier.incLearn(trainingFile);
			createIncTrainingFile(buffer.toString(),incTrainingFile);
			classifier.incLearn(incTrainingFile);
			
		}
		
		if (LOG.isInfoEnabled()) {
			LOG.info("crawl finished: " + dir);				//** Crawl finished message
		}
		
		// Why is this used???
		// Its not present also
		appendOutputToFile(outBuffer.toString(),getConf().get("outputFolder")+"/"+i);
    }
    return 0;
  }

  
  private void createIncTrainingFile(String incTrainData, String incTrainingFile) {
	// TODO Auto-generated method stub
	  //incTrainingFile
	  
	  try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(incTrainingFile,false));
			writer.write(incTrainData);
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}


//Method to write training data (containing Positive and Negative example) present in "out",
  // in a file "train.txt" stored in config entry present by the name of "trainingFile"
  public void appendTrainingData(String out, String trainingFile){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(trainingFile,true));
			writer.write(out);
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
  
  
  public void appendOutputToFile(String out, String file){
	  try {
		  BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
		  writer.write(out);
		  writer.close();
	  } catch (Exception e) {
		  // TODO Auto-generated catch block
		  e.printStackTrace();
	  }		
  }  

}
