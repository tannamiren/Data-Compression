import java.io.*;
import java.util.*;

/**
 * Created by miren_t on 3/21/2015.
 */
/*TODO: during index construction, term should be compressed using block and front coding
* */
public class IR2 {
    static TreeMap<String, String> stopwordsMap= new TreeMap<String, String>();
    static TreeMap<String, TreeMap<Integer, Integer>> index= new TreeMap<String, TreeMap<Integer, Integer>>();
    static File uncompressedIndex;

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

        for(File files:listOfFiles){
            int docID=Integer.parseInt(files.getName().substring(9));

            HashMap<String, Integer> lemmaHashMap= new Lemmatizer().lemmatize(files);
            HashMap<String, Integer> lemmaNormalizedHashMap= removeStopwords(lemmaHashMap, stopwords);
         //working   System.out.println(docId + " " + lemmaNormalizedHashMap.size());
            //TODO: Version 1: Dictionary compression--> blocked ______ posting file--> gamma
            //TODO: Version 2: Dictionary compression--> front coding ______ posting file--> delta

            createIndex(docID, lemmaNormalizedHashMap);

        }
        writeUncompressedIndex("./", "Index_Version1.uncompressed");

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
    public static void createIndex(int docID, HashMap<String, Integer> lemmaNormalizedHashMap){
        Iterator<Map.Entry<String, Integer>> iterator= lemmaNormalizedHashMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, Integer> entry=iterator.next();
            // entry.getkey, docid, entry.getvalue(entrygetkey)
            insertInIndex(entry.getKey(), docID, entry.getValue());
        }
    }
    public static void insertInIndex(String term, int docID, int termFrequency){
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
    public static void writeUncompressedIndex(String indexDirectoryPath, String indexName) throws IOException {
        uncompressedIndex= new File(indexDirectoryPath +"/"+ indexName);
        ObjectOutputStream objectOutputStream= new ObjectOutputStream(new FileOutputStream(uncompressedIndex));
        objectOutputStream.writeObject(index);
        objectOutputStream.flush();
        objectOutputStream.close();
    }
}
