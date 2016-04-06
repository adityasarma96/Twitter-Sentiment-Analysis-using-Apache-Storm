package storm.tools;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.util.Properties;

public class SentimentAnalyzer {
    static StanfordCoreNLP pipeline;

    public static void init() {
    	Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,parse,sentiment");
        pipeline = new StanfordCoreNLP(props);
    }

    public static double findSentiment(String tweet) {

        int sumSentiment = 0;
        double n = 0;
        if (tweet != null && tweet.length() > 0) {
            int longest = 0;
            Annotation annotation = pipeline.process(tweet);
            for (CoreMap sentence : annotation
                    .get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence
                        .get(SentimentCoreAnnotations.AnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                sumSentiment += sentiment;
                n++;

            }
        }
        return sumSentiment/n;
    }
}
