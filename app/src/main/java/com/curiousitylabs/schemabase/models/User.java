package com.curiousitylabs.schemabase.models;

import com.curiousitylabs.schemabase.SchemabaseClient;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// [START blog_user_class]
@IgnoreExtraProperties
public class User extends Model<User> {


    public String name;
    public String email;
    public Date dob;
    public String imageUrl;

    public User(){

    }

    @Override
    public SchemabaseClient.DbValueRef<User> write() {
        String key = getUid();
        if(key == null){
            key = FirebaseDatabase.getInstance().getReference("schemas").getKey();
        }

        return new SchemabaseClient.DbValueRef<>(SchemabaseClient.Verb.POST, this, User.class, "users", key);
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("email", email);
        result.put("dob", dob);
        result.put("imageUrl", imageUrl);
        return result;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
// [END blog_user_class]
