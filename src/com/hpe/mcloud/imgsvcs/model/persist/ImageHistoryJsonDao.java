package com.hpe.mcloud.imgsvcs.model.persist;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by hans on 2017/3/27.
 */
@Entity
public class ImageHistoryJsonDao implements Cloneable {

    @Id
    private String id;

    @Property
    private String orgId;

    @Property
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String toJson() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{")
                .append("\"id\":").append("\"" + id + "\",")
                .append("\"orgId\":").append("\"" + orgId + "\",")
                .append("\"value\":").append( value ).append("}");
        return buffer.toString();
    }

    public String toExport() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(ImageHistoryJsonDao.class.getName()).append(" | ")
                .append(orgId).append(" | ")
                .append(id).append(" | ")
                .append(value);
        return buffer.toString();
    }
}
