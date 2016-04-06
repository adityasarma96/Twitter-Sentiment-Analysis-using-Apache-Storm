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
import storm.geocode.GeoLookup;
import java.util.Map;
import storm.tools.*;

/**
 * A bolt that get the State info of the tweet, then emit the tweet with its
 * STATE INFO
 */
public class StateBolt extends BaseRichBolt
{
  private OutputCollector collector;
  private GeoLookup lookup;
  @Override
  public void prepare(
      Map                     map,
      TopologyContext         topologyContext,
      OutputCollector         outputCollector)
  {
    collector = outputCollector;
    lookup = new GeoLookup();
  }

  @Override
  public void execute(Tuple tuple)
  {

    String tweet = tuple.getString(0);
    String geoinfo = tuple.getString(1);
    int commapos = geoinfo.indexOf(",");
    Double latitude = Double.parseDouble(geoinfo.substring(0, commapos));
    Double longitude = Double.parseDouble(geoinfo.substring(commapos + 1));
    String st = lookup.getStateByGeo(latitude, longitude);
    collector.emit(new Values(tweet, st));
  }

  public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer)
  {
    outputFieldsDeclarer.declare(new Fields("tweet", "state"));
  }
}
