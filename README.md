# Information-Retrieval-Data-Compression
Program to compress the Cranfield database collection using Block Compression and Front Coding algorithm

Part of **CS 6322 Information Retrieval** coursework.

###IR2.java
Includes code to compress the tokens in Cranfield documents using Block Compression and Front Coding algorithm. The
location of the documents is to be passed to the program. The documents are converted to lemmas and stems and indexed
accordingly.

###Lemmatizer.java
Includes code to process the Cranfield documents into lemmas. A file is passed to Lemmatizer and using Stanford CoreNLP
methods, lemmas are returned to the calling program, which in this case is IR2.java.

###Stemmer.java
Implementation of Porters Stemming algorithm to obtain the root of a given word.

###compile
Includes statements to compile the main program.

###run
Includes statements to run the main program. The location of Cranfield documents is passed to the program.

###Program Description:
###Problem 1: 
* Initially, the program accepts the directory path of the Cranfield Collection as a command line argument.

* The program at this point, stores the current time

* We now call the `scanFiles` method, which accepts the Cranfield Collection location as as argument. `scanFiles` method 
traverses through all the files in the target location. Here we count the number of files in the collection and store it
into the *`numberOfFiles`* variable. This method also performs indexing operation.

* `scanFiles` method calls the `getStopwords` method. This method takes the stopwords file as an argument. The stopwords
file has been provided beforehand. The stopwords are read from the file and stored into a **TreeMap**.

* At this point, the program begins creating the first version of the index, and the current time in milliseconds is stored
in a **long** variable, *`startTimeVersion1`*.

* The program iterates through all the files, and stores the document ID in a variable, *`docID`*. The `lemmatizeHash`
method from Lemmatizer program is called, which returns a **HashMap** of lemmas and frequency of term in the file,
stored as *`lemmaHashMap`*. `lemmatizeHash` takes a single **File** of the Cranfield Collection as an output (this is
the reason behind iterating through all the files). `removeStopwords` method is called, which returns a **HashMap** of
lemmas with stopwords eliminated from them. This HashMap is stored as *`lemmaNormalizedHashMap`*.

* *`docID`*, *`lemmaNormalizedHashMap`* and *`indexVersion1`* is passed to the method `createIndex` as arguments.
*`indexVersion1`* is a **TreeMap** having structure of `TreeMap<String, TreeMap<Integer, Integer>>`. This method iterates
through all the *<key, value>* pairs within it, and calls the `insertInIndex` method.

* `insertInIndex` method takes the *`docID`*, *`indexVersion1`* and *<key, value>* pair obtained from `createIndex` method.
The key is the term, and the value is the frequency of term. If the term exists in the index, an entry of term and
<docID, term frequency> is added into the index, else a new entry is created. *`indexVersion1`* is the raw uncompressed
index.

* This uncompressed raw index stores the term, document IDs and term frequencies of each term and document. It is then
written to a file of the format **.uncompressed** in order to obtain the actual size of the uncompressed index.

* The `blockCompression` method is called and the raw uncompressed index is passed to it as an argument. Details of the
algorithm are available at [Block Compression](http://nlp.stanford.edu/IR-book/pdf/05comp.pdf).

* `blockCompression` method creates a compressed index made using `LinkedHashMap` in order to maintain the order of insertion
in the index. The name of the index is *`testIndexBlockVersion1`*.

* Once the index has been created, the current time is obtained. This provides us the total index creation time, which
is stored in the *`timeTakenVersion1`* variable.

* The compressed index is written to a file of the format **.compressed**, which gives us the size of the compressed index.

* The process is repeated by calling the `frontCoding` method to create version 2 of the index by using the Front Coding
Algorithm and the time taken to create the index is measured. Details of the algorithm are available [here](http://en.wikipedia.org/wiki/Incremental_encoding).

* `frontCoding` method creates a compressed index made using `LinkedHashMap` in order to maintain the order of insertion
  in the index. The name of the index is *`testIndexFrontCoding`*.

* The index that is created is made up of stems that were obtained by calling the Stemmer program. The index does not
contain any stopwords. `longestCommonPrefix` method assists `frontCoding` method to obtain the prefix of each term set.

* During index creation of version 2, an **Integer** array, *`documents`* stores the total number of words in the
document, and the frequency of the most frequent term in the document.

* To display the information required, `displayData` method is called.

* Data of 7 sample terms is searched in both the indexes and displayed. `documentFrequency`, `termFreq` and `getPostingSize`
 methods are called to obtain the data. `documentFrequency` provides the number of documents that the term occurs in.
 `termFreq` provides the number of times that the term occurs in each document. `getPostingSize` returns the inverted list
 length.

* Size of uncompressed and compressed index of both the versions is then displayed.

* *`documents`* is written to a file called `documentsInfo`.

* `deltaEncoding` and `gammaEncoding` returns the delta and gamma code of the gaos between document ids.

* Data structures used in the program are `TreeMap`, `HashMap`, `LinkedHashMap`, `List`.

##Output:
![Output](https://github.com/tannamiren/Data-Compression/blob/master/output.PNG)
![Indexes](https://github.com/tannamiren/Data-Compression/blob/master/indexes.PNG)
