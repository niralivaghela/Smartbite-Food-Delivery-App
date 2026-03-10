package com.smartbite.utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
public class FirebaseHelper {
    private static FirebaseHelper instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private FirebaseHelper(){
        auth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        storage=FirebaseStorage.getInstance();
    }
    public static FirebaseHelper getInstance(){
        if(instance==null) instance=new FirebaseHelper();
        return instance;
    }
    public FirebaseAuth getAuth(){return auth;}
    public FirebaseFirestore getDb(){return db;}
    public FirebaseStorage getStorage(){return storage;}
    public String getCurrentUserId(){
        return auth.getCurrentUser()!=null?auth.getCurrentUser().getUid():null;
    }
    public boolean isLoggedIn(){return auth.getCurrentUser()!=null;}
    public void logout(){auth.signOut();}
}