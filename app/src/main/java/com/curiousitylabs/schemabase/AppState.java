package com.curiousitylabs.schemabase;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.ObservableArrayList;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.curiousitylabs.schemabase.models.Schema;
import com.curiousitylabs.schemabase.models.User;
import com.firebase.ui.storage.images.FirebaseImageLoader;

/**
 * Created by anthony on 29/04/16.
 */
public class AppState {
    private static AppState ourInstance = new AppState();

    private Application mApp;
    private boolean configured;

    public static final String TAG = AppState.class.getSimpleName();
    public static final String APP_SESSION = "schemabase_session";

    public static final String KEY_USER_EMAIL = "session_key_user_email";
    public static final String KEY_USER_ID = "session_key_user_id";

    private ConnectivityManager mConnManager;

    public ObservableArrayList<Schema> schemas = new ObservableArrayList<>();

    protected SharedPreferences session;

    private User mUser;

    public RequestQueue mRequestQueue;
    public ImageLoader mImageLoader;
    public FirebaseImageLoader mFbImageLoader;

    private SchemabaseClient mClient;

    public void setRequestQueue(RequestQueue queue) {
        this.mRequestQueue = queue;
    }

    public static AppState getInstance() {
        return ourInstance;
    }

    private AppState() {

    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public void configure(Application appContext) {
        mApp = appContext;

        session = mApp.getSharedPreferences(APP_SESSION, Context.MODE_PRIVATE);

        mConnManager = (ConnectivityManager) mApp.getSystemService(Context.CONNECTIVITY_SERVICE);
        mFbImageLoader = new FirebaseImageLoader();

        mRequestQueue = Volley.newRequestQueue(mApp);
        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<>(50);

            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }

            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });

        mClient = new SchemabaseClient();
        setConfigured(true);
    }

    public SharedPreferences getSession() {
        return session;
    }


    public void setUser(User user) {
        this.mUser = user;
        if (user != null) {
//            getSession().edit()
//                    .putInt(KEY_USER_ID, )
//                    .putString(KEY_USER_EMAIL, user.email)
//                    .commit();
        } else {
            getSession().edit().clear().apply();
        }
    }

    public User getUser() {
        return this.mUser;
    }

    public SchemabaseClient getClient() {
        return mClient;
    }


    public boolean isSignedIn() {
        return getUser() != null && getUser().email.equals(session.getString(KEY_USER_EMAIL, ""));
    }
}
