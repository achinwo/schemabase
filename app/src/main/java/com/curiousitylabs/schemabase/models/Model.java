package com.curiousitylabs.schemabase.models;

import com.curiousitylabs.schemabase.AppState;
import com.curiousitylabs.schemabase.SchemabaseClient;
import com.google.firebase.database.DataSnapshot;

import org.json.JSONObject;

/**
 * Created by anthony on 12/12/2016.
 */

public abstract class Model<T> {
    public String uid;
    public JSONObject schema;
    public DataSnapshot snapshot;

    public Model(){}

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public boolean isNew(){
        return uid == null;
    }

    public abstract SchemabaseClient.DbValueRef<T> write();

    protected SchemabaseClient getClient(){
        return AppState.getInstance().getClient();
    }
}
