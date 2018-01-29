import com.hpe.mcloud.imgsvcs.model.persist.ImageHistoryJsonDao;
import com.hpe.mcloud.imgsvcs.model.persist.ImageIndexJsonDao;
import com.hpe.mcloud.imgsvcs.model.persist.ImageRequestJsonDao;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import dao.DBBase;
import org.json.JSONException;
import org.json.JSONObject;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.io.*;
import java.util.List;
import java.util.Properties;

/**
 * Created by hans on 1/9/2018.
 */
public class DBStore extends DBBase{

    private class JsonBean {
        private String id;
        private String orgId;
        private String value;

        public JsonBean(String id, String orgId, String value) {
            this.id = id;
            this.orgId = orgId;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getOrgId() {
            return orgId;
        }

        public void setOrgId(String orgId) {
            this.orgId = orgId;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
    protected static String ORGID = "orgId";
    protected static String ID = "id";
    protected static String VALUE = "value";

    private String path;

    public DBStore(String p) {
        path = p;
    }

    private void storeIndexJsonDao(JsonBean bean) {
        if(bean != null && bean.getId() != null && bean.getOrgId() != null && bean.getValue() != null) {
            ImageIndexJsonDao jsonDao = new ImageIndexJsonDao();
            jsonDao.setId(bean.getId());
            jsonDao.setOrgId(bean.getOrgId());
            jsonDao.setValue(bean.getValue());
            super.datastore.save(jsonDao);
        }
    }
    private void storeRequestJsonDao(JsonBean bean) {
        if(bean != null && bean.getId() != null && bean.getOrgId() != null && bean.getValue() != null) {
            ImageRequestJsonDao jsonDao = new ImageRequestJsonDao();
            jsonDao.setId(bean.getId());
            jsonDao.setOrgId(bean.getOrgId());
            jsonDao.setValue(bean.getValue());
            super.datastore.save(jsonDao);
        }
    }
    private void storeHistoryJsonDao(JsonBean bean) {
        if(bean != null && bean.getId() != null && bean.getOrgId() != null && bean.getValue() != null) {
            ImageHistoryJsonDao jsonDao = new ImageHistoryJsonDao();
            jsonDao.setId(bean.getId());
            jsonDao.setOrgId(bean.getOrgId());
            jsonDao.setValue(bean.getValue());
            super.datastore.save(jsonDao);
        }
    }

    private void load(String filename) throws IOException {
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(path + "/" + filename));
            if(reader != null && reader.ready()) {
                reader.lines().forEach(line -> {
                    if(line != null && !line.isEmpty()) {
                        try {
                            JSONObject object = new JSONObject(line);
                            String orgId = object.getString(ORGID);
                            String id = object.getString(ID);
                            String value = object.getString(VALUE);
                            JsonBean bean = new JsonBean(id, orgId, value);
                            if(indexData.equals(filename)) {
                                storeIndexJsonDao(bean);
                            }
                            if(requestData.equals(filename)) {
                                storeRequestJsonDao(bean);
                            }
                            if(historyData.equals(filename)) {
                                storeHistoryJsonDao(bean);
                            }
                        } catch (JSONException e) {
                            System.out.println("cannot parse json data: " + line);
                            e.printStackTrace();
                        }
                    }
                });
            }
            reader.close();
        }catch(IOException ioe){
            System.out.println("exception throws when loading data from file: " + filename);
            ioe.printStackTrace();
        }finally{
            if(reader != null) {
                reader.close();
            }
        }
    }

    private void store() throws IOException {
        System.out.println("Starting to store data to mongodb from " + path);
        load(indexData);
        System.out.println("Finish to store index data.");
        load(requestData);
        System.out.println("Finish to store request data.");
        load(historyData);
        System.out.println("Finish to store history data.");
        System.out.println("Finish to store data to mongodb.");
    }

    public static void main(String[] args) {
        String configFile = "config.properties";
        if(args.length > 1) {
            configFile = args[1];
        }

        DBStore store = new DBStore(args[0]);
        try{
            store.connect(configFile);
            store.store();
        }catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        store.close();
    }
}
