package com.curiousitylabs.schemabase.view_builders;

import android.databinding.ObservableArrayList;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.curiousitylabs.schemabase.R;
import com.curiousitylabs.schemabase.SchemaBaseActivity;
import com.curiousitylabs.schemabase.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by anthony on 13/12/2016.
 */

public class ArraySchemaViewBuilder extends SchemaViewBuilder implements View.OnClickListener {


    ObservableArrayList<Item> items;
    Utils.ViewAdapter<Item> adapter;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.array_view_add_btn:
                Log.d(TAG(), "add button clicked");
                items.add(new Item("Hello world " + items.size()));
                break;
            case R.id.array_view_remove_btn:
                int pos = adapter.getSelectedPosition();

                if(pos >= 0 && pos < items.size()) {
                    Log.d(TAG(), "remove button clicked: " + pos);
                    items.remove(pos);
                    adapter.setSelectedPosition(-1);
                } else {
                    Log.d(TAG(), "remove button clicked: invalid pos " + pos +", num items=" + items.size());
                }

                break;
        }
    }

    class Item {
        public String name;

        public Item(String name){
            this.name = name;
        }
    }

    public ArraySchemaViewBuilder(SchemaBaseActivity ctx, JSONObject jsonSchema, JSONObject jsonData, String... pathFragments) {
        super(ctx, jsonSchema, jsonData, pathFragments);
    }

    @Override
    public View createView() {
        Log.d(TAG(), "JSON:" + Utils.getJsonObjectByPath(pathFragments, jsonSchema));
        final JSONObject itemSchema = Utils.getJsonObjectByPath(pathFragments, jsonSchema).optJSONObject("item");
        final String itemType = itemSchema.optString("type");

        TextView tv = new TextView(context);
        Button btn = new Button(context);
        Button removeBtn = new Button(context);
        RecyclerView lv = new RecyclerView(context);
        //LinearLayout la = new LinearLayout(context);
        RelativeLayout rel = new RelativeLayout(context);


        //la.setOrientation(LinearLayout.VERTICAL);
        //la.addView(tv);
        //la.addView(btn);

        tv.setId(context.generateViewId());
        btn.setId(R.id.array_view_add_btn);
        removeBtn.setId(R.id.array_view_remove_btn);

        btn.setOnClickListener(this);
        removeBtn.setOnClickListener(this);

        btn.setText("Add");
        removeBtn.setText("Remove");

        rel.addView(tv);
        rel.addView(btn);
        rel.addView(removeBtn);
        rel.addView(lv);

        Utils.Layout.addRule(btn, tv, RelativeLayout.RIGHT_OF);
        Utils.Layout.addRule(removeBtn, btn, RelativeLayout.RIGHT_OF);

        Utils.Layout.addRule(btn, tv, RelativeLayout.ALIGN_BASELINE);
        Utils.Layout.addRule(removeBtn, btn, RelativeLayout.ALIGN_BASELINE);

        Utils.Layout.setDimensions(btn, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        Utils.Layout.setDimensions(removeBtn, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        tv.setText("Array Section");

        //tv.setBackgroundColor(Color.WHITE);
        tv.setPadding(10, 10, 10, 10);


        Utils.Layout.addRules(tv, RelativeLayout.ALIGN_PARENT_TOP);
        Utils.Layout.addRules(tv, RelativeLayout.ALIGN_PARENT_LEFT);

        Utils.Layout.addRules(btn, RelativeLayout.ALIGN_PARENT_TOP);
        Utils.Layout.addRules(removeBtn, RelativeLayout.ALIGN_PARENT_TOP);
        Utils.Layout.addRules(removeBtn, RelativeLayout.ALIGN_PARENT_RIGHT);

        Utils.Layout.setDimensions(tv, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        Utils.Layout.setDimensions(lv, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


        Utils.Layout.addRule(lv, tv, RelativeLayout.BELOW);

        items = new ObservableArrayList<>();
        items.add(new Item("hello world"));

        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        lv.setLayoutManager(llm);

        adapter = new Utils.ViewAdapter<Item>() {

            @Override
            public View createView(ViewGroup parent, int viewType) {
                return SchemaViewBuilder.viewForSchemaType(context, itemType, itemSchema, null);
            }

            @NotNull
            @Override
            public HashMap<String, View> createViewMap(View view) {
                HashMap<String, View> map = new HashMap<>();
                map.put("view", view);
                return map;
            }

            @Override
            public void onBindView(HashMap<String, View> viewMap, Item item, final int position) {
                View tv = viewMap.get("view");
                //tv.setText(item.name);

                if(getSelectedPosition() == position){
                    // Here I am just highlighting the background
                    tv.setBackgroundColor(Color.GREEN);
                }else{
                    tv.setBackgroundColor(Color.TRANSPARENT);
                }

                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setSelectedPosition(position);
                    }
                });
            }
        };

        lv.setAdapter(new Utils.RecyclerViewAdapter<>(items, adapter));

        rel.setBackground(Utils.createBorder(null, null, null, null));


        rel.setTag(R.id.key_json_value_getter, new ValueGetter() {
            @Override
            public String jsonFromView(View myView) {
                StringBuilder js = new StringBuilder("[");
                for (int i = 0; i < items.size(); i++) {
                    Item item = items.get(i);
                    js.append('"');
                    js.append(item.name);
                    js.append('"');

                    if(i != items.size() - 1) js.append(',');
                }
                js.append(']');
                return js.toString();
            }
        });
        return rel;
    }
}
