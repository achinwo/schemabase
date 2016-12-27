package com.curiousitylabs.schemabase;

import android.content.Context;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by anthony on 12/12/2016.
 */

public class Utils {

    public static final String TAG = Utils.class.getSimpleName();

    public static JSONObject getJsonObjectByPath(String[] paths, JSONObject json){
        JSONObject jsonRet = json;

        for (String pathFrag : paths) {
            if(pathFrag.equals(".")) continue;

            if (pathFrag.endsWith("]")){
                String[] keys = pathFrag.split("\\[");
                String idxStr = keys[1].substring(0, keys[1].length() - 2);
                int index = Integer.getInteger(idxStr);
                jsonRet = jsonRet.optJSONArray(keys[0]).optJSONObject(index);
            } else {
                jsonRet = jsonRet.optJSONObject(pathFrag);
            }
        }
        return jsonRet;
    }

    public static String getStringByPath(String[] paths, JSONObject json){
        if(paths.length == 0) return null;

        if(paths.length == 1) {
            return json.optString(paths[0]);
        }else{
            JSONObject obj = getJsonObjectByPath(Arrays.copyOfRange(paths, 0, paths.length - 2), json);
            return obj.optString(paths[paths.length - 1]);
        }
    }

    public static JSONObject setJsonObjectByPath(String[] paths, JSONObject container, String json){
        JSONObject jsonRet = container;

        for (int i = 0; i < paths.length; i++) {
            String pathFrag = paths[i];

            if(i == paths.length - 1) {
                try {
                    jsonRet.put(pathFrag, json);
                } catch (JSONException e) {
                    Log.e(TAG, String.format("error putting %s of %s", pathFrag, Arrays.asList(paths)), e);
                }
            }
            else if (pathFrag.endsWith("]")){
                Log.e(TAG, "array setter not supported atm!!");
            } else {
                JSONObject nextJsonObj = jsonRet.optJSONObject(pathFrag);

                if(nextJsonObj == null){
                    try {
                        nextJsonObj = new JSONObject();
                        jsonRet.put(pathFrag, new JSONObject());
                    } catch (JSONException e) {
                        Log.e(TAG, String.format("error putting %s of %s", pathFrag, Arrays.asList(paths)), e);
                    }
                }
                jsonRet = nextJsonObj;
            }
        }
        return container;
    }

    public static String[] arrayOmit(String[] arr, final String valueToOmit){
        Collection<String> filtered = Collections2.filter(Arrays.asList(arr), new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return !s.equals(valueToOmit);
            }
        });
        return filtered.toArray(new String[filtered.size()]);
    }

    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    public static ShapeDrawable createBorder(Shape shape, Float strokeWidth, @ColorInt Integer color, Paint.Style style){
        ShapeDrawable shapedrawable = new ShapeDrawable();
        shapedrawable.setShape(shape == null ? new RectShape() : shape);
        shapedrawable.getPaint().setColor(color == null ? Color.RED : color);
        shapedrawable.getPaint().setStrokeWidth(strokeWidth == null ? 10f : strokeWidth);
        shapedrawable.getPaint().setStyle(style == null ? Paint.Style.STROKE : style);
        return shapedrawable;
    }

    public static class Layout {

        public static void alignBaseLine(View label, View spinner) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) label.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_BASELINE, spinner.getId());
            label.setLayoutParams(params);
        }

        public static void addRule(View view1, View view2, Integer rule){
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view1.getLayoutParams();
            params.addRule(rule, view2.getId());
            view1.setLayoutParams(params);
        }

        public static void addRules(View view, Integer...rules) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();

            for (Integer rule :
                    rules) {
                params.addRule(rule);
            }
            view.setLayoutParams(params);
        }

        public static void setMargin(View view, Integer left, Integer top, Integer right, Integer bottom){
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            params.leftMargin = left == null ? params.leftMargin : left;
            params.topMargin = top == null ? params.topMargin : top;
            params.rightMargin = right == null ? params.rightMargin : right;
            params.bottomMargin = bottom == null ? params.bottomMargin : bottom;
            view.setLayoutParams(params);
        }

        public static void setMargin(View view, Integer all){
            setMargin(view, all, all, all, all);
        }

        public static void setDimensions(View view, int width, int height){
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = height;
            params.width = width;
            view.setLayoutParams(params);
        }
    }

    public abstract static class ViewAdapter<T> {

        public int layoutResId = -1;
        public int selectedPosition = -1;
        public RecyclerView.Adapter rvBaseAdapter;

        public ViewAdapter(int layoutResId){
            this.layoutResId = layoutResId;
        }

        public ViewAdapter(){
        }

        public void setSelectedPosition(int selectedPosition) {
            if(rvBaseAdapter == null){
                this.selectedPosition = selectedPosition;

            }else{
                if(this.selectedPosition >= 0) rvBaseAdapter.notifyItemChanged(this.selectedPosition);
                this.selectedPosition = selectedPosition;
                if(selectedPosition >= 0) rvBaseAdapter.notifyItemChanged(selectedPosition);
            }
        }

        public int getSelectedPosition() {
            return selectedPosition;
        }

        public View createView(ViewGroup parent, int viewType){
            return LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
        }

        @NotNull
        public abstract HashMap<String, View> createViewMap(View view);
        public abstract void onBindView(HashMap<String, View> viewMap, T item, int position);

    }

    public static class SchemaListAdapter<T> extends ArrayAdapter<T> {

        public ObservableList.OnListChangedCallback<ObservableArrayList<T>> callback = new ObservableList.OnListChangedCallback<ObservableArrayList<T>>(){
            @Override
            public void onChanged(ObservableArrayList<T> item) {
                SchemaListAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(ObservableArrayList<T> item, int positionStart, int itemCount) {
                SchemaListAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeInserted(ObservableArrayList<T> item, int positionStart, int itemCount) {
                SchemaListAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeMoved(ObservableArrayList<T> item, int fromPosition, int toPosition, int itemCount) {
                SchemaListAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeRemoved(ObservableArrayList<T> item, int positionStart, int itemCount) {
                SchemaListAdapter.this.notifyDataSetChanged();
            }
        };

        private ObservableArrayList<T> mValues;
        public ViewAdapter<T> viewAdapter;

        public SchemaListAdapter(Context context, ObservableArrayList<T> items, ViewAdapter<T> viewAdapter) {
            super(context, -1, items);
            this.mValues = items;
            this.viewAdapter = viewAdapter;
        }


        public void registerObserver(){
            mValues.addOnListChangedCallback(callback);
        }

        public void unregisterObserver(){
            mValues.removeOnListChangedCallback(callback);
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            RecyclerViewAdapter.ViewHolder<T> holder;
            if(row == null){
                row = viewAdapter.createView(parent, -1);
                HashMap<String, View> viewMap = viewAdapter.createViewMap(row);
                holder = new RecyclerViewAdapter.ViewHolder<>(row, viewMap);
                row.setTag(R.id.key_view_holder, holder);
            }else{
                holder = (RecyclerViewAdapter.ViewHolder<T>) row.getTag(R.id.key_view_holder);
            }
            viewAdapter.onBindView(holder.viewMap, mValues.get(position), position);
            return row;
        }
    }

    public static class RecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder<T>> {

        private ObservableArrayList<T> mValues;
        public ViewAdapter<T> viewAdapter;

        public ObservableList.OnListChangedCallback<ObservableArrayList<T>> callback = new ObservableList.OnListChangedCallback<ObservableArrayList<T>>(){
            @Override
            public void onChanged(ObservableArrayList<T> item) {
                RecyclerViewAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(ObservableArrayList<T> item, int positionStart, int itemCount) {
                RecyclerViewAdapter.this.notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(ObservableArrayList<T> item, int positionStart, int itemCount) {
                RecyclerViewAdapter.this.notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(ObservableArrayList<T> item, int fromPosition, int toPosition, int itemCount) {
                RecyclerViewAdapter.this.notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onItemRangeRemoved(ObservableArrayList<T> item, int positionStart, int itemCount) {
                RecyclerViewAdapter.this.notifyItemRangeRemoved(positionStart, itemCount);
            }
        };

        public RecyclerViewAdapter(ObservableArrayList<T> items, ViewAdapter<T> viewAdapter) {
            mValues = items;
            this.viewAdapter = viewAdapter;
            this.viewAdapter.rvBaseAdapter = this;
            registerObserver();
        }

        public void registerObserver(){
            mValues.addOnListChangedCallback(callback);
        }

        public void unregisterObserver(){
            mValues.removeOnListChangedCallback(callback);
        }

        @Override
        public ViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = viewAdapter.createView(parent, viewType);
            HashMap<String, View> viewMap = viewAdapter.createViewMap(view);
            return new ViewHolder<>(view, viewMap);
        }

        @Override
        public void onBindViewHolder(final ViewHolder<T> holder, int position) {
            holder.mItem = mValues.get(position);

            viewAdapter.onBindView(holder.viewMap, holder.mItem, position);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public static class ViewHolder<IT> extends RecyclerView.ViewHolder {
            public IT mItem;
            public HashMap<String, View> viewMap;

            public ViewHolder(View view, HashMap<String, View> viewMap) {
                super(view);
                this.viewMap = viewMap;
            }

            @Override
            public String toString() {
                return super.toString() + " " + viewMap.toString();
            }
        }
    }
}
