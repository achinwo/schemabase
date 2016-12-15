package com.curiousitylabs.schemabase.view_builders;

import android.view.View;

import com.curiousitylabs.schemabase.SchemaBaseActivity;

import org.json.JSONObject;

/**
 * Created by anthony on 13/12/2016.
 */

public class ArraySchemaViewBuilder extends SchemaViewBuilder {

    public ArraySchemaViewBuilder(SchemaBaseActivity ctx, JSONObject jsonSchema, JSONObject jsonData, String... pathFragments) {
        super(ctx, jsonSchema, jsonData, pathFragments);
    }

    @Override
    public View createView() {
        return null;
    }
}
