package com.curiousitylabs.schemabase.models;

import android.util.Log;

import com.curiousitylabs.schemabase.R;
import com.curiousitylabs.schemabase.SchemabaseClient;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by anthony on 12/12/2016.
 */

public class Schema extends Model<Schema> {


    public String TAG() {
        return this.getClass().getSimpleName();
    }
    public String name;
    public String imageUrl;

    public static int JSON_SCHEMA_RES_ID = R.raw.schema_schema;

    public Schema(){}

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public SchemabaseClient.DbValueRef<Schema> write() {
        String key = getUid();
        if(key == null){
            key = FirebaseDatabase.getInstance().getReference("schemas").push().getKey();
            setUid(key);
        }

        return new SchemabaseClient.DbValueRef<>(SchemabaseClient.Verb.POST, this, Schema.class, "schemas", key);
    }

    public static Schema fromJson(String json){
        Schema obj = new Schema();

        try {
            JSONObject jo = new JSONObject(json);
            obj.name = jo.optString("name");
        } catch (JSONException e) {
            Log.e(Schema.class.getSimpleName(), e.toString());
        }

        return obj;
    }

    @Override
    public int compareTo(@NotNull  Schema model) {
        return this.getUid().compareTo(model.getUid());
    }
}
