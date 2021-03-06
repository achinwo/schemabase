package com.curiousitylabs.schemabase;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.curiousitylabs.schemabase.view_builders.SchemaViewBuilder;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.curiousitylabs.schemabase.Utils.setJsonObjectByPath;



public class SchemaRenderedActivity extends SchemaBaseActivity {

    public static final String KEY_RESULT_JSON = "result_json";
    public static final String KEY_MODEL_NAME = "model_name";
    public static final String KEY_MODEL_SCHEMA = "model_schema";
    public static final String KEY_STYLE_OPTIONS = "style_options";
    public static final int KEY_VALUE_TAG = R.id.key_json_value_getter;
    public static final String KEY_MODEL_DATA = "instance_data";
    public static final String KEY_SAVE_PREVIEW = "save_preview";
    public static final String KEY_RESULT_PREVIEW_IMAGE = "result_image_file_name";

    private JSONObject jsonSchema;
    private JSONObject jsonData;
    private JSONObject jsonStyleOpts;
    public CoordinatorLayout mainView;
    public LinearLayout bodyContainer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String modelName = getIntent().getStringExtra(KEY_MODEL_NAME);
        setTitle(modelName);

        String jsonString = getIntent().getStringExtra(KEY_MODEL_SCHEMA);
        String jsonDataString = getIntent().getStringExtra(KEY_MODEL_DATA);
        String jsonStyleString = getIntent().getStringExtra(KEY_STYLE_OPTIONS);

        setContentView(R.layout.activity_schema_rendered);

        mainView = (CoordinatorLayout) findViewById(R.id.main_view);
        bodyContainer = (LinearLayout) findViewById(R.id.content);

        try {
            if (jsonDataString != null) jsonData = new JSONObject(jsonDataString);

            if (jsonStyleString != null) {
                jsonStyleOpts = new JSONObject(jsonStyleString);
            }else{
                jsonStyleOpts = new JSONObject();
            }

            jsonSchema = new JSONObject(jsonString);
            Log.d(TAG(), "JSON: " + jsonString);

            View view = SchemaViewBuilder.viewForSchemaType(this, jsonSchema.optString("type", ""), jsonSchema, jsonData);


            HashMap<String, Integer> viewIdMap = (HashMap<String, Integer>) view.getTag(R.id.key_id_map);
            if(viewIdMap != null){
                idMap = viewIdMap;
            }

            bodyContainer.addView(view);

            String color = jsonStyleOpts.optString("background");
            if(color != null){
                bodyContainer.setBackgroundColor(Color.parseColor(color));
            }

        } catch (Exception e) {
            Log.e(TAG(), "Error building schema view", e);
        }

        bodyContainer.requestLayout();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    protected HashMap<String, Integer> idMap = new HashMap<>();

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("rendered_schema_images", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, String.format("%s.png", UUID.randomUUID().toString()));

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            Log.e(TAG(), e.toString());
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                Log.e(TAG(), e.toString());
            }
        }
        return mypath.getAbsolutePath();
    }


    public void onSaveTodo(View view){

        JSONObject result = new JSONObject();

        for (Map.Entry<String, Integer> entry: idMap.entrySet()) {
            View fldView = findViewById(entry.getValue());


            SchemaViewBuilder.ValueGetter getter = (SchemaViewBuilder.ValueGetter) fldView.getTag(KEY_VALUE_TAG);
            Log.d(TAG(), String.format("onSaveTodo: view=%s, entry=%s, getter=%s", fldView, entry, getter));

            if (getter != null) setJsonObjectByPath(Utils.arrayOmit(entry.getKey().split("\\."), "properties"), result, getter.jsonFromView(fldView));

        }

        Log.d(TAG(), "result:" + result.toString());

        Intent resultIntent  = new Intent().putExtra(KEY_RESULT_JSON, result.toString());

        if(getIntent().getBooleanExtra(KEY_SAVE_PREVIEW, false)){
            Bitmap image = Utils.getBitmapFromView(bodyContainer);
            String imageFileName = saveToInternalStorage(image);
            Log.d(TAG(), "saved preview image: " + imageFileName);
            resultIntent.putExtra(KEY_RESULT_PREVIEW_IMAGE, imageFileName);
        }

        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

}
