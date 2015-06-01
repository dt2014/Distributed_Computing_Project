package dcp.congestionTag;

/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 * @author Fengmin Deng (Student ID: 659332)
 */

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.HttpURLConnection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * This class is responsible for creating the input file to the ELKI program 
 *   for the clustering process. It sends HTTP requests to CouchDB databases
 *   to retrieve the relevant View data for the ELKI input file.
 */
public class ElkiInputProcessor {
	
	public static void main(String[] args) {
		
		JsonObject paras = Utils.validateCmdArgs(args);
		
		buildElkiInput(paras.getAsJsonObject("brisbane"));
		buildElkiInput(paras.getAsJsonObject("melbourne"));
		buildElkiInput(paras.getAsJsonObject("sydney"));
	}

	/**
	 * This method sends HTTP request to get data from CouchDB View, then it
	 *   writes the response to a 'txt' file which will be the input data for
	 *   running ELKI clustering process.
	 * @param url
	 * @param elkiInputFileName
	 */
	private static void buildElkiInput(JsonObject city) {
		String url = city.get("url_elki_view").getAsString();
		String elkiInputFileName = city.get("file_elki_in").getAsString();
		String line = null;
		String json = null;
		BufferedReader reader = null;
		PrintWriter writer = null;
		StringBuilder sb = new StringBuilder();
		try {
			URL myURL = new URL(url);
			HttpURLConnection httpConnection = (HttpURLConnection) myURL.openConnection();
			httpConnection.setRequestProperty("Accept-Charset", Utils.UTF8);
			
			int status = httpConnection.getResponseCode();
			
			if (status != HttpURLConnection.HTTP_OK) {
				System.out.println("Something wrong when retrieving view data from CouchDB.");
				System.out.println("URL: " + httpConnection.getURL());
				System.out.println("HTTP Response Message: " + httpConnection.getResponseMessage());
			}
			
			reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream(), Utils.UTF8));

			while ((line = reader.readLine()) != null) {
				sb = sb.append(line);
			}
			json = sb.toString();

			if (json != null) {
				JsonObject jobj = Utils.toJson(json);
				JsonArray rows = jobj.getAsJsonArray("rows");

				writer = new PrintWriter(new FileOutputStream(elkiInputFileName, false));
				String elkiInput = "# time lat lng id";
				writer.println(elkiInput);

				for (int i = 0; i < rows.size(); ++i) {
					JsonObject row = (JsonObject) rows.get(i);
					elkiInput = row.get("value").getAsString() + " "
							+ row.get("id").getAsString();
					writer.println(elkiInput);
				}
			}
		} catch (Exception e) {
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (Exception e) {

			}
		}
	}
}
