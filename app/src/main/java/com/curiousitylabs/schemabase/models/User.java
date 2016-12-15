package com.curiousitylabs.schemabase.models;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// [START blog_user_class]
@IgnoreExtraProperties
public class User extends Model {


    public String name;
    public String email;
    public Date dob;
    public String imageUrl;

    public User(){

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
