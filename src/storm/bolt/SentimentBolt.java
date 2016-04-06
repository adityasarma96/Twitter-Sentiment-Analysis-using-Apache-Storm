package storm.bolt;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.testing.TestWordSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import java.util.Map;
import storm.tools.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.trees.*;

import java.util.*;


/**
 * A bolt that gets the sentiment of a tweet, then emit the sentiment with its
 * state info
 */
public class SentimentBolt extends BaseRichBolt
{
  private OutputCollector collector;
  private SentimentAnalyzer sentiment;
  @Override
  public void prepare(
      Map                     map,
      TopologyContext         topologyContext,
      OutputCollector         outputCollector)
  {
    collector = outputCollector;
    sentiment = new SentimentAnalyzer();
    sentiment.init();
  }

  @Override
  public void execute(Tuple tuple)
  {

    String tweet = tuple.getString(0);
    String state = tuple.getString(1);
    Double tweetSentiment = sentiment.findSentiment(tweet);
    float a=0,n=0;
    String text = tweet;
		Properties props = new Properties();
		props.setProperty("annotators",
                "tokenize, ssplit, pos, lemma, parse, sentiment");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation annotation = pipeline.process(text);

		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		for(CoreMap sentence: sentences){
			String sentiment = sentence.get(SentimentCoreAnnotations.ClassName.class);
			Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
			int s = RNNCoreAnnotations.getPredictedClass(tree);
			a+=s;
			n++;
		}
		tweetSentiment= (double) (a/n);
    collector.emit(new Values(tweetSentiment, state));
  }

  public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer)
  {
    outputFieldsDeclarer.declare(new Fields("sentiment", "state"));
  }
}
