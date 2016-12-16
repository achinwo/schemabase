package com.curiousitylabs.schemabase.models;

import com.curiousitylabs.schemabase.AppState;
import com.curiousitylabs.schemabase.SchemabaseClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by anthony on 12/12/2016.
 */

public abstract class Model<T> implements Serializable, Comparable<T> {

    private static final String TAG = Model.class.getSimpleName();

    public String uid;
    public JSONObject schema;
    public DataSnapshot snapshot;

    public Model(){}

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Exclude
    public String getUid() {
        return uid;
    }

    @Exclude
    public boolean isNew(){
        return getUid() == null;
    }

    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

    public static Gson getGson() {
        return gson;
    }

    public abstract SchemabaseClient.DbValueRef<T> write();

    protected SchemabaseClient getClient(){
        return AppState.getInstance().getClient();
    }

    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object != null && getUid() != null && object.getClass().equals(this.getClass())) {
            sameSame = this.getUid().equals(getClass().cast(object).getUid());
        }

        return sameSame;
    }

    @Override
    public String toString() {
        return String.format("%s: id=%s", getClass().getSimpleName(), getUid());
    }
}
