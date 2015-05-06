
###Run Command###
bin/nutch crawl urls -dir crawlDir -solr http://10.208.36.48:8983/solr/mycollection -depth 1 -topN 10

### Steps ###
1. Added directory **resorces/** to project folder
    * Few files like *negativeSet.txt*, *positiveSet.txt* were also copied.
    * We have to create a train file for the initial training of Classifier (train.txt).
    * *appendTrainingData(buffer.toString(),trainingFile);* writes the Postive and Negative examples onto *train.txt* file.
2. Added directory **ClassifierModels**
    * No files were added.
    * This directory is used to store the *train.txt*, *model*, *model.idx* files.
    * *train.txt* file is created by step *1(c)*.
    * *model*, *model.idx* files are created while actually training the Classifier i.e. *classifier.learn(trainingFile);*
