import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hans
 */
public class BulkMain {

    private MongoDatabase db;
    private MongoClient client;

    private static AtomicInteger count = new AtomicInteger();

    private static List<Object> ids = new ArrayList<>();

    private void connect(String config) throws Exception {
        Properties properties = new Properties();
        properties.load(new BufferedInputStream(new FileInputStream(config)));
        String uri = properties.getProperty("dbconnection");
        String dbname = properties.getProperty("dbname");
        System.out.println("loaded configuration info with dbname: " + dbname + ", and conn: " + uri);

        MongoClientURI connectionURI = new MongoClientURI(uri);
        client = new MongoClient(connectionURI);
        db = client.getDatabase(dbname);
        System.out.println("Connected db : " + db.getName());
    }

    private void close() {
        if (db != null) {
            db = null;
        }

        if (client != null) {
            client.close();
        }
    }

    public static Block<Document> getBlock() {
        Block<Document> output = new Block<Document>() {
            @Override
            public void apply(Document document) {
                ids.add(document.get("_id"));
            }
        };
        return output;
    }

    public static Block<Document> getCountBlock() {
        Block<Document> countDoc = new Block<Document>() {

            @Override
            public void apply(Document document) {
                count.incrementAndGet();
            }
        };
        return countDoc;
    }

    public static Block<String> getStringBlock() {
        Block<String> stringBlock = new Block<String>() {

            @Override
            public void apply(String s) {
                System.out.println(s);
            }
        };
        return stringBlock;
    }


    private void queryAll(String collection) throws Exception {
        System.out.println(" ----- quering all: collection = " + collection);
        ids.clear();
        db.getCollection(collection).find(new Document()).forEach(getBlock());
        System.out.println(" ----- end queryAll -----");
    }

    private void delete(String collection, String key, String value) throws Exception {
        System.out.println(" ----- delete : collection = " + collection + "; key = " + key + "; value = " + value);
        Document doc = db.getCollection(collection).findOneAndDelete(Filters.eq(key, value));
        if (doc != null) {
            System.out.println(" ----- end delete : " + doc.toJson() + " -----");
        } else {
            System.out.println(" ----- end delete with nothing -----");
        }
    }

    public static void printUsage() {
        System.out.println("----------- usage ------------");
        System.out.println("Start: java --classpath:mongo-java-driver-3.4.0.jar BulkMain <configFile> sourceCollection targetCollection");
        System.out.println("It will delete all records in the targetCollection that id is NOT in the sourceCollection...");
        System.out.println("----------- end ------------");
    }

    public static void main(String[] args) {
        String configFile = "config.properties";
        String sourceCollection = "";
        String targetCollection = "";

        if (args.length == 3) {
            configFile = args[0];
            sourceCollection = args[1];
            targetCollection = args[2];
        }else if (args.length == 2) {
            sourceCollection = args[0];
            targetCollection = args[1];
        }else{
            printUsage();
            System.exit(1);
        }

        System.out.println("Will delete all records from " + targetCollection + " and the _id is NOT in the collection " + sourceCollection + "; Please input y to confirm!");
        System.out.println();
        Scanner scaner = new Scanner(System.in);
        while (scaner.hasNextLine()) {
            String input = scaner.nextLine();
            if ("y".equals(input.trim()) || "yes".equals(input.trim())) {
                break;
            }else{
                System.exit(0);
            }
        }

        BulkMain main = new BulkMain();
        try {
            main.connect(configFile);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }


            try {
                main.queryAll(sourceCollection);
                List<Object> keepIds = new ArrayList<>();
                ids.stream().forEach(id -> keepIds.add(id));
                System.out.println("the count of " + sourceCollection + " is " + keepIds.size());
                main.queryAll(targetCollection);
                System.out.println("the count of " + targetCollection + " is " + ids.size());
                final String target = targetCollection;
                ids.stream().forEach(id -> {
                    if(!keepIds.contains(id)) {
                        try {
                            main.delete(target, "_id", String.valueOf(id));
                        }catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        main.close();

    }
}
