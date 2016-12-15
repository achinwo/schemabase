package com.curiousitylabs.schemabase.view_builders;

import android.util.Log;
import android.view.View;
import android.widget.EditText;


import com.curiousitylabs.schemabase.R;
import com.curiousitylabs.schemabase.SchemaBaseActivity;

import org.json.JSONObject;

import static com.curiousitylabs.schemabase.Utils.getJsonObjectByPath;


/**
 * Created by anthony on 12/12/2016.
 */

public class NumberSchemaViewBuilder extends SchemaViewBuilder {

    public static final String TAG = NumberSchemaViewBuilder.class.getSimpleName();

    public NumberSchemaViewBuilder(SchemaBaseActivity ctx, JSONObject json, String... pathFragments) {
        super(ctx, json, pathFragments);
    }

    public NumberSchemaViewBuilder(SchemaBaseActivity ctx, JSONObject jsonSchema, JSONObject jsonData, String...pathFragments){
        super(ctx, jsonSchema, jsonData, pathFragments);
    }

    @Override
    public View createView() {
        JSONObject json = getJsonObjectByPath(pathFragments, this.jsonSchema);
        String key = pathFragments[pathFragments.length - 1];
        Log.d(TAG, "viewForIntegerSchema:"+json.toString());
        final EditText txt = new EditText(context);
        txt.setHint(key);

        txt.setTag(R.id.key_json_value_getter, new ValueGetter() {
            @Override
            public String jsonFromView(View myView) {
                return txt.getText().toString();
            }
        });
        return txt;
    }
}
