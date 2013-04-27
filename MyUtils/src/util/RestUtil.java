package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class RestUtil {

	public static String get(String uri) {
		System.getProperties().put("http.proxyHost", "proxy.iiit.ac.in");
		System.getProperties().put("http.proxyPort", "8080");
		System.getProperties().put("https.proxyHost", "proxy.iiit.ac.in");
		System.getProperties().put("https.proxyPort", "8080");
		
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(uri);
		HttpResponse response = null;
		try {
			response = client.execute(request);
			BufferedReader rd = null;
			rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
