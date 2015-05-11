package org.apache.nutch.crawl;

/*
 * Added by Ravi
 */

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapFileOutputFormat;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.nutch.crawl.CrawlDbReader.CrawlDatumCsvOutputFormat;
import org.apache.nutch.tools.CrawlDBScanner;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;

public class Prioritizer {
	LinkDbReader linkDbReader;
	CrawlDbReader crawlDbReader;
	Configuration conf;
	
	
	public Configuration getConf() {
		return conf;
	}
	
	
	public void setConf(Configuration conf) {
		this.conf = conf;
	}
	
	
	public void init(Configuration conf, Path directory) {
		if (conf==null) {
			conf=NutchConfiguration.create();
		}
		try {
			linkDbReader = new LinkDbReader(conf, directory);
			crawlDbReader = new CrawlDbReader();
			this.conf = conf;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
		Prioritizer prioritizer = new Prioritizer();
		//prioritizer.init(NutchConfiguration.create(), new Path("/home/swapnil/WWWworkspace/apache-nutch-1.7/crawl4/linkdb"));
		//prioritizer.init(NutchConfiguration.create(), new Path(conf.get("outputFolder"));
		
		
//		prioritizer.getURLDetails("http://blogs.timesofindia.indiatimes.com/","/home/swapnil/WWWworkspace/apache-nutch-1.7/crawl4/crawldb");
//		prioritizer.getCrawlInfo("http://timespolls.itimes.com/polls","/home/swapnil/WWWworkspace/apache-nutch-1.7/crawl4/crawldb");
	}
	
	
	public URLDetails getURLDetails(String url, String crawlDbPath) {
		try {
			URLDetails urlDetails = new URLDetails(url);
			//System.out.println("Shri Ram : " + url);
			Inlinks links = linkDbReader.getInlinks(new Text(url));
			if (links == null) {
		          System.out.println(" - no link information. in Prioritizer");
		        } else {
		          for (Inlink inlink : (links.getInlinks())) {
					
		            String par = inlink.getFromUrl();
		            urlDetails.addParent(par);
		            String arr[] = inlink.getAnchor().trim().toLowerCase().split(" ");
		            
		            CrawlDatum datum = crawlDbReader.readUrlDatum(crawlDbPath,par,getConf());
		            
		            
		            //** Need to check this ???
		            // Is it always constant like 0 or 1, which is not good
		            urlDetails.addParentScore(datum.getScore());
		            urlDetails.addParentURLTokens(par);
		            //** Need to check this also
		            // Why Anchor Text is same as URL always ???
		            urlDetails.getAnchorTextWords().addAll(Arrays.asList(arr));
		          }
		          urlDetails.setURLTokens(urlDetails.getUrl());
		          
		          //System.out.println(urlDetails.toString());
		          return urlDetails;
		        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(url);
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println(url);
			e.printStackTrace();
		}
		return null;
		
	}
	
	
	public void getCrawlInfo(String url, String crawlDbPath) {
		CrawlDatum datum=null;
		try {
			datum = crawlDbReader.readUrlDatum(crawlDbPath, url, getConf());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("----"+datum);
	}
	
	
	public void printAll() {
		CrawlDBScanner cds = new CrawlDBScanner();
		
	}
	
	
	public static void processPrioritizeJob(String crawlDb, String linkDb, String output, Configuration config, String format, String regex, String status) throws IOException {
	    Path outFolder = new Path(output);

	    JobConf job = new NutchJob(config);
	    job.setJobName("dump " + crawlDb);

	    FileInputFormat.addInputPath(job, new Path(crawlDb, CrawlDb.CURRENT_NAME));
	    job.setInputFormat(SequenceFileInputFormat.class);
	    FileOutputFormat.setOutputPath(job, outFolder);

	    if (format.equals("csv")) {
	      job.setOutputFormat(CrawlDatumCsvOutputFormat.class);
	    }
	    else if (format.equals("crawldb")) {
	      job.setOutputFormat(MapFileOutputFormat.class);
	    } else {
	      job.setOutputFormat(TextOutputFormat.class);
	    }

	    if (status != null) job.set("status", status);
	    if (regex != null) job.set("regex", regex);
	    if (linkDb != null) job.set("linkDb", linkDb);
	    if (crawlDb != null) job.set("crawlDb", crawlDb);
	    
	    job.setMapperClass(CrawlDbPrioritizeMapper.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(URLDetails.class);

	    JobClient.runJob(job);
	    
	  }
	

	public static class CrawlDbPrioritizeMapper implements Mapper<Text, CrawlDatum, Text, URLDetails> {
	    Pattern pattern = null;
	    Matcher matcher = null;
	    String status = null;
	    Prioritizer prioritizer = null;
	    String linkDb = null;
	    String crawlDb = null;
	    

	    public void configure(JobConf job) {
	      if (job.get("regex", null) != null) {
	        pattern = Pattern.compile(job.get("regex"));
	      }
	      status = job.get("status", null);
	      
	      linkDb = job.get("linkDb", null);
	      crawlDb = job.get("crawlDb", null);
	      prioritizer = new Prioritizer();
	      prioritizer.init(null, new Path(linkDb));
	     
	    }

	    public void close() {}
	    public void map(Text key, CrawlDatum value, OutputCollector<Text, URLDetails> output, Reporter reporter)
	            throws IOException {

	    	
	      // check status
	      if (status != null
	        && !status.equalsIgnoreCase(CrawlDatum.getStatusName(value.getStatus()))) return;

	      // check regex
	      if (pattern != null) {
	        matcher = pattern.matcher(key.toString());
	        if (!matcher.matches()) {
	          return;
	        }
	      }
	      
	      URLDetails urlDetails = prioritizer.getURLDetails(key.toString(), (new Path(crawlDb)).toString());
	      if(urlDetails == null) return;

	      output.collect(key, urlDetails);
	    }
	  }
	
}
