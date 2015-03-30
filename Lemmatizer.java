/**
 * Created by miren_t on 3/21/2015.
 */

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

public class Lemmatizer {
    StanfordCoreNLP pipeline;
    Properties properties;
    public Lemmatizer() {
        properties = new Properties();
        properties.put("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(properties);
    }


    public TreeMap<String, Integer> lemmatize(File file) throws IOException {
        String fileContent= Jsoup.parse(file, "UTF-8").text();
        Annotation annotation= new Annotation(fileContent);
        pipeline.annotate(annotation);
        List<CoreMap> sentenceCoreMapList= annotation.get(CoreAnnotations.SentencesAnnotation.class);

        TreeMap<String, Integer> lemmaHashMap= new TreeMap<String, Integer>();
        for(CoreMap coreMap: sentenceCoreMapList){
            for(CoreLabel coreLabel: coreMap.get(CoreAnnotations.TokensAnnotation.class)){
                String lemma = coreLabel.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase();

                if(lemma.matches("[a-zA-Z0-9]+")){
                    if(lemmaHashMap.get(lemma) == null){
                        lemmaHashMap.put(lemma, 1);
                    }
                    else{
                        lemmaHashMap.put(lemma, lemmaHashMap.get(lemma)+1);
                    }
                }
            }

        }
        return lemmaHashMap;
    }
    public HashMap<String, Integer> lemmatizeHash(File file) throws IOException {
        String fileContent= Jsoup.parse(file, "UTF-8").text();
        Annotation annotation= new Annotation(fileContent);
        pipeline.annotate(annotation);
        List<CoreMap> sentenceCoreMapList= annotation.get(CoreAnnotations.SentencesAnnotation.class);

        HashMap<String, Integer> lemmaHashMap= new HashMap<String, Integer>();
        for(CoreMap coreMap: sentenceCoreMapList){
            for(CoreLabel coreLabel: coreMap.get(CoreAnnotations.TokensAnnotation.class)){
                String lemma = coreLabel.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase();

                if(lemma.matches("[a-zA-Z0-9]+")){
                    if(lemmaHashMap.get(lemma) == null){
                        lemmaHashMap.put(lemma, 1);
                    }
                    else{
                        lemmaHashMap.put(lemma, lemmaHashMap.get(lemma)+1);
                    }
                }
            }

        }
        return lemmaHashMap;
    }
    public String lemmatizeString(String text) throws IOException {
        Annotation annotation= new Annotation(text);
        pipeline.annotate(annotation);
        String lemma="";
        List<CoreMap> sentence= annotation.get(CoreAnnotations.SentencesAnnotation.class);

        for(CoreMap coreMap: sentence){
            for(CoreLabel coreLabel: coreMap.get(CoreAnnotations.TokensAnnotation.class)){
                lemma = coreLabel.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase();
            }

        }
        return lemma;
    }
}
