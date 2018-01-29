import com.hpe.mcloud.imgsvcs.model.persist.ImageHistoryJsonDao;
import com.hpe.mcloud.imgsvcs.model.persist.ImageIndexJsonDao;
import com.hpe.mcloud.imgsvcs.model.persist.ImageRequestJsonDao;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import dao.DBBase;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by hans on 1/9/2018.
 */
public class Backup extends DBBase{

    private int total = 0;

    private MongoDatabase db;
    private MongoClient client;
    private Datastore datastore;

    public void writeData(Path path, String fileName, String data) throws IOException {
        File file = path.resolve(fileName).toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))){
            writer.write(data);
            total++;
        }
    }

    private String generateTimeStamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
        return format.format(new Date());
    }

    private void writeJson(Path sequencePath, String filename, String json) {
        if(json != null) {
            try{
                writeData(sequencePath, filename, json + "\n");
            }catch(IOException ioe) {
                System.out.println("Cannot write to backup file " + sequencePath.toString());
                throw new RuntimeException(ioe);
            }
        }
    }

    private void backup() throws IOException {
        Path rootPath = Paths.get("./").resolve(generateTimeStamp());
        if(!Files.exists(rootPath)) {
            Files.createDirectories(rootPath);
        }
        System.out.println("Starting to backup mongodb data to " + rootPath.toString() + ".");
        datastore.createQuery(ImageRequestJsonDao.class).asList().forEach(item -> writeJson(rootPath, requestData, item.toJson()));
        System.out.println("Backup ImageRequestJsonDao: " + total);
        total = 0;
        datastore.createQuery(ImageIndexJsonDao.class).asList().forEach(item -> writeJson(rootPath, indexData, item.toJson()));
        System.out.println("Backup ImageIndexJsonDao: " + total);
        total = 0;
        datastore.createQuery(ImageHistoryJsonDao.class).asList().forEach(item -> writeJson(rootPath, historyData, item.toJson()));
        System.out.println("Backup ImageHistoryJsonDao: " + total);
        total = 0;
        System.out.println("Finished to backup mongodb data.");
    }

    public static void main(String[] args) {
        String configFile = "config.properties";
        if(args.length > 0) {
            configFile = args[0];
        }

        Backup export = new Backup();
        try{
            export.connect(configFile);
            export.backup();
        }catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        export.close();
    }
}
