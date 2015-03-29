import org.jsoup.Jsoup;

import java.io.*;
import java.util.*;

/**
 * Created by miren_t on 3/21/2015.
 */
/*TODO: during index construction, term should be compressed using block and front coding
* */
public class IR2 {
    static TreeMap<String, String> stopwordsMap= new TreeMap<String, String>();
    static TreeMap<String, TreeMap<Integer, Integer>> indexVersion1= new TreeMap<String, TreeMap<Integer, Integer>>();
    static TreeMap<String, LinkedHashMap<Short, Short>> compressedIndexVersion1= new TreeMap<String, LinkedHashMap<Short, Short>>();
    static TreeMap<String, TreeMap<Integer, Integer>> indexVersion2= new TreeMap<String, TreeMap<Integer, Integer>>();
    static TreeMap<String, LinkedHashMap<Short, Short>> compressedIndexVersion2= new TreeMap<String, LinkedHashMap<Short, Short>>();
    static TreeMap<String, Integer> termFreq= new TreeMap<String, Integer>();
    static LinkedHashMap<String, List<Object>> testIndexBlockVersion1= new LinkedHashMap<String, List<Object>>();
    static File uncompressedIndexFile, compressedIndexFile;

    static int numberOfFiles=0;
    public static void main(String[] args) {

        String CRANFIELD_DIRECTORY=args[0];
        String OUTPUT_PATH="";

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
        getStopwords("stopwords");
        numberOfFiles=listOfFiles.length;
        int[][] documents = new int[numberOfFiles + 1][2];
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
            //TODO: Version 1: Dictionary compression--> blocked ______ posting file--> gamma
            //TODO: Version 2: Dictionary compression--> front coding ______ posting file--> delta

            createIndex(docID, lemmaNormalizedHashMap, indexVersion1);
        }
        double timeTakenVersion1= (System.currentTimeMillis() - startTimeVersion1)/1000;

        System.out.println("Writing uncompressed index..");
        writeUncompressedIndex("./", "Index_Version1.uncompressed", indexVersion1);

        System.out.println("Done..\nCompressing index..");
        blockCompression(indexVersion1);

        System.out.println("Done..\nWriting compressed index..");
        writeCompressedIndex("./", "Index_Version1.compressed", testIndexBlockVersion1);

        System.out.println("Done..");
        System.out.println("Time taken by version 1: " + timeTakenVersion1);


   /*     System.out.println("*******************************************");
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
            //TODO: Version 1: Dictionary compression--> blocked ______ posting file--> gamma
            //TODO: Version 2: Dictionary compression--> front coding ______ posting file--> delta

            createIndex(docID, stemmedNormalizedHashMap, indexVersion2);
        }
        double timeTakenVersion2= (System.currentTimeMillis() - startTimeVersion2)/1000;
        System.out.println("Writing uncompressed index..");
        writeUncompressedIndex("./", "Index_Version2.uncompressed", indexVersion2);
        System.out.println("Done..\nCompressing index..");
        compressIndex(indexVersion2, compressedIndexVersion2, "delta");
        System.out.println("Done..\nWriting compressed index..");
        writeCompressedIndex("./", "Index_Version2.compressed", compressedIndexVersion2);
        System.out.println("Done..");
        System.out.println("Time taken by version 2: " + timeTakenVersion2);
*/
    } /*
    TODO: document frequency --> dF, need file of document information?
    */
    public static TreeMap<String, String> getStopwords(String stopwordFile) throws FileNotFoundException {
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
    /*TODO: is this right?*/stemmedTokens.put(stemmedToken, 1);
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
    public static List<Object> testIndexList= new ArrayList<Object>();
    public static TreeMap<Integer, Short> termFreqBlock= new TreeMap<Integer, Short>();
    //public static LinkedHashMap<String, List<Object>> example= new LinkedHashMap<String, List<Object>>();
    public static LinkedHashMap<String, List<Object>> blockCompression(TreeMap<String, TreeMap<Integer, Integer>> index){
        int k=8;int currentK=0;
        String dictionaryString=new String("");
        Set<String> terms= index.keySet();
        TreeSet<Short> gammaEncodingSet= new TreeSet<Short>();
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
                dictionaryString="";
                testIndexList.clear();termFreqBlock.clear();
            }
        }
        return testIndexBlockVersion1;
    }
    public static void compressIndexTest(){

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
