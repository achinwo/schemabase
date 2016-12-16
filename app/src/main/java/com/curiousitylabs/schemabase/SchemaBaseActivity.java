package com.curiousitylabs.schemabase;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by anthony on 12/12/2016.
 */

public class SchemaBaseActivity extends AppCompatActivity {


    public String TAG() {
        return this.getClass().getSimpleName();
    }

    public String loadJSONFromAsset(int asset) {
        String json;
        try {
            InputStream is = getResources().openRawResource(asset);
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }

            json = writer.toString();
        } catch (IOException ex) {
            Log.e(TAG(), ex.toString());
            return null;
        }
        return json;
    }

    private AtomicInteger lastFldId = null;

    public int generateViewId(){

        if(lastFldId == null) {
            int maxFld = 0;
            String fldName = "";
            Field[] flds = R.id.class.getDeclaredFields();
            R.id inst = new R.id();

            for (int i = 0; i < flds.length; i++) {
                Field fld = flds[i];

                try {
                    int value = fld.getInt(inst);

                    if (value > maxFld) {
                        maxFld = value;
                        fldName = fld.getName();
                    }
                } catch (IllegalAccessException e) {
                    Log.e(TAG(), "error getting value for \'"+ fld.getName() + "\' " + e.toString());
                }
            }
            Log.d(TAG(), "maxId="+maxFld +"  name="+fldName);
            lastFldId = new AtomicInteger(maxFld);
        }

        return lastFldId.addAndGet(1);
    }

    public static abstract class Thenable {

        public int reqCode;
        public Intent origIntent;
        public Bundle origOpts;
        public Thenable callback;

        public Thenable(){
        }

        public Thenable(int reqCode, Intent origIntent, Bundle origOpts){
            this.reqCode = reqCode;
            this.origIntent = origIntent;
            this.origOpts = origOpts;
        }

        public Thenable then(Thenable callback){
            this.callback = callback;
            return callback;
        }

        public void onCancelled(Intent data) {
        }

        public void onFirstUser(Intent data) {
        }

        public abstract void onOk(Intent data);
    }

    protected HashMap<Integer, Thenable> thens = new HashMap<>();

    public Thenable startActivityForResult(Intent intent){
        return startActivityForResult(intent, null);
    }

    public Thenable startActivityForResult(Intent intent, Bundle options) {
        Random rand = new Random();
        int reqCode = rand.nextInt(1000) + 1;

        while (thens.containsKey(reqCode)) {
            reqCode = rand.nextInt(1000) + 1;
        }

        Thenable callback = new Thenable(reqCode, intent, options) {
            @Override
            public void onOk(Intent data) {
                callback.onOk(data);
            }
        };

        thens.put(reqCode, callback);
        super.startActivityForResult(intent, reqCode, options);
        return callback;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG(), String.format("thens=%s, reqCode=%s, resCode=%s, ok=%s", thens, requestCode, resultCode, Activity.RESULT_OK));
        switch(resultCode){
            case Activity.RESULT_CANCELED:
                thens.remove(requestCode).onCancelled(data);
                break;
            case Activity.RESULT_FIRST_USER:
                thens.remove(requestCode).onFirstUser(data);
                break;
            case Activity.RESULT_OK:
                thens.remove(requestCode).onOk(data);
                break;
            default:
                Log.e(TAG(), "unrecognised result code: " + resultCode);
        }

    }

    protected AppState appState;

    public AppState getAppState() {
        appState = AppState.getInstance();

        if (!appState.isConfigured()) {
            Log.d(TAG(), "configuring app state singleton...");
            appState.configure(getApplication());
        }
        return appState;
    }

    private ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public SchemabaseClient getClient() {
        return getAppState().getClient();
    }
}
