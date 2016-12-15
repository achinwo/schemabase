package com.curiousitylabs.schemabase.models;

import com.google.firebase.database.DataSnapshot;

import org.json.JSONObject;

/**
 * Created by anthony on 12/12/2016.
 */

public abstract class Model {
    public String uid;
    public JSONObject schema;
    public DataSnapshot snapshot;

    public Model(){

    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
