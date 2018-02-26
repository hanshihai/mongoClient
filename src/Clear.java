import com.hpe.mcloud.imgsvcs.model.persist.ImageHistoryJsonDao;
import com.hpe.mcloud.imgsvcs.model.persist.ImageIndexJsonDao;
import com.hpe.mcloud.imgsvcs.model.persist.ImageRequestJsonDao;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteResult;
import com.mongodb.client.MongoDatabase;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by hans on 1/9/2018.
 */
public class Clear {

    private MongoDatabase db;
    private MongoClient client;
    private Datastore datastore;

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

        Morphia morphia = new Morphia();
        morphia.mapPackage("dao");
        datastore = morphia.createDatastore(client, dbname);
        datastore.ensureIndexes();
    }

    private void close() {
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

    public void deleteImageIndexByKey(String id, String orgId) {
        WriteResult result =  datastore.delete(datastore.createQuery(ImageIndexJsonDao.class).field("id").equal(id).field("orgId").equal(orgId));
        System.out.println("Delete image index " + result.getN());
    }

    public void deleteImageRequestByKey(String id, String orgId) {
        WriteResult result =  datastore.delete(datastore.createQuery(ImageRequestJsonDao.class).field("id").equal(id).field("orgId").equal(orgId));
        System.out.println("Delete image request " + result.getN());
    }

    public void deleteImageHistoryByKey(String id, String orgId) {
        WriteResult result =  datastore.delete(datastore.createQuery(ImageHistoryJsonDao.class).field("id").equal(id).field("orgId").equal(orgId));
        System.out.println("Delete image history " + result.getN());
    }

    private void clear(String id, String orgId) {
        System.out.println("Starting delete for " + id + "/" + orgId);
        deleteImageIndexByKey(id, orgId);
        deleteImageRequestByKey(id, orgId);
        deleteImageHistoryByKey(id, orgId);
        System.out.println("End of delete for " + id + "/" + orgId);
    }

    public static void main(String[] args) throws Exception {
        String configFile = "config.properties";
        if(args.length > 0) {
            configFile = args[0];
        }

        Clear clear = new Clear();

        clear.connect(configFile);

        System.out.println("Clear is running, please input id orgId now:");
        Scanner scaner = new Scanner(System.in);
        while(scaner.hasNextLine()) {
            String input = scaner.nextLine();
            if("q".equals(input.trim()) || "quit".equals(input.trim()) || "exit".equals(input.trim())) {
                break;
            }
            try{
                String[] parameters = input.split(" ");
                if(parameters != null && parameters.length == 2) {
                    clear.clear(parameters[0], parameters[1]);
                }else{
                    System.out.println("Bad parameter. Should be \"id orgId\"");
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        clear.close();
    }
}
