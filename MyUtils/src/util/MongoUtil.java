package util;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MongoUtil {
	private static MongoClient mongoClient;
	private static DB db;
	static DBCollection dictionaryCollection;
	
	static{
		try {
			mongoClient = new MongoClient();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		db = mongoClient.getDB("test");
		dictionaryCollection = db.getCollection("dictionaryCollection");
	}

	@Override
	protected void finalize() throws Throwable {
		mongoClient.close();
		super.finalize();
	}
}
