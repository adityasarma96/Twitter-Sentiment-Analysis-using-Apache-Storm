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

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import storm.tools.*;

/**
 * A bolt that prints the word and count to redis
 */
public class ReportBolt extends BaseRichBolt
{

  HashMap<String,String> stmap = new HashMap<String,String>();
  OutputCollector coll;

  @Override
  public void prepare(
      Map                     map,
      TopologyContext         topologyContext,
      OutputCollector         outputCollector)
  {
    // instantiate a redis connection
    coll = outputCollector;
  }

  @Override
  public void execute(Tuple tuple)
  {
    String sentiment = tuple.getStringByField("avg-sentiment");
    String state = tuple.getStringByField("state");
    System.out.println(state + "   "+ sentiment);
    stmap.put(state,sentiment);

  }
  public void cleanup() {
		// TODO Auto-generated method stub
		try
		{
			File dumpfile = new File("sentiment.txt");
			if (dumpfile.exists()==false)
				dumpfile.createNewFile();
			FileWriter fw = new FileWriter(dumpfile,true);
			Iterator<String> it = stmap.keySet().iterator();
			while(it.hasNext())
			{
				String temp = it.next();
				fw.write(temp + " " + stmap.get(temp)+"\n");
			}
			fw.flush();
			fw.close();
		}
		catch(Exception e)
		{ System.out.println("Exception in Clean UP");e.printStackTrace();}
	}

  public void declareOutputFields(OutputFieldsDeclarer declarer)
  {
    // nothing to add - since it is the final bolt
  }
}
