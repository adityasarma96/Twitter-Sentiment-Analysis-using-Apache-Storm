import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;


public class RedisTest {

	transient RedisConnection<String,String> redis;
	
	public void conn(){
	    RedisClient client = new RedisClient("localhost",6379);

	    // initiate the actual connection
	    redis = client.connect();
	    for(int i=0;i<500;i++){
	    	redis.publish("SentimentTweetTopology", String.valueOf(i));
	    	System.out.println(i);
	    }
	    
	   
	}
	public static void main(String[] args){
		
		RedisTest t = new RedisTest();
		t.conn();
	   }
}
