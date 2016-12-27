package com.curiousitylabs.schemabase;

import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.curiousitylabs.schemabase.models.Schema;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import bolts.CancellationTokenSource;

/**
 * An activity representing a list of Schemas. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link SchemaDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class SchemaListActivity extends SchemaBaseActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    protected CancellationTokenSource schemasTS;
    protected Utils.SchemaListAdapter<Schema> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schema_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateTodoForm(null);

            }
        });



        adapter = new Utils.SchemaListAdapter<>(this, getAppState().schemas, new Utils.ViewAdapter<Schema>(R.layout.schema_list_content) {

            @NotNull
            @Override
            public HashMap<String, View> createViewMap(View view) {
                HashMap<String, View> mapper = new HashMap<>();
                mapper.put("textView", view.findViewById(R.id.schema_name));
                mapper.put("imageView", view.findViewById(R.id.imageView));
                mapper.put("view", view);
                return mapper;
            }

            @Override
            public void onBindView(HashMap<String, View> viewMap, final Schema item, int position) {
                View view = viewMap.get("view");

                TextView txtView = (TextView) viewMap.get("textView");
                txtView.setText(item.getName());

                String imageUrl = item.getImageUrl();

                if(imageUrl != null && !imageUrl.startsWith("https")){
                    ImageView imageView = (ImageView) viewMap.get("imageView");
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference(imageUrl);

                    Glide.with(SchemaListActivity.this)
                            .using(getAppState().mFbImageLoader)
                            .load(storageReference)
                            .into(imageView);
                }


                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            Context context = v.getContext();
                            Intent intent = new Intent(context, SchemaDetailActivity.class);
                            intent.putExtra(SchemaDetailFragment.ARG_ITEM_ID, item);

                            context.startActivity(intent);
                    }
                });

                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return false;
                    }
                });
                //view.requestLayout();
            }

        });

        GridView gridView = (GridView) findViewById(R.id.schema_grid_view);
        assert gridView != null;
        gridView.setAdapter(adapter);

        registerForContextMenu(gridView);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        String[] names = {"1", "2", "3", "4", "5", "6"};

        switch(item.getItemId()) {

            case R.id.editItem:
                Toast.makeText(this, "You have chosen the " + "edit" +
                                " context menu option for " + names[(int)info.id],
                        Toast.LENGTH_SHORT).show();
                return true;

            case R.id.saveItem:
                Toast.makeText(this, "You have chosen the save" +
                                " context menu option for " + names[(int)info.id],
                        Toast.LENGTH_SHORT).show();
                return true;

            case R.id.deleteItem:
                adapter.remove(adapter.getItem(info.position));


                Toast.makeText(this, "You have chosen the delete" +
                                " context menu option for " + names[(int)info.id],
                        Toast.LENGTH_SHORT).show();

                return true;

            case R.id.viewItem:

                Toast.makeText(this, "You have chosen the view" +
                                " context menu option for " + names[(int)info.id],
                        Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        schemasTS = getClient().getSchemas().subscribe(new SchemabaseClient.Callback<Schema>() {
            @Override
            public void onData(SchemabaseClient.DbRefEventType eventType, Schema model, String previousUid) {
                ObservableArrayList<Schema> schemas = getAppState().schemas;
                switch(eventType){
                    case CHILD_ADDED:
                        if(!schemas.contains(model)){
                            schemas.add(model);
                        }
                        break;
                    case CHILD_REMOVED:

                        Log.d(TAG(), "removed: " + schemas.remove(model));
                        break;

                }


            }
        });
        adapter.registerObserver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        schemasTS.cancel();
        adapter.unregisterObserver();
    }

    public void showCreateTodoForm(View view){
        Intent startActivityIntent = new Intent(this, SchemaRenderedActivity.class);

        startActivityIntent.putExtra(SchemaRenderedActivity.KEY_MODEL_NAME, "Schema");
        startActivityIntent.putExtra(SchemaRenderedActivity.KEY_STYLE_OPTIONS, "{\"background\":\"#FFFFFF\"}");
        startActivityIntent.putExtra(SchemaRenderedActivity.KEY_SAVE_PREVIEW, true);


        String schema = loadJSONFromAsset(Schema.JSON_SCHEMA_RES_ID);

        //Log.d(TAG, "id:" + Venue.JSON_SCHEMA_RES_ID+ " jsonSchema:"+schema);
        startActivityIntent.putExtra(SchemaRenderedActivity.KEY_MODEL_SCHEMA, schema);


        startActivityForResult(startActivityIntent, null)
                .then(new Thenable() {

                    @Override
                    public void onOk(Intent data) {
                        String json = data.getStringExtra(SchemaRenderedActivity.KEY_RESULT_JSON);
                        String imageFileName = data.getStringExtra(SchemaRenderedActivity.KEY_RESULT_PREVIEW_IMAGE);
                        final Schema newSchema = Schema.fromJson(json);
                        Log.d(TAG(), "result for create:" + newSchema.getName() + "  isNew="+newSchema.isNew() + " imageFileName=" + imageFileName);
                        showProgressDialog(String.format("Saving schema \"%s\"", newSchema.getName()));
                        mProgressDialog.setProgress(0);

                        getClient()
                                .uploadFile(imageFileName, "images")
                                .then(new DoneCallback<String>() {
                                    @Override
                                    public void onDone(String result) {
                                        newSchema.imageUrl = result;
                                    }
                                })
                                .progress(new ProgressCallback<Double>() {
                                    @Override
                                    public void onProgress(Double progress) {
                                        mProgressDialog.setProgress(progress.intValue());
                                    }
                                })
                                .fail(new FailCallback<Exception>() {
                                    @Override
                                    public void onFail(Exception error) {
                                        Log.e(TAG(), "image upload failed!", error);
                                    }
                                })
                                .always(new AlwaysCallback<String, Exception>() {
                                    @Override
                                    public void onAlways(Promise.State state, String resolved, Exception rejected) {
                                        hideProgressDialog();

//                                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                adapter.notifyDataSetChanged();

                                        newSchema.write()
                                                .then(new SchemabaseClient.Callback<Schema>() {
                                            @Override
                                            public void onData(SchemabaseClient.DbRefEventType eventType, Schema model, String previousUid) {
                                                Log.d(TAG(), "saved schema:" + model.getName() + "  isNew=" + model.isNew() +" uid="+model.getUid()+" image="+model.getImageUrl());




                                            }
                                        });
                                            //}
                                              //  }, 1000);
                                    }
                                });

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void doTest(){
//        getClient().getUserById("ld80mycm75M1me7HnZfs3BKUKFw2").then(new SchemabaseClient.Callback<User>() {
//            @Override
//            public void onData(SchemabaseClient.DbRefEventType eventType, User model, String previousUid) {
//                Log.d(TAG(), String.format("[%s]: %s", eventType, model.email));
//            }
//
//            @Override
//            public void onError(DatabaseError error) {
//                super.onError(error);
//            }
//        });
        CancellationTokenSource subTokenSource = getClient()
                .getSchemas()
                .subscribe(new SchemabaseClient.Callback<Schema>() {
                    @Override
                    public void onData(SchemabaseClient.DbRefEventType eventType, Schema schema, String previousUid) {
                        Log.d(TAG(), String.format("[%s]: first=%s", eventType, schema.getName()));
                    }
                });

        subTokenSource.cancelAfter(3000);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id){
            case R.id.action_test:
                doTest();
                return true;
            case R.id.action_sign_out:
                FirebaseAuth.getInstance().signOut();


                Intent signIn = new Intent(this, SignInActivity.class);
                signIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(signIn);
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


}
