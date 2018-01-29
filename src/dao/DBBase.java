package dao;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by hans on 1/29/2018.
 */
public class DBBase {

    protected MongoDatabase db;
    protected MongoClient client;
    protected Datastore datastore;

    public static String requestData = "request.json";
    public static String indexData = "index.json";
    public static String historyData = "history.json";

    protected void connect(String config) throws Exception {
        Properties properties = new Properties();
        properties.load(new BufferedInputStream(new FileInputStream(config)));
        String uri = properties.getProperty("dbconnection");
        String dbname = properties.getProperty("dbname");
        System.out.println("loaded configuration info with dbname: " + dbname + ", and conn: " + uri);

        MongoClientURI connectionURI = new MongoClientURI(uri);
        client = new MongoClient(connectionURI);
        db = client.getDatabase(dbname);
        System.out.println("Connected db : " + db.getName());

        Morphia morphia = new Morphia();
        morphia.mapPackage("dao");
        datastore = morphia.createDatastore(client, dbname);
        datastore.ensureIndexes();
    }

    protected void close() {
        if(datastore != null) {
            datastore = null;
        }

        if(db != null) {
            db = null;
        }

        if(client != null) {
            client.close();
        }
    }

}
