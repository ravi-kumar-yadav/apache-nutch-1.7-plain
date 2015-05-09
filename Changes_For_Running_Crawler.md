
###Run Command###
bin/nutch crawl urls -dir crawlDir -solr http://10.208.36.48:8983/solr/mycollection -depth 1 -topN 10


###Need to Check
1. Value of ParentScore is always '1.0f' in `getURLDetails(String url, String crawlDbPath)` method of class `Prioritizer.java`.
2. Current implementation already supports collation of un_fetched URLs till now at any depth.
3. What about One-Class classifier.
4. Some error in stopping the classifier `classifier.classify("Shutdown");`.
5. Changed some code to support Incremental Classifier in an Efficient way and created one method `createIncTrainingFile(buffer.toString(),incTrainingFile);` to always create one separate file for training  containing data regarding newly classified URLs.
6. Where are we changing the score of URLs being stored in CrawlDb, so that this score can be used by Generator to choose topN URLs in next iteration.
7. We did not updated the `posUrlDetailBase` and `negUrlDetailBase` static variables for each depth. So I added following code.
	```java
	
		//** Updating Positive Feature Pool
		URLDetails.posUrlDetailBase.getAnchorTextWords().addAll(details.getAnchorTextWords());
		URLDetails.posUrlDetailBase.getParentURLTokens().addAll(details.getParentURLTokens());
		URLDetails.posUrlDetailBase.getUrlTokens().addAll(details.getUrlTokens());
			
		//** Updating Negative Feature Pool
		URLDetails.negUrlDetailBase.getAnchorTextWords().addAll(urlDetails.getAnchorTextWords());
		URLDetails.negUrlDetailBase.getParentURLTokens().addAll(urlDetails.getParentURLTokens());
		URLDetails.negUrlDetailBase.getUrlTokens().addAll(urlDetails.getUrlTokens());
	```
8. Lot of `no link information. in Prioritizer` message pops-up even when we run crawler with `-depth 2 -topN 10`. This shows that there is no parent(inlinks) information in **crawldb**.


###Tasks###
Date | Task | Status
------------ | -------------
09 May | Understand seed url concept | Pending
09 May | Check Score gets reflected in crawldb | Pending
09 May | Working of Classifier | Pending
09 May | URL Score Computation | Pending
09 May | Positive and Negative Examples getting reflected | Pending
09 May | Applying Stop Word Removal | Pending
09 May | Testing | Pending


###Code Update###
1. Commented code line present in class `BaseSetLoader.java` and in method `updatePosBaseSetUsingDepth(Configuration configuration)`
	```java
		// Old State
		details.setFinalScore(-1.0f);
		
		// New State
		details.setFinalScore(1.0f);
	```
2. Commented following lines of code present in class `BaseSetLoader.java` and in method `updatePosBaseSetUsingDepth(Configuration configuration)`

   ```java
      	ind = line.indexOf("]") + 1;
		line = line.substring(ind);
		line = line.trim();
   ```

### Steps ###
1. Added directory **resorces/** in project root directory
    * Few files like *negativeSet.txt*, *positiveSet.txt* were also copied.
    * We have to create a train file for the initial training of Classifier (train.txt).
    * *appendTrainingData(buffer.toString(),trainingFile);* writes the Postive and Negative examples onto *train.txt* file.
2. Added directory **ClassifierModels** in project root directory
    * No files were added.
    * This directory is used to store the *train.txt*, *model*, *model.idx* files.
    * *train.txt* file is created by step *1(c)*.
    * *model*, *model.idx* files are created while actually training the Classifier i.e. *classifier.learn(trainingFile);*
    * 
