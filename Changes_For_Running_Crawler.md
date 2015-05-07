
###Run Command###
bin/nutch crawl urls -dir crawlDir -solr http://10.208.36.48:8983/solr/mycollection -depth 1 -topN 10


###Need to Check### 
[Search for Need to check this ???]
1. Value of ParentScore is always '1.0f' in `getURLDetails(String url, String crawlDbPath)`
2. 



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
