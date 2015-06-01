package dcp.congestionTag;

/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 * @author Fengmin Deng (Student ID: 659332)
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

/**
 * This class is responsible for updating the entries in CouchDB databases
 *   with congestion labels. Basically the clusters created by ELKI are taken
 *   as possible congestion. The cluster files of three cities would be read
 *   and each tweet record would be examined to be labeled as "in a cluster"
 *   (congestion indicator) or "not in any cluster".
 */
public class ClusterLabelMaker {
	private static final Charset charset = Charset.forName("UTF-8");
	private static int conflictCount = 0;
	
	public static void main(String[] args) {
		
		JsonObject paras = Utils.validateCmdArgs(args);
		
		markCongestion(paras.getAsJsonObject("brisbane"));
		printConflictNumberAndReset("Brisbane");
		
		markCongestion(paras.getAsJsonObject("melbourne"));
		printConflictNumberAndReset("Melbourne");
		
		markCongestion(paras.getAsJsonObject("sydney"));
		printConflictNumberAndReset("Sydney");
		
	
	}
	
	/**
	 * This method reads in the result file of clustering, maps each entry with
	 *   tweet id, retrieves the tweet record, adds in clustering information
	 *   and finally update the tweet record in CouchDB.
	 * @param city The parameters regarding a city.
	 */
	private static void markCongestion(JsonObject city) {
		Map<Integer, String> tweetIds = mapIds(city.get("file_elki_in").getAsString());
		
		String dbURL = city.get("url_db").getAsString();
		String clustersFileName = city.get("file_elki_out").getAsString();
		Path path = FileSystems.getDefault().getPath(clustersFileName);
		String line = null;
		JsonObject clusterInfo = new JsonObject();
		
		try {
			BufferedReader reader = Files.newBufferedReader(path, charset);
		    while ((line = reader.readLine()) != null) {
		    	if (line.startsWith("# Cluster: ")) {
		    		String clusterName = line.replace("# Cluster: ", "");
		    		clusterInfo.addProperty("cluster_name", clusterName);
		    		clusterInfo.addProperty("noise_flag", clusterName.equals("Noise"));
		    		reader.readLine(); // skip the line "# Cluster name: Cluster" or "# Cluster name: Noise"
		    		reader.readLine(); // skip the line "# Cluster noise flag:..."
		    		line = reader.readLine(); // line for cluster size
		    		int clusterSize = Integer.valueOf(line.replace("# Cluster size: ", ""));
		    		clusterInfo.addProperty("cluster_size", clusterSize);
		    		for (int i = 0; i < clusterSize; ++i) {
		    			line = reader.readLine(); // the entry under a cluster
		    			String[] cells = line.split(" ");
		    			int elkiId = Integer.valueOf(cells[0].replace("ID=", ""));
		    			String tweetId = tweetIds.get(elkiId);
		    			JsonObject tweet = getTweetByID(dbURL + tweetId);
		    			if (tweet == null) {
		    				System.out.println(tweetId + " : something wrong in retrieving this tweet");
		    				continue;
		    			}
		    			tweet.add("cluster_info", clusterInfo);
		    			updateTweet(dbURL + tweetId, tweet.toString());
		    		}
		    	}
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
	}
	
	/**
	 * This method maps the tweet IDs from the ELKI input file to the entry
	 *   ID of ELKI after clustering. The HashMap will then be used to update
	 *   the relevant tweet entry with cluster information.
	 * @param tweetsIds
	 */
	private static Map<Integer, String> mapIds(String fileName) {
		Map<Integer, String> tweetIds = new HashMap<>();
		Path path = FileSystems.getDefault().getPath(fileName);
		BufferedReader reader = null;
		String line = null;
		int elkiId = 1; //ELKI ID starts from 1
		try {
			reader = Files.newBufferedReader(path, charset);
			reader.readLine(); // drop the first line with titles
//			System.out.println("Starting to read file now....");
		    while ((line = reader.readLine()) != null) {
		    	String[] entry = line.split(" ");
		    	tweetIds.put(elkiId, entry[3]); // the 3rd element is tweet id
//		        System.out.println(elkiId + ":" + tweetIds.get(elkiId));
		        ++elkiId;
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		}
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (Exception e) {
		}
		return tweetIds;
	}
	
	/**
	 * This method retrieves one tweet record by its ID
	 * @param url
	 * @return tweet record in Json format
	 */
	private static JsonObject getTweetByID(String url) {
		JsonObject tweet = null;
		BufferedReader reader = null;
		String line = null;
		StringBuilder sb = new StringBuilder();
		try {
			URL myURL = new URL(url);
			HttpURLConnection httpConnection = (HttpURLConnection) myURL.openConnection();
			httpConnection.setRequestProperty("Accept-Charset", Utils.UTF8);
			
			int status = httpConnection.getResponseCode();
			
			if (status != HttpURLConnection.HTTP_OK) {
				System.out.println("Something wrong when retrieving tweet data from CouchDB.");
				System.out.println("URL: " + httpConnection.getURL());
				System.out.println("HTTP Response Message: " + httpConnection.getResponseMessage());
			}
			
			reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream(), Utils.UTF8));

			while ((line = reader.readLine()) != null) {
				sb = sb.append(line);
			}
			String json = sb.toString();

			if (json != null) {
				tweet = Utils.toJson(json);
			}
		} catch (Exception e) {
		} 
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (Exception e) {
		}
		return tweet;
	}

	/**
	 * This method update the tweet record with cluster information by HTTP PUT
	 * @param url
	 * @param tweet
	 */
	private static void updateTweet(String url, String tweet) {		
		try {
			URL myURL = new URL(url);
			HttpURLConnection httpConnection = (HttpURLConnection) myURL.openConnection();
			httpConnection.setRequestProperty("Accept-Charset", Utils.UTF8);
			httpConnection.setDoOutput(true); // Triggers POST.
			httpConnection.setRequestMethod("PUT");
			httpConnection.setRequestProperty("Content-Type", "application/json");
			OutputStream output = httpConnection.getOutputStream();
		    output.write(tweet.getBytes(Utils.UTF8));
		    output.flush();
		    
		    int status = httpConnection.getResponseCode();
			
			if (status == HttpURLConnection.HTTP_CONFLICT) {
				++conflictCount;
				System.out.println("Update conflict.");
				System.out.println("URL: " + httpConnection.getURL());
				System.out.println("HTTP Response Message: " + httpConnection.getResponseMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void printConflictNumberAndReset(String city) {
		System.out.println("\n" + conflictCount + " conflicts in updating data from " + city);
		conflictCount = 0;
	}
}
