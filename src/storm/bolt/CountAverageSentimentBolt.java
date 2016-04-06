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

import java.util.HashMap;
import java.util.Map;

/**
 * A bolt that counts the average sentiment
 */
public class CountAverageSentimentBolt extends BaseRichBolt
{
  private OutputCollector collector;
  private long intervalToReport = 20;
  private long lastReportTime = System.currentTimeMillis();

  // Map to store the count of the words and average sentiment
  private Map<String, Integer> countMap;
  private Map<String, Double> sentimentMap;

  @Override
  public void prepare(
      Map                     map,
      TopologyContext         topologyContext,
      OutputCollector         outputCollector)
  {
    collector = outputCollector;
    countMap = new HashMap<String, Integer>();
    sentimentMap = new HashMap<String, Double>();
  }

  @Override
  public void execute(Tuple tuple)
  {
    String state = tuple.getString(1);
    Double sentiment = tuple.getDouble(0);
    if (countMap.get(state) == null) {
        countMap.put(state, 1);
        sentimentMap.put(state, sentiment);
    } else {
        int val = countMap.get(state);
        double avgSentiment = (sentimentMap.get(state)*val + sentiment)/(val + 1);
        countMap.put(state, ++val);
        sentimentMap.put(state, avgSentiment);
    }

    // emit the word and count
    if (System.currentTimeMillis() - lastReportTime >= intervalToReport) {
        double sentimentAvg = sentimentMap.get(state);
        String sentimentReadable = "";

        if(sentimentAvg < 0.5){
          sentimentReadable = "Super Unhappy "+ String.valueOf(sentimentAvg);
        }else if(sentimentAvg < 1){
          sentimentReadable = "Very Unhappy "+ String.valueOf(sentimentAvg);
        }else if(sentimentAvg < 1.5){
          sentimentReadable = "Unhappy "+ String.valueOf(sentimentAvg);
        }else if(sentimentAvg < 1.75){
          sentimentReadable = "Little Unhappy "+ String.valueOf(sentimentAvg);
        }else if(sentimentAvg < 2.25){
          sentimentReadable = "Neutral "+ String.valueOf(sentimentAvg);
        }else if(sentimentAvg < 2.5){
          sentimentReadable = "Little Happy "+ String.valueOf(sentimentAvg);
        }else if(sentimentAvg < 3.0){
          sentimentReadable = "Happy "+ String.valueOf(sentimentAvg);
        }else if(sentimentAvg < 3.5){
          sentimentReadable = "Very Happy "+ String.valueOf(sentimentAvg);
        }else{
          sentimentReadable = "Super Happy "+ String.valueOf(sentimentAvg);
        }
        collector.emit(new Values(sentimentReadable, state));
        lastReportTime = System.currentTimeMillis();
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer)
  {
    // tell storm the schema of the output tuple for this spout
    // tuple consists of a two columns called 'word' and 'count'

    // declare the first column 'word', second column 'count'
    outputFieldsDeclarer.declare(new Fields("avg-sentiment","state"));
  }
}
