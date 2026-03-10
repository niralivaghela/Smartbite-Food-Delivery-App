package com.smartbite.viewmodels;
import androidx.lifecycle.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartbite.models.User;
import com.smartbite.utils.Constants;
public class AuthViewModel extends ViewModel {
    private final FirebaseAuth auth=FirebaseAuth.getInstance();
    private final FirebaseFirestore db=FirebaseFirestore.getInstance();
    private final MutableLiveData<Boolean> loginSuccess=new MutableLiveData<>();
    private final MutableLiveData<String> errorMsg=new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading=new MutableLiveData<>();
    public LiveData<Boolean> getLoginSuccess(){return loginSuccess;}
    public LiveData<String> getErrorMsg(){return errorMsg;}
    public LiveData<Boolean> getLoading(){return loading;}
    public void login(String email,String password){
        loading.setValue(true);
        auth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener(r->{loading.setValue(false);loginSuccess.setValue(true);})
            .addOnFailureListener(e->{loading.setValue(false);errorMsg.setValue(e.getMessage());});
    }
    public void register(String email,String password){
        loading.setValue(true);
        auth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener(r->{loading.setValue(false);loginSuccess.setValue(true);})
            .addOnFailureListener(e->{loading.setValue(false);errorMsg.setValue(e.getMessage());});
    }
    public void logout(){auth.signOut();}
    public boolean isLoggedIn(){return auth.getCurrentUser()!=null;}
}