package storm.topology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;
import storm.spout.TweetSpout;
import storm.bolt.StateBolt;
import storm.bolt.SentimentBolt;
import storm.bolt.ReportBolt;
import storm.bolt.TestBolt;
import storm.bolt.CountAverageSentimentBolt;

class SentimentTweetTopology
{
  public static void main(String[] args) throws Exception
  {
    TopologyBuilder builder = new TopologyBuilder();

    /*
     * In order to create the spout, you need to get twitter credentials
     * If you need to use Twitter firehose/Tweet stream for your idea,
     * create a set of credentials by following the instructions at
     *
     * https://dev.twitter.com/discussions/631
     *
     */
    // now create the tweet spout with the credentials
    // credential
    TweetSpout tweetSpout = new TweetSpout(
    "",
    "",
    "",
    ""
    );

    // attach the tweet spout to the topology - parallelism of 1
    builder.setSpout("tweet-spout", tweetSpout, 1);
    builder.setBolt("state-bolt", new StateBolt(), 10).shuffleGrouping("tweet-spout");
    builder.setBolt("sentiment-bolt", new SentimentBolt(),10).shuffleGrouping("state-bolt");
    builder.setBolt("count-avg-sentiment-bolt", new CountAverageSentimentBolt(), 10).
                    fieldsGrouping("sentiment-bolt", new Fields("state"));
    //builder.setBolt("test-bolt", new TestBolt(), 1).globalGrouping("count-avg-sentiment-bolt");
    builder.setBolt("report-bolt", new ReportBolt(), 1).globalGrouping("count-avg-sentiment-bolt");
    // create the default config object
    Config conf = new Config();

    // set the config in debugging mode
    conf.setDebug(true);

    if (args != null && args.length > 0) {

      // run it in a live cluster

      // set the number of workers for running all spout and bolt tasks
      conf.setNumWorkers(3);

      // create the topology and submit with config
      StormSubmitter.submitTopology(args[0], conf, builder.createTopology());

    } else {

      // run it in a simulated local cluster

      // set the number of threads to run - similar to setting number of workers in live cluster
      conf.setMaxTaskParallelism(4);

      // create the local cluster instance
      LocalCluster cluster = new LocalCluster();

      // submit the topology to the local cluster
      cluster.submitTopology("state-tweet-sentiment", conf, builder.createTopology());

      // let the topology run for 300 seconds. note topologies never terminate!
      Utils.sleep(30000);

      // now kill the topology
      cluster.killTopology("state-tweet-sentiment");

      // we are done, so shutdown the local cluster
      cluster.shutdown();
      System.out.println("The End");
    }
  }
}
