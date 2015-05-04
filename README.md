# apache-nutch-1.7-plain
Code is downloaded from apache site and being modified for MTP2

My MTP2 Project

## Suggestions By Swapnil

1. mere time pe 2 class svm use kiya tha with linear kernel, try with gaussian kernel, linear kaam nahi karega as data increases (ganesh aur sunita ne two class svm use karne bola tha)
2. make sure koi bhi tourism page ko positive me daalo
3. humne negative set sirf health ka liya tha... which not enough... saare orthogonal categories dhundo aur sabke thode thode urls base set me daalo. humne india ke bahar waalon ko -ve mark kiya tha, which causes error... meri report me error analysis [padh lena...exact details mil jaayenge
4. one class better fit hoga yahaa shayad, try to get answer to this question as well, isse jaldi implement karna...10 depths tak results aane ko it takes around 10-12 hrs


## Rough Idea
1. Try Guassian Kernel
2. Build Larger Negative Training set
3. Koi bhi tourism page ko positive me daalo

## Features
1. Percentage of overlapping URL tokens in the already crawled URLs set.
2. Percentage of overlapping parents URL tokens in the already crawled URLS’ parent tokens set.
3. Percentage of overlapping anchor texts of the URL in the already crawled URLs’ anchor text set.
4. Percentage of overlapping URL tokens in the already discarded URL tokens set.
5. Percentage of overlapping parents URL tokens in the already discarded URLS’ parent tokens set.
6. Percentage of overlapping anchor texts of the URL in the already discarded URLs’ anchor text set.
7. Average parent scores of the URL.


