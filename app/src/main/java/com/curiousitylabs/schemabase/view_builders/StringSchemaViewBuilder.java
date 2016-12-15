package com.curiousitylabs.schemabase.view_builders;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.curiousitylabs.schemabase.R;
import com.curiousitylabs.schemabase.SchemaBaseActivity;
import com.curiousitylabs.schemabase.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.curiousitylabs.schemabase.Utils.getJsonObjectByPath;

/**
 * Created by anthony on 12/12/2016.
 */

public class StringSchemaViewBuilder extends SchemaViewBuilder {

    public static final String TAG = StringSchemaViewBuilder.class.getSimpleName();

    public StringSchemaViewBuilder(SchemaBaseActivity ctx, JSONObject json, String... pathFragments) {
        super(ctx, json, pathFragments);
    }

    public StringSchemaViewBuilder(SchemaBaseActivity ctx, JSONObject jsonSchema, JSONObject jsonData, String...pathFragments){
        super(ctx, jsonSchema, jsonData, pathFragments);
    }

    @Override
    public View createView() {
        JSONObject json = getJsonObjectByPath(pathFragments, this.jsonSchema);
        String key = pathFragments[pathFragments.length - 1];
        Log.d(TAG, "viewForStringSchema:"+ key+ " jsonSchema:" +json.toString());

        JSONArray enumObj = json.optJSONArray("enum");
        if (enumObj == null) {

            final EditText txt = new EditText(context);
            txt.setHint(key);

            txt.setTag(R.id.key_json_value_getter, new ValueGetter() {
                @Override
                public String jsonFromView(View myView) {
                    return txt.getText().toString();
                }
            });

            String instValue = getInstanceValue();
            if(instValue != null) txt.setText(instValue);

            return txt;
        }else{
            RelativeLayout layout = new RelativeLayout(context);
            layout.setId(context.generateViewId());

            TextView label = new TextView(context);
            label.setText(key);
            label.setId(context.generateViewId());

            List<String> choices = new ArrayList<>();
            for (int i = 0; i < enumObj.length(); i++) {
                String value = enumObj.optString(i);
                choices.add(value);
            }

            final Spinner spinner = new Spinner(context);
            final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, choices);
            spinner.setAdapter(spinnerArrayAdapter);
            Log.d(TAG, "Spinner view id: " + spinner.getId());
            spinner.setId(context.generateViewId());

            layout.addView(label);
            layout.addView(spinner);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Utils.Layout.alignParent(label, RelativeLayout.ALIGN_PARENT_START);
                Utils.Layout.alignParent(spinner, RelativeLayout.ALIGN_PARENT_END);
            }else{
                Utils.Layout.alignParent(label, RelativeLayout.ALIGN_PARENT_LEFT);
                Utils.Layout.alignParent(spinner, RelativeLayout.ALIGN_PARENT_RIGHT);
            }
            Utils.Layout.alignBaseLine(label, spinner);

            layout.setTag(R.id.key_json_value_getter, new ValueGetter() {
                @Override
                public String jsonFromView(View myView) {
                    return (String) spinner.getSelectedItem();
                }
            });

            return layout;
        }

    }
}
