import org.jsoup.Jsoup;

import java.io.*;
import java.util.*;

/**
 * Created by miren_t on 3/21/2015.
 */
public class IR2 {

    static TreeMap<String, TreeMap<Integer, Integer>> indexVersion1= new TreeMap<String, TreeMap<Integer, Integer>>();
    static TreeMap<String, LinkedHashMap<Short, Short>> compressedIndexVersion1= new TreeMap<String, LinkedHashMap<Short, Short>>();
    static TreeMap<String, TreeMap<Integer, Integer>> indexVersion2= new TreeMap<String, TreeMap<Integer, Integer>>();
    static TreeMap<String, LinkedHashMap<Short, Short>> compressedIndexVersion2= new TreeMap<String, LinkedHashMap<Short, Short>>();
    static TreeMap<String, Integer> termFreq= new TreeMap<String, Integer>();
    static LinkedHashMap<String, List<Object>> testIndexBlockVersion1= new LinkedHashMap<String, List<Object>>();
    static LinkedHashMap<String, List<Object>> testIndexFrontCoding= new LinkedHashMap<String, List<Object>>();
    static File uncompressedIndexFile, compressedIndexFile;
    static double timeTakenVersion1=0;
    static double timeTakenVersion2=0;
    static int numberOfFiles=0;
    public static void main(String[] args) {

        String CRANFIELD_DIRECTORY=args[0];

        try {
            scanFiles(CRANFIELD_DIRECTORY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    static void scanFiles(String directoryPath) throws IOException {
        File file = new File(directoryPath);
        boolean check=true;

        File listOfFiles[] = file.listFiles();
        Arrays.sort(listOfFiles);
        numberOfFiles=listOfFiles.length;
        int documents[][] = new int[numberOfFiles+1][2];
        TreeMap<String, String> stopwords= getStopwords("stopwords");

        System.out.println("Version 1");
        System.out.println("Creating index");
        long startTimeVersion1= System.currentTimeMillis();
        for(File files:listOfFiles){
            int docID=Integer.parseInt(files.getName().substring(9));

 //           HashMap<String, Integer> tokensInFile= getTokens(files);
 //           HashMap<String, Integer> stemmedTokens= getStemmedTokens(tokensInFile);
            HashMap<String, Integer> lemmaHashMap= new Lemmatizer().lemmatizeHash(files);
            HashMap<String, Integer> lemmaNormalizedHashMap= removeStopwords(lemmaHashMap, stopwords);
         //working   System.out.println(docId + " " + lemmaNormalizedHashMap.size());

            createIndex(docID, lemmaNormalizedHashMap, indexVersion1);
        }

        System.out.println("Writing uncompressed index..");
        writeUncompressedIndex("./", "Index_Version1.uncompressed", indexVersion1);

        System.out.println("Done..\nCompressing index..");
        blockCompression(indexVersion1);
        timeTakenVersion1= (System.currentTimeMillis() - startTimeVersion1)/1000;

        System.out.println("Done..\nWriting compressed index..");
        writeCompressedIndex("./", "Index_Version1.compressed", testIndexBlockVersion1);

        System.out.println("Done..");


        System.out.println("*******************************************");
        System.out.println("Version 2");
        System.out.println("Creating index");
        long startTimeVersion2= System.currentTimeMillis();
        for(File files:listOfFiles){
            int docID=Integer.parseInt(files.getName().substring(9));

            HashMap<String, Integer> tokensInFile= getTokens(files);
   //         System.out.println(files.getName());
            HashMap<String, Integer> stemmedTokens= getStemmedTokens(tokensInFile);
            HashMap<String, Integer> stemmedNormalizedHashMap= removeStopwords(stemmedTokens, stopwords);
            //working   System.out.println(docId + " " + lemmaNormalizedHashMap.size());

            updateDoclenInfo(documents, docID, stemmedTokens, stemmedNormalizedHashMap);
            createIndex(docID, stemmedNormalizedHashMap, indexVersion2);
        }
        if(indexVersion2.containsKey(""))
            indexVersion2.remove("");

        System.out.println("Writing uncompressed index..");
        writeUncompressedIndex("./", "Index_Version2.uncompressed", indexVersion2);

        System.out.println("Done..\nCompressing index..");
        frontCoding(indexVersion2);
        timeTakenVersion2= (System.currentTimeMillis() - startTimeVersion2)/1000;

        System.out.println("Done..\nWriting compressed index..");
        writeCompressedIndex("./", "Index_Version2.compressed", testIndexFrontCoding);
        System.out.println("Done..");

        displayData();
        writeDoclenFile("./", "documentsInfo", documents);

    } /*
    TODO: document frequency --> dF, need file of document information?
    */
    /*********************data collection***************************/
    public static void updateDoclenInfo(int documents[][], int docID, HashMap<String, Integer> stemmedTokens, HashMap<String, Integer> stemmedNormalizedHashMap){
        int totalFileStems=0, max_tf=0;
        Set<String> fileStems= stemmedTokens.keySet();
        for(String f:fileStems){
            totalFileStems+=stemmedTokens.get(f);
        }

        Set<String> normalizedFileStems= stemmedNormalizedHashMap.keySet();
        for(String n:normalizedFileStems){
            max_tf=Math.max(max_tf, stemmedNormalizedHashMap.get(n));
        }
        documents[docID][0]=totalFileStems;
        documents[docID][1]=max_tf;
    }
    public static void writeDoclenFile(String directoryPath, String fileName, int documents[][]) throws IOException {
        File doclen= new File(directoryPath + "/" + fileName);
        ObjectOutputStream objectOutputStream= new ObjectOutputStream(new FileOutputStream(doclen));
        objectOutputStream.writeObject(documents);
        objectOutputStream.flush();
        objectOutputStream.close();
    }
    public static void displayData() throws IOException {
        String term1="Reynolds"; String term2="NASA"; String term3="Prandtl"; String term4="flow";
        String term5="pressure"; String term6="boundary"; String term7="shock";

        String versionData="";
        String term1v1=new Lemmatizer().lemmatizeString("Reynolds".toLowerCase());
        versionData+="Reynolds\t" + documentFrequency(term1v1, indexVersion1) +
                "\t\t" + termFreq(term1v1, indexVersion1) + "\t\t" + getPostingSize(term1v1, indexVersion1) + " \n";

        String term2v1= new Lemmatizer().lemmatizeString("NASA".toLowerCase());
        versionData+="NASA\t\t" + documentFrequency(term2v1, indexVersion1) +
                "\t\t" + termFreq(term2v1, indexVersion1) + "\t\t" + getPostingSize(term2v1, indexVersion1) + " \n";

        String term3v1= new Lemmatizer().lemmatizeString("Prandtl".toLowerCase());
        versionData+="Prandtl\t\t" + documentFrequency(term3v1, indexVersion1) +
                "\t\t" + termFreq(term3v1, indexVersion1) + "\t\t" + getPostingSize(term3v1, indexVersion1) + " \n";

        String term4v1= new Lemmatizer().lemmatizeString("flow".toLowerCase());
        versionData+="flow\t\t" + documentFrequency(term4v1, indexVersion1) +
                "\t\t" + termFreq(term4v1, indexVersion1) + "\t\t" + getPostingSize(term4v1, indexVersion1) + " \n";

        String term5v1= new Lemmatizer().lemmatizeString("pressure".toLowerCase());
        versionData+="pressure\t" + documentFrequency(term5v1, indexVersion1) +
                "\t\t" + termFreq(term5v1, indexVersion1) + "\t\t" + getPostingSize(term5v1, indexVersion1) + " \n";

        String term6v1= new Lemmatizer().lemmatizeString("boundary".toLowerCase());
        versionData+="boundary\t" + documentFrequency(term6v1, indexVersion1) +
                "\t\t" + termFreq(term6v1, indexVersion1) + "\t\t" + getPostingSize(term6v1, indexVersion1) + "\n";

        String term7v1= new Lemmatizer().lemmatizeString("shock".toLowerCase());
        versionData+="shock\t\t" + documentFrequency(term7v1, indexVersion1) +
                "\t\t" + termFreq(term7v1, indexVersion1) + "\t\t" + getPostingSize(term7v1, indexVersion1) + "\n";

        System.out.println("*********Index Info***************");
        System.out.println("Time taken by version 1: " + timeTakenVersion1 + " seconds");
        System.out.println("Size of uncompressed index: " + (new File("Index_Version1.uncompressed").length())/1024 + " KiB");
        System.out.println("Size of compressed index: " + (new File("Index_Version1.compressed").length())/1024 + " KiB");
        System.out.println("Size of inverted lists in index 1: " + testIndexBlockVersion1.size());
        System.out.println("Information of some terms in index 1");

        System.out.println("Term\tDocument Frequency\tTerm Frequency\tPosting Size");
        System.out.println(versionData);


        System.out.println("*********Index Info***************");
        System.out.println("Time taken by version 2: " + timeTakenVersion2 + " seconds");
        System.out.println("Size of uncompressed index: " + (new File("Index_Version2.uncompressed").length())/1024 + " KiB");
        System.out.println("Size of compressed index: " + (new File("Index_Version2.compressed").length())/1024 + " KiB");
        System.out.println("Size of inverted lists in index 2: " + testIndexFrontCoding.size());
        System.out.println("Information of some terms in index 2");

        System.out.println("Term\tDocument Frequency\tTerm Frequency\tPosting Size");
        Stemmer stemmer= new Stemmer();

        stemmer.add(term1.toLowerCase().toCharArray(), term1.length());
        stemmer.stem();
        String term1v2=stemmer.toString();
        System.out.println("Reynolds\t" + documentFrequency(term1v2, indexVersion2) +
                "\t\t" + termFreq(term1v2, indexVersion2) + "\t\t" + getPostingSize(term1v2, indexVersion2) + " ");

        stemmer.add(term2.toLowerCase().toCharArray(), term2.length());
        stemmer.stem();
        String term2v2=stemmer.toString();
        System.out.println("NASA\t\t" + documentFrequency(term2v2, indexVersion2) +
                "\t\t" + termFreq(term2v2, indexVersion2) + "\t\t" + getPostingSize(term2v2, indexVersion2) + " ");

        stemmer.add(term3.toLowerCase().toCharArray(), term3.length());
        stemmer.stem();
        String term3v2=stemmer.toString();
        System.out.println("Prandtl\t\t" + documentFrequency(term3v2, indexVersion2) +
                "\t\t" + termFreq(term3v2, indexVersion2) + "\t\t" + getPostingSize(term3v2, indexVersion2) + " ");

        stemmer.add(term4.toLowerCase().toCharArray(), term4.length());
        stemmer.stem();
        String term4v2=stemmer.toString();
        System.out.println("flow\t\t" + documentFrequency(term4v2, indexVersion2) +
                "\t\t" + termFreq(term4v2, indexVersion2) + "\t\t" + getPostingSize(term4v2, indexVersion2) + " ");

        stemmer.add(term5.toLowerCase().toCharArray(), term5.length());
        stemmer.stem();
        String term5v2=stemmer.toString();
        System.out.println("pressure\t" + documentFrequency(term5v2, indexVersion2) +
                "\t\t" + termFreq(term5v2, indexVersion2) + "\t\t" + getPostingSize(term5v2, indexVersion2) + " ");

        stemmer.add(term6.toLowerCase().toCharArray(), term6.length());
        stemmer.stem();
        String term6v2=stemmer.toString();
        System.out.println("boundary\t" + documentFrequency(term6v2, indexVersion2) +
                "\t\t" + termFreq(term6v2, indexVersion2) + "\t\t" + getPostingSize(term6v2, indexVersion2) + " ");

        stemmer.add(term7.toLowerCase().toCharArray(), term7.length());
        stemmer.stem();
        String term7v2=stemmer.toString();
        System.out.println("shock\t\t" + documentFrequency(term7v2, indexVersion2) +
                "\t\t" + termFreq(term7v2, indexVersion2) + "\t\t" + getPostingSize(term7v2, indexVersion2) + " ");

    }
    public static int documentFrequency(String term, TreeMap<String, TreeMap<Integer, Integer>> index){
        TreeMap<Integer, Integer> posting= index.get(term);
        if(posting!=null)
            return posting.size();
        return 0;
    }
    public static int termFreq(String term, TreeMap<String, TreeMap<Integer, Integer>> index){
        TreeMap<Integer, Integer> posting= index.get(term);
        int count=0;
        for(Map.Entry<Integer, Integer> entry: posting.entrySet())
            count+= entry.getValue();
        return count;
    }
    public static long getPostingSize(String term, TreeMap<String, TreeMap<Integer, Integer>> index) throws IOException {
        TreeMap<Integer, Integer> posting= index.get(term);
        File tempFile= new File("TempPostingFile");
        ObjectOutputStream objectOutputStream= new ObjectOutputStream(new FileOutputStream(tempFile));
        objectOutputStream.writeObject(posting);
        objectOutputStream.close();
        objectOutputStream.flush();
        return tempFile.length();
    }

    public static void frontCoding(TreeMap<String, TreeMap<Integer, Integer>> index) {

        List<Object> testIndexList= new ArrayList<Object>();
        LinkedHashMap<Integer, Short> termFreqBlock= new LinkedHashMap<Integer, Short>();
        LinkedHashSet<Short> deltaEncodingSet= new LinkedHashSet<Short>();
        Set<String> terms = index.keySet();
        List<String> termsList = new ArrayList<String>();
        String termsArray[] = terms.toArray(new String[terms.size()]);
        int k = 8, currentK = 0, originalGap=0;
        String prefix = "";
        String temp = new String("");
        for (int i = 0; i < termsArray.length; i++) {
      //      System.out.println(termsArray[i]);
            if (currentK < k) {
                termsList.add(currentK, termsArray[i]);
                currentK++;

            }
            if (currentK == k || i + 1 == termsArray.length) {
                if (!(prefix = longestCommonPrefix(termsList)).equals("")) {
                    temp += "[";
                    for (int j = 0; j < termsList.size(); j++) {
                        if (termsList.get(j).startsWith(prefix)) {
                            if (j == 0)
                                temp += termsList.get(j).length() + prefix + "*" + termsList.get(j).substring(prefix.length());
                            if (j > 0) {
                                temp += termsList.get(j).substring(prefix.length()).length() + "|" + termsList.get(j).substring(prefix.length());
                            }
                        } else {
                            if (j == 0)
                                temp += termsList.get(j).length() + prefix + "*" + termsList.get(j).substring(0);
                            if (j > 0) {
                                temp += termsList.get(j).substring(0).length() + "|" + termsList.get(j).substring(0);
                            }

                        }
                        TreeMap<Integer, Integer> postingList= index.get(termsList.get(j));
                        for(Map.Entry<Integer, Integer> entry: postingList.entrySet()) {
                            originalGap = Math.abs(entry.getKey() - originalGap);
                            deltaEncodingSet.add(deltaEncoding(originalGap));
 //                           System.out.println(originalGap + "=" + deltaEncoding(originalGap));
                            originalGap = entry.getKey();
                        }
                        termFreqBlock.put(j, termFrequency(termsList.get(j), index));
                    }

                    temp += "]";
                    testIndexList.add(0, deltaEncodingSet);
                    testIndexList.add(1, termFreqBlock);
                    testIndexFrontCoding.put(temp, testIndexList);
 //                   System.out.println(temp+"="+testIndexList.toString());
                    currentK = 0;testIndexList.clear();termFreqBlock.clear();
                    termsList.clear();temp="";originalGap=0;deltaEncodingSet.clear();
                }
            }
        }
   //     return testIndexFrontCoding;
    }
    public static String longestCommonPrefix(List<String> strings){
        if(strings.size()==0)
            return "";
        int stringArrayLength=strings.size();
        for(int prefixLength=0; prefixLength<strings.get(0).length(); prefixLength++){
            char c= strings.get(0).charAt(prefixLength);
            for(int i=1; i<stringArrayLength; i++){
                if(prefixLength>=strings.get(i).length() || strings.get(i).charAt(prefixLength)!=c){
                    if(!strings.get(i).substring(0, prefixLength).equals("")){
                        return strings.get(i).substring(0, prefixLength);
                    }
                    else{
                        stringArrayLength--;
                        break;
                    }
                }
            }
        }
        return strings.get(0);
    }

    public static TreeMap<String, String> getStopwords(String stopwordFile) throws FileNotFoundException {
        TreeMap<String, String> stopwordsMap= new TreeMap<String, String>();
        Scanner read= new Scanner(new File(stopwordFile));
   //     stopwordsMap=new TreeMap<String, String>();
        while(read.hasNext()){
            String stopword=read.next();
            stopwordsMap.put(stopword, stopword);
        }
        read.close();
        return stopwordsMap;
    }

    public static HashMap<String, Integer> getStemmedTokens(HashMap<String, Integer> tokensInFile){
        HashMap<String, Integer> stemmedTokens= new HashMap<String, Integer>();

        Stemmer stemmer= new Stemmer();
        for(String token: tokensInFile.keySet()){
            stemmer.add(token.toCharArray(), token.length());
            stemmer.stem();
            String stemmedToken= stemmer.toString();
            if(stemmedTokens.get(stemmedToken)==null){
                stemmedTokens.put(stemmedToken, 1);
            }
            else{
                stemmedTokens.put(stemmedToken, stemmedTokens.get(stemmedToken)+1);
            }
        }
        return stemmedTokens;
    }

    public static HashMap<String, Integer> getTokens(File file){
        HashMap<String, Integer> tokensInFile= new HashMap<String, Integer>();
        ArrayList<String> normalizedTokens= getNormalizedTokens(file);
        for(String nTokens: normalizedTokens){
            ArrayList<String> noPuncTokens= removePunctuations(nTokens);
            if(noPuncTokens!=null){
                for(String noPuncToken: noPuncTokens){
                    if(tokensInFile.get(noPuncToken)==null){
                        tokensInFile.put(noPuncToken, 1);
                    }
                    else{
                        tokensInFile.put(noPuncToken, tokensInFile.get(noPuncToken)+1);
                    }
                }
            }
        }
        return tokensInFile;
    }

    public static ArrayList<String> removePunctuations(String tokens){
        ArrayList<String> noPuncTokens= new ArrayList<String>();
        ArrayList<String> noPuncTokensCopy= new ArrayList<String>();
        if(tokens.contains("-")){
            String noHyphens[]= tokens.split("-");
            for(String noH: noHyphens){
                noPuncTokens.add(noH.toLowerCase());
            }
        }
        else
            noPuncTokens.add(tokens.toLowerCase());

        for(String token: noPuncTokens){
            if(token.contains(".") && token.length()>1){
                noPuncTokensCopy.add(token.replaceAll("\\.", ""));
            }
            else if(!token.contains("."))
                noPuncTokensCopy.add(token.toLowerCase());
        }
        noPuncTokens.clear();

        for(String token: noPuncTokensCopy){
            if(token.endsWith("'s")){
                noPuncTokens.add(token.replace("'s", "").trim());
            }
            else if(token.contains("'") && token.length()>1){
                noPuncTokens.add(token.replace("'", ""));
            }
            else if(!token.contains("'"))
                noPuncTokens.add(token.toLowerCase());
        }
        return noPuncTokens;
    }

    public static ArrayList<String> getNormalizedTokens(File file){
        ArrayList<String> tokens= new ArrayList<String>();
        try {
            String fileContent= Jsoup.parse(file, "UTF-8").text();
            String normalizedTokens= fileContent.replaceAll("[^a-zA-Z0-9'.]", " ").replaceAll("[\\s+]", " ").trim();
            String normalizedTokensArray[]= normalizedTokens.split(" ");

            for(String t: normalizedTokensArray){
                if(t.length()>0){
                    tokens.add(t.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            }
        return tokens;
    }

    public static HashMap<String, Integer> removeStopwords(HashMap<String, Integer> lemmaHashMap, TreeMap<String, String> stopwords) throws FileNotFoundException {
        Iterator<Map.Entry<String, Integer>> lemmaIterator= lemmaHashMap.entrySet().iterator();
        HashMap<String, Integer> lemmaNormalizedHashMap= new HashMap<String, Integer>();
        while(lemmaIterator.hasNext()){
            Map.Entry<String, Integer> lemmaMap= lemmaIterator.next();
                if(!stopwords.containsKey(lemmaMap.getKey())){
                    lemmaNormalizedHashMap.put(lemmaMap.getKey(), lemmaMap.getValue());
                }
        }
        return lemmaNormalizedHashMap;
    }

    /********************************index creation methods*********************************/
    public static void createIndex(int docID, HashMap<String, Integer> normalizedHashMap, TreeMap<String, TreeMap<Integer, Integer>> index){
        Iterator<Map.Entry<String, Integer>> iterator= normalizedHashMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, Integer> entry=iterator.next();
            // entry.getkey, docid, entry.getvalue(entrygetkey)
            insertInIndex(entry.getKey(), docID, entry.getValue(), index);
        }
    }

    public static void insertInIndex(String term, int docID, int termFrequency, TreeMap<String, TreeMap<Integer, Integer>> index){
        TreeMap<Integer, Integer> currentPostingList= index.get(term);
        if(currentPostingList!=null){
            currentPostingList.put(docID, termFrequency);
            index.put(term, currentPostingList);
        }
        else{
            TreeMap<Integer, Integer> newPostingList= new TreeMap<Integer, Integer>();
            newPostingList.put(docID, termFrequency);
            index.put(term, newPostingList);
        }
    }
    public static void writeUncompressedIndex(String indexDirectoryPath, String indexName, TreeMap<String, TreeMap<Integer, Integer>> index) throws IOException {
        uncompressedIndexFile= new File(indexDirectoryPath +"/"+ indexName);
        ObjectOutputStream objectOutputStream= new ObjectOutputStream(new FileOutputStream(uncompressedIndexFile));
        objectOutputStream.writeObject(index);
        objectOutputStream.flush();
        objectOutputStream.close();
    }

  /*  public static void writeCompressedIndex(String indexDirectoryPath, String indexName, TreeMap<String, LinkedHashMap<Short, Short>> compressedIndex) throws IOException {
        compressedIndexFile= new File(indexDirectoryPath +"/"+ indexName);
        ObjectOutputStream objectOutputStream= new ObjectOutputStream(new FileOutputStream(compressedIndexFile));
        objectOutputStream.writeObject(compressedIndex);
        objectOutputStream.flush();
        objectOutputStream.close();
    }*/

    public static void writeCompressedIndex(String indexDirectoryPath, String indexName, LinkedHashMap<String, List<Object>> compressedIndex) throws IOException {
        compressedIndexFile= new File(indexDirectoryPath +"/"+ indexName);
        ObjectOutputStream objectOutputStream= new ObjectOutputStream(new FileOutputStream(compressedIndexFile));
        objectOutputStream.writeObject(compressedIndex);
        objectOutputStream.flush();
        objectOutputStream.close();
    }
    public static void compressIndex(TreeMap<String, TreeMap<Integer, Integer>> index, TreeMap<String, LinkedHashMap<Short, Short>> compressedIndex, String encodingType){
        Set<String> terms= index.keySet();
        for(String term: terms){
            TreeMap<Integer, Integer> postingList= index.get(term);
            LinkedHashMap<Short, Short> postingListWithGaps= new LinkedHashMap<Short, Short>();
            int originalGap=0;
            for(Map.Entry<Integer, Integer> entry: postingList.entrySet()){
                originalGap= entry.getKey()-originalGap;
                if(encodingType.equals("gamma")){
            //        postingListWithGaps.put(gammaEncoding(originalGap), gammaEncoding(entry.getValue()));
                    postingListWithGaps.put(gammaEncoding(originalGap), termFrequency(term, index));
                }
                else if(encodingType.equals("delta"))
                    postingListWithGaps.put(deltaEncoding(originalGap), deltaEncoding(entry.getValue()));

                originalGap= entry.getKey();
            }
            compressedIndex.put(term, postingListWithGaps);
        }
    }

    public static LinkedHashMap<String, List<Object>> blockCompression(TreeMap<String, TreeMap<Integer, Integer>> index){
        int k=8;int currentK=0;
        LinkedHashMap<Integer, Short> termFreqBlock= new LinkedHashMap<Integer, Short>();
        List<Object> testIndexList= new ArrayList<Object>();
        String dictionaryString=new String("");
        Set<String> terms= index.keySet();
        LinkedHashSet<Short> gammaEncodingSet= new LinkedHashSet<Short>();
        int originalGap=0;

        String termsArray[]= terms.toArray(new String[terms.size()]);
        for(int i=0; i<termsArray.length; i++){
            if(currentK<k){
                dictionaryString+="".concat(termsArray[i].length()+"").concat(termsArray[i]);
                TreeMap<Integer, Integer> postingList= index.get(termsArray[i]);
                LinkedHashMap<Short, Short> postingListWithGaps= new LinkedHashMap<Short, Short>();
                for(Map.Entry<Integer, Integer> entry: postingList.entrySet()){
                    originalGap= Math.abs(entry.getKey()-originalGap);
                    gammaEncodingSet.add(gammaEncoding(originalGap));

                    originalGap= entry.getKey();
                }
                termFreqBlock.put(currentK, termFrequency(termsArray[i], index));
                currentK++;
            }
            if(currentK==k ||  (i+1==termsArray.length)){
                currentK=0;originalGap=0;
                testIndexList.add(0, gammaEncodingSet);
                testIndexList.add(1, termFreqBlock);
                testIndexBlockVersion1.put(dictionaryString, testIndexList);
                dictionaryString="";gammaEncodingSet.clear();
                testIndexList.clear();termFreqBlock.clear();
            }
        }
        return testIndexBlockVersion1;
    }

    public static short gammaEncoding(int valueToEncode){   //valueToEncode= 5

        if(valueToEncode>0){
            int offsetUnaryLength=0;
            int unary=0;
            int valueToEncodeCopy=valueToEncode;

            while(valueToEncodeCopy!=1){
                valueToEncodeCopy/=2;
                offsetUnaryLength++;        //offsetUnaryLength= 2
            }
            int offset=1 << offsetUnaryLength;  //offset= 1<<2= 100
            offset-=1;                          //offset= 100-1= 99
            offset=offset & valueToEncode;  //offset= 99&5= 1
            int offsetLengthCopy= offsetUnaryLength;        //offsetLengthCopy= 2
            while(offsetUnaryLength!=0){
                unary= unary << 1;
                unary= unary | 1;
                offsetUnaryLength--;
            }                                   //unary= 11
            unary= unary << 1;                  //unary= 11<<1= 110
            unary= unary << offsetLengthCopy;   //unary= 110<<2= 11000
            offset= offset | unary; // offset= 1|11000= 11001

            return (short)offset;
        }
        else return (short)-1;
    }

    public static short deltaEncoding(int valueToEncode){   //valueToEncode=5

        if(valueToEncode>0){
            int offsetUnaryLength=1;
        int valueToEncodeCopy=valueToEncode;

        while(valueToEncodeCopy>1){
            valueToEncodeCopy/=2;
            offsetUnaryLength++;        //offsetUnaryLength= 3
        }
        int code= gammaEncoding(offsetUnaryLength);     //code=101
        int offsetUnaryLengthCopy=0;
        int valueToEncodeCopy2=valueToEncode;       //valueToEncodeCopy2=5
        while (valueToEncodeCopy2!=1){
            valueToEncodeCopy2/=2;
            offsetUnaryLengthCopy++;            //offsetUnaryLengthCopy=2
        }
        int offset=1 << offsetUnaryLengthCopy;  //offset= 1<<2= 100
        offset-=1;                          //offset= 100-1= 99
        offset=offset & valueToEncode;  //offset= 99&5= 1
        code= code << offsetUnaryLengthCopy;   //code= 101<<2= 10100
        code= offset | code; // offset= 1|10100= 10101

        return (short)code;
        }
        else return (short)-1;
    }
    /***************************************document information methods***********************************************/
    public static void termFrequencyOfIndex(TreeMap<String, TreeMap<Integer, Integer>> index){
        Set<String> terms= index.keySet();
        for(String term: terms){
            int tF=0;
            TreeMap<Integer, Integer> termInfo= index.get(term);
            Set<Integer> termInfoDocIDs= termInfo.keySet();
            for(Integer termInfoDocID : termInfoDocIDs){
                tF+=termInfo.get(termInfoDocID);
            }
            termFreq.put(term, tF);
        }
    }

    public static short termFrequency(String term, TreeMap<String, TreeMap<Integer, Integer>> index){
        int tF=0;
        TreeMap<Integer, Integer> termInfo= index.get(term);
        Set<Integer> termInfoDocIDs= termInfo.keySet();
        for(Integer termInfoDocID : termInfoDocIDs){
            tF+=termInfo.get(termInfoDocID);
        }
        return (short)tF;
    }
}
//TODO: document info
//TODO: display output
