package com.curiousitylabs.schemabase.models;

import com.curiousitylabs.schemabase.R;
import com.curiousitylabs.schemabase.SchemabaseClient;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by anthony on 12/12/2016.
 */

public class Schema extends Model<Schema> {


    public String TAG() {
        return this.getClass().getSimpleName();
    }
    public String name;

    public static int JSON_SCHEMA_RES_ID = R.raw.schema_schema;

    public Schema(){}

    @Override
    public SchemabaseClient.DbValueRef<Schema> write() {
        String key = getUid();
        if(key == null){
            key = FirebaseDatabase.getInstance().getReference("schemas").getKey();
        }

        return new SchemabaseClient.DbValueRef<>(SchemabaseClient.Verb.POST, this, Schema.class, "schemas", key);
    }

    public String getName() {
        return name;
    }
}
