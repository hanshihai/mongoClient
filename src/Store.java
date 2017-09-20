import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dao.*;
/**
 * Created by hans on 2017/9/20.
 */
public class Store {

    public final static String DEFAULT_STAGE_PATTERN = "(\\D*)([^\\]]*)([^-]*)([-]{1})(.*)using\\s(\\d*)";

    private MongoDatabase db;
    private MongoClient client;
    private Datastore datastore;

    private String pattern;
    private String timeFormat;
    private SimpleDateFormat format;

    private void connect(String config) throws Exception {
        Properties properties = new Properties();
        properties.load(new BufferedInputStream(new FileInputStream(config)));
        String uri = properties.getProperty("dbconnection");
        String dbname = properties.getProperty("dbname");
        System.out.println("loaded configuration info with dbname: " + dbname + ", and conn: " + uri);

        pattern = properties.getProperty("pattern");
        System.out.println("loaded configuration info with pattern: " + pattern);
        if(pattern == null || pattern.trim().isEmpty()) {
            pattern = DEFAULT_STAGE_PATTERN;
        }
        timeFormat = properties.getProperty("timeformat");
        System.out.println("loaded configuration info with time format: " + timeFormat);

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

    public Date getTimestampDate(String t) throws ParseException {
        if(format == null) {
            format = new SimpleDateFormat(timeFormat);
        }
        return format.parse(t);
    }

    public LogBean parse(String s) throws RuntimeException {
        Pattern regrex = Pattern.compile(pattern);
        Matcher matcher = regrex.matcher(s);
        if(matcher.find() && matcher.groupCount() == 6) {
            LogBean bean = new LogBean();
            bean.setTimestamp(matcher.group(2));
            bean.setKey(matcher.group(5).trim());
            bean.setDuration(Long.parseLong(matcher.group(6)));
            return bean;
        }
        return null;
    }

    public void save(LogBean bean) {
        datastore.save(bean);
    }

    public static void main(String[] args) {
        String configFile = "config.properties";
        if(args.length > 0) {
            configFile = args[0];
        }

        Store store = new Store();
        try{
            store.connect(configFile);
        }catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        AtomicInteger count = new AtomicInteger();
        Scanner scaner = new Scanner(System.in);
        while(scaner.hasNextLine()) {
            String input = scaner.nextLine();
            if("q".equals(input.trim()) || "quit".equals(input.trim()) || "exit".equals(input.trim())) {
                break;
            }
            try{
                LogBean bean = store.parse(input);
                System.out.println(bean.toString());
                store.save(bean);
                count.addAndGet(1);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Total save: " + count.get());
        store.close();
    }
}
