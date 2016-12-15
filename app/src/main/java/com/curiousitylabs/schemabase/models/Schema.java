package com.curiousitylabs.schemabase.models;

import com.curiousitylabs.schemabase.R;

/**
 * Created by anthony on 12/12/2016.
 */

public class Schema extends Model {


    public String TAG() {
        return this.getClass().getSimpleName();
    }
    public String name;

    public static int JSON_SCHEMA_RES_ID = R.raw.schema_schema;

    public Schema(){}

    public String getName() {
        return name;
    }
}
