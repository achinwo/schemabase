package com.curiousitylabs.schemabase;

import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.curiousitylabs.schemabase.models.Schema;
import com.google.firebase.auth.FirebaseAuth;

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
    private boolean mTwoPane;
    protected CancellationTokenSource schemasTS;
    protected Utils.RecyclerViewAdapter<Schema> schemaRecyclerViewAdapter;

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



        schemaRecyclerViewAdapter = new Utils.RecyclerViewAdapter<>(getAppState().schemas, new Utils.ViewAdapter<Schema>(R.layout.schema_list_content) {

            @NotNull
            @Override
            public HashMap<String, View> createViewMap(View view) {
                HashMap<String, View> mapper = new HashMap<>();
                mapper.put("textView", view.findViewById(R.id.schema_name));
                mapper.put("view", view);
                return mapper;
            }

            @Override
            public void onBindView(HashMap<String, View> viewMap, final Schema item, int position) {
                View view = viewMap.get("view");
                TextView txtView = (TextView) viewMap.get("textView");
                txtView.setText(item.getName());

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mTwoPane) {
                            Bundle arguments = new Bundle();
                            arguments.putSerializable(SchemaDetailFragment.ARG_ITEM_ID, item);
                            SchemaDetailFragment fragment = new SchemaDetailFragment();
                            fragment.setArguments(arguments);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.schema_detail_container, fragment)
                                    .commit();
                        } else {
                            Context context = v.getContext();
                            Intent intent = new Intent(context, SchemaDetailActivity.class);
                            intent.putExtra(SchemaDetailFragment.ARG_ITEM_ID, item);

                            context.startActivity(intent);
                        }
                    }
                });
            }

        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.schema_list);
        assert recyclerView != null;
        recyclerView.setAdapter(schemaRecyclerViewAdapter);

        if (findViewById(R.id.schema_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        schemasTS = getClient().getSchemas().subscribe(new SchemabaseClient.Callback<Schema>() {
            @Override
            public void onData(SchemabaseClient.DbRefEventType eventType, Schema model, String previousUid) {
                ObservableArrayList<Schema> schemas = getAppState().schemas;

                if(!schemas.contains(model)){
                    schemas.add(model);
                }

            }
        });
        schemaRecyclerViewAdapter.registerObserver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        schemasTS.cancel();
        schemaRecyclerViewAdapter.unregisterObserver();
    }

    public void showCreateTodoForm(View view){
        Intent startActivityIntent = new Intent(this, SchemaRenderedActivity.class);

        startActivityIntent.putExtra(SchemaRenderedActivity.KEY_MODEL_NAME, "Schema");


        String schema = loadJSONFromAsset(Schema.JSON_SCHEMA_RES_ID);

        //Log.d(TAG, "id:" + Venue.JSON_SCHEMA_RES_ID+ " jsonSchema:"+schema);
        startActivityIntent.putExtra(SchemaRenderedActivity.KEY_MODEL_SCHEMA, schema);


        startActivityForResult(startActivityIntent, null)
                .then(new Thenable() {

                    @Override
                    public void onOk(Intent data) {
                        String json = data.getStringExtra(SchemaRenderedActivity.KEY_RESULT_JSON);
                        Schema s = Schema.fromJson(json);
                        Log.d(TAG(), "result for create:" + s.getName() + "  isNew="+s.isNew());

                        s.write().then(new SchemabaseClient.Callback<Schema>() {
                            @Override
                            public void onData(SchemabaseClient.DbRefEventType eventType, Schema model, String previousUid) {
                                Log.d(TAG(), "saved schema:" + model.getName() + "  isNew=" + model.isNew() +" uid="+model.getUid());
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
