package dcp.congestionTag;

/**
 * COMP90019 Distributed Computing Project, Semester 1 2015
 * @author Fengmin Deng (Student ID: 659332)
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/*
 * This class has methods that are shared by both ElkiInputProcessor and
 *   ClusterLabelMaker.
 */
public class Utils {

    public static final String UTF8 = StandardCharsets.UTF_8.name();
    
    /**
     * This method validates the command line argument as which the file name 
     *   'paras.json' must be provided. A JsonObject with the parameters 
     *   defined in the file 'paras.json' would be returned if file is provided
     *   and the Json format is fine.
     * @param args
     * @return 
     */
    public static JsonObject validateCmdArgs(String[] args) {
        JsonObject paras = null;
        
        if (args.length == 0) {
            System.out.println("Missing command arguments. 'paras.json' should be included in command line.");
            System.exit(0);
        } else {
            String paraFileName = args[0];
            try {
                String file = readFile(paraFileName, UTF8);
                if (file != null) {
                    paras = toJson(file);
//                    System.out.println(paras.toString());
                }
            } catch (NoSuchFileException e) {
                System.out.println("The file " + paraFileName + " does not exist.");
                System.exit(1);
            } catch (JsonSyntaxException e) {
                System.out.println("Not in valid Json format.");
                e.printStackTrace();
                System.exit(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return paras;
    }

    public static JsonObject toJson(String str) {
        Gson gson = new Gson();
        JsonObject jobj = gson.fromJson(str, JsonObject.class);
        return jobj;
    }
    
    // read file to String
    public static String readFile(String path, String encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
