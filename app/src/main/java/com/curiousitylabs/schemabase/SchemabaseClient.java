package com.curiousitylabs.schemabase;


import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.curiousitylabs.schemabase.models.Model;
import com.curiousitylabs.schemabase.models.Schema;
import com.curiousitylabs.schemabase.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.base.Joiner;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.io.File;

import bolts.CancellationTokenSource;

/**
 * Created by anthony on 13/12/2016.
 */

public class SchemabaseClient {

    public String TAG() {
        return this.getClass().getSimpleName();
    }

    StorageReference storageRef;

    public SchemabaseClient(){
        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://schemabase-3c550.appspot.com");
    }

    public DbValueRef<User> getUserById(String uid){
        return new DbValueRef<>(Verb.GET, User.class, "users", uid);
    }

    public DbChildRef<Schema> getSchemas(){
        return new DbChildRef<>(Verb.GET, Schema.class, "schemas");
    }

    public DbValueRef<Schema> getSchemaById(String id){
        return new DbValueRef<>(Verb.GET, Schema.class, "schemas", id);
    }

    public Promise<String, Exception, Double> uploadFile(String imageFileName, String...dirPathFragments) {
        final DeferredObject<String, Exception, Double> deferred = new DeferredObject<>();

        Uri file = Uri.fromFile(new File(imageFileName));
        String dirPath = Joiner.on('/').join(dirPathFragments);
        String remoteFileName = dirPath + "/" + file.getLastPathSegment();

        Log.d(TAG(), String.format("uploading local file \'%s\' to remote location \'%s\'", imageFileName, remoteFileName));
        StorageReference riversRef = storageRef.child(remoteFileName);
        UploadTask uploadTask = riversRef.putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                deferred.reject(exception);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                String downloadUrl = taskSnapshot.getMetadata().getPath();
                deferred.resolve(downloadUrl);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                deferred.notify(progress);
            }
        });
        return deferred.promise();
    }

    // Helper Classes

    enum DbRefEventType {
        CHILD_ADDED, CHILD_REMOVED, CHILD_CHANGED, CHILD_MOVED, DATA_CHANGE, CANCELLED
    }

    public enum Verb {
        GET, POST, PUT
    }

    public static abstract class Callback<T> {

        public static String TAG = Callback.class.getSimpleName();


        public abstract void onData(DbRefEventType eventType, T model, String previousUid);

        public void onError(DatabaseError error) {
            Log.e(TAG, error.toString());
        }

        public void processSnapshot(DbRefEventType eventType, Class<T> clazz, DataSnapshot dataSnapshot, String previousChildName){
            T model = dataSnapshotToModel(dataSnapshot, clazz);

            if(model instanceof Model){
                ((Model) model).setUid(dataSnapshot.getKey());
            }

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

        Object payload;

        public DbValueRef(Verb verb, Class<T> clazz, String... pathFragments) {
            super(verb, clazz, pathFragments);
        }

        public DbValueRef(Verb verb, Object payload, Class<T> clazz, String... pathFragments) {
            super(verb, clazz, pathFragments);
            this.payload = payload;
        }

        @Override
        public void then(final Callback<T> callback) {
            if(verb == Verb.GET) {
                super.then(callback);
            } else if(verb == Verb.POST || verb == Verb.PUT) {
                getRef().setValue(payload, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null) {
                            callback.onData(DbRefEventType.DATA_CHANGE, clazz.cast(payload), null);
                        }else{
                            callback.onError(databaseError);
                        }
                    }
                });
            }
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
