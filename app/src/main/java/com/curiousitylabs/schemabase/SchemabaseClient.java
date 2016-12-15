package com.curiousitylabs.schemabase;


import android.util.Log;

import com.curiousitylabs.schemabase.models.Schema;
import com.curiousitylabs.schemabase.models.User;
import com.google.common.base.Joiner;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import bolts.CancellationTokenSource;

/**
 * Created by anthony on 13/12/2016.
 */

public class SchemabaseClient {

    public String TAG() {
        return this.getClass().getSimpleName();
    }

    public DbValueRef<User> getUserById(String uid){
        return new DbValueRef<>(DbRef.Verb.GET, User.class, "users", uid);
    }

    public DbChildRef<Schema> getSchemas(){
        return new DbChildRef<>(DbRef.Verb.GET, Schema.class, "schemas");
    }

    public DbValueRef<Schema> getSchemaById(String id){
        return new DbValueRef<>(DbRef.Verb.GET, Schema.class, "schemas", id);
    }


    // Helper Classes

    enum DbRefEventType {
        CHILD_ADDED, CHILD_REMOVED, CHILD_CHANGED, CHILD_MOVED, DATA_CHANGE, CANCELLED
    }

    public static abstract class Callback<T> {

        public static String TAG = Callback.class.getSimpleName();


        public abstract void onData(DbRefEventType eventType, T model, String previousUid);

        public void onError(DatabaseError error) {
            Log.e(TAG, error.toString());
        }

        public void processSnapshot(DbRefEventType eventType, Class<T> clazz, DataSnapshot dataSnapshot, String previousChildName){
            T model = dataSnapshotToModel(dataSnapshot, clazz);
            onData(eventType, model, previousChildName);
        }

        public T dataSnapshotToModel(final DataSnapshot dataSnapshot, final Class<T> clazz){
            try {
                return dataSnapshot.getValue(clazz);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }
    }

    public abstract static class DbRef<T> {

        public String TAG() {
            return this.getClass().getSimpleName();
        }

        enum Verb {
            GET, POST, PUT
        }

        String[] pathFragments;
        Callback<T> callback;
        DatabaseReference ref;
        Verb verb;
        Class<T> clazz;

        public DbRef(Verb verb, Class<T> clazz, String... pathFragments) {
            this.verb = verb;
            this.pathFragments = pathFragments;
            this.clazz = clazz;
        }

        public DatabaseReference getRef() {
            if (ref == null) {
                this.ref = FirebaseDatabase.getInstance().getReference().child(getPath());
            }
            return ref;
        }

        public String getPath() {
            return Joiner.on('/').join(pathFragments);
        }

        public void then(final Callback<T> callback) {
            this.callback = callback;

            final DatabaseReference reference = getRef();
            final ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    callback.processSnapshot(DbRefEventType.DATA_CHANGE, clazz, dataSnapshot, null);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onError(databaseError);
                }
            };

            reference.addListenerForSingleValueEvent(listener);
        }

        public abstract CancellationTokenSource subscribe(final Callback<T> callback);
    }

    public static class DbChildRef<T> extends DbRef<T> {

        public DbChildRef(Verb verb, Class<T> clazz, String... pathFragments) {
            super(verb, clazz, pathFragments);
        }

        public CancellationTokenSource subscribe(final Callback<T> callback) {
            final DatabaseReference reference = getRef();
            final ChildEventListener listener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    callback.processSnapshot(DbRefEventType.CHILD_ADDED, clazz, dataSnapshot, previousChildName);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    callback.processSnapshot(DbRefEventType.CHILD_CHANGED, clazz, dataSnapshot, previousChildName);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    callback.processSnapshot(DbRefEventType.CHILD_REMOVED, clazz, dataSnapshot, null);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    callback.processSnapshot(DbRefEventType.CHILD_MOVED, clazz, dataSnapshot, previousChildName);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onError(databaseError);
                }
            };

            reference.addChildEventListener(listener);

            CancellationTokenSource tokenSource = new CancellationTokenSource();

            tokenSource.getToken().register(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG(), String.format("removed subscription: ref=%s, listener=%s", reference.getKey(), listener));
                    reference.removeEventListener(listener);
                }
            });

            return tokenSource;
        }

    }

    public static class DbValueRef<T> extends DbRef<T> {

        public DbValueRef(Verb verb, Class<T> clazz, String... pathFragments) {
            super(verb, clazz, pathFragments);
        }

        public CancellationTokenSource subscribe(final Callback<T> callback){
            final DatabaseReference reference = getRef();
            final ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    callback.processSnapshot(DbRefEventType.DATA_CHANGE, clazz, dataSnapshot, null);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    callback.onError(databaseError);
                }
            };

            reference.addValueEventListener(listener);

            CancellationTokenSource tokenSource = new CancellationTokenSource();

            tokenSource.getToken().register(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG(), String.format("removed subscription: ref=%s, listener=%s", reference.getKey(), listener));
                    reference.removeEventListener(listener);
                }
            });

            return tokenSource;
        }
    }
}
