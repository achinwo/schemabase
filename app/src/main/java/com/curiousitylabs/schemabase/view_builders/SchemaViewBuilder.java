package com.curiousitylabs.schemabase.view_builders;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.curiousitylabs.schemabase.R;
import com.curiousitylabs.schemabase.SchemaBaseActivity;
import com.curiousitylabs.schemabase.Utils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.curiousitylabs.schemabase.Utils.getJsonObjectByPath;

/**
 * Created by anthony on 12/12/2016.
 */

public abstract class SchemaViewBuilder {

    public static String TAG = SchemaViewBuilder.class.getSimpleName();

    public String TAG() {
        return this.getClass().getSimpleName();
    }

    public enum SchemaType {
        OBJECT, STRING, INTEGER, NUMBER, ARRAY, BOOLEAN;

        public static SchemaType fromString(String typeStr){
            SchemaType schemaType = null;
            if (typeStr.toLowerCase().equals("object")){
                schemaType = OBJECT;
            } else if (typeStr.toLowerCase().equals("string")) {
                schemaType = STRING;
            } else if (typeStr.toLowerCase().equals("integer")) {
                schemaType = INTEGER;
            } else if (typeStr.toLowerCase().equals("array")) {
                schemaType = ARRAY;
            } else if (typeStr.toLowerCase().equals("number")) {
                schemaType = NUMBER;
            } else if (typeStr.toLowerCase().equals("boolean")) {
                schemaType = BOOLEAN;
            }
            return schemaType;
        }
    }

    public static class Entry {
        public View view;
        public HashMap<String, Integer> idMap;

        public Entry(View view, HashMap<String, Integer> map){
            this.idMap = map;
            this.view = view;
        }
    }

    public JSONObject jsonSchema;
    public JSONObject jsonData;
    public String[] pathFragments;
    public SchemaBaseActivity context;
    public View view;

    public static abstract class ValueGetter {
        public abstract String jsonFromView(View myView);
    }

    public SchemaViewBuilder(SchemaBaseActivity ctx, JSONObject json, String...pathFragments){
        this.jsonSchema = json;
        this.pathFragments = pathFragments;
        this.context = ctx;
    }

    public SchemaViewBuilder(SchemaBaseActivity ctx, JSONObject jsonSchema, JSONObject jsonData, String...pathFragments){
        this.jsonSchema = jsonSchema;
        this.jsonData = jsonData;
        this.pathFragments = pathFragments;
        this.context = ctx;
    }

    public String getInstanceValue(){
        if(jsonData == null) return null;
        return Utils.getStringByPath(Utils.arrayOmit(pathFragments, "properties"), jsonData);
    }

    public abstract View createView();

    public static View viewForSchemaType(SchemaBaseActivity ctx, String schemaTypeString, JSONObject jsonSchema, JSONObject jsonData){
        SchemaType schemaType = SchemaType.fromString(schemaTypeString);
        assert schemaType != null;

        View view = null;
        switch (schemaType){
            case OBJECT:
                view = viewForObjectSchema(ctx, jsonSchema, jsonData);
                break;
            case STRING:
                view = new StringSchemaViewBuilder(ctx, jsonSchema, jsonData).build();
                break;
            case INTEGER:
                view = new NumberSchemaViewBuilder(ctx, jsonSchema, jsonData).build();
                break;
        }

        return view;
    }

    public static View viewForObjectSchema(SchemaBaseActivity ctx, JSONObject jsonSchema, JSONObject jsonData, String...path){
        JSONObject json = getJsonObjectByPath(path, jsonSchema);

        JSONObject properties = json.optJSONObject("properties");
        List<String> keys = Lists.newArrayList(properties.keys());

        final LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(getParentType(jsonSchema, path) == SchemaType.ARRAY ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        layout.setId(ctx.generateViewId());

        final HashMap<String, Integer> idMap = new HashMap<>();

        for(String key: keys) {
            JSONObject propJson = properties.optJSONObject(key);
            String[] pathExt = {"properties", key};
            String[] newPath = ObjectArrays.concat(path, pathExt, String.class);
            Log.d(TAG, "new path: "+ Arrays.asList(newPath) + "  value:"+ getJsonObjectByPath(newPath, jsonSchema));
            SchemaType schemaType = SchemaType.fromString(propJson.optString("type", ""));
            assert schemaType != null;

            View view = null;
            switch (schemaType) {
                case OBJECT:
                    View sep = new View(ctx);

                    layout.addView(sep);
                    Utils.Layout.setDimensions(sep, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    Utils.Layout.setMargin(sep, 0, ctx.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin), 0, 0);
                    sep.setBackgroundColor(Color.DKGRAY);

                    view = viewForObjectSchema(ctx, jsonSchema, jsonData, newPath);


                    HashMap<String, Integer> viewIdMap = (HashMap<String, Integer>) view.getTag(R.id.key_id_map);
                    if(viewIdMap != null){
                        idMap.putAll(viewIdMap);
                    }

                    break;
                case STRING:
                    view = new StringSchemaViewBuilder(ctx, jsonSchema, jsonData, newPath).build();

                    break;
                case INTEGER:
                    view = new NumberSchemaViewBuilder(ctx, jsonSchema, jsonData, newPath).build();

                    break;
                case ARRAY:
                    view = new ArraySchemaViewBuilder(ctx, jsonSchema, jsonData, newPath).build();
                    break;
            }
            idMap.put(Joiner.on(".").join(newPath), view.getId());

            //Log.d(TAG, String.format("View id for \'%s\': id=%d", Joiner.on(".").join(newPath), view.getId()));
            layout.addView(view);

            int horizMargin = schemaType == SchemaType.OBJECT ? 0 : ctx.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
            int vertMargin = schemaType == SchemaType.OBJECT ? 0 : ctx.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
            layoutParams.leftMargin = horizMargin;
            layoutParams.rightMargin = horizMargin;

            if(layout.getChildCount() == 1){
                layoutParams.topMargin = vertMargin;
            }

            view.setLayoutParams(layoutParams);
        }
        Log.d(TAG, "viewForObjectSchema: idMap=" + idMap);

        layout.setTag(R.id.key_id_map, idMap);
        return layout;
    }

    public static SchemaType getParentType(JSONObject jsonSchema, String...path) {
        if(path.length < 2) return null;

        String[] pathFrags = Arrays.copyOfRange(path, 0, path.length - 2);
        String typeName = getJsonObjectByPath(pathFrags, jsonSchema).optString("type", "");
        return SchemaType.fromString(typeName);
    }


    public View build(){
        if (view == null){
            view = createView();
        }
        assert view.getTag(R.id.key_json_value_getter) != null;

        if(view.getId() < 1) {
            int viewId = context.generateViewId();
            view.setId(viewId);
        }
        return view;
    }

}
