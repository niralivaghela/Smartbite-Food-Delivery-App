package com.smartbite.activities;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smartbite.R;
import com.smartbite.databinding.ActivityRegisterBinding;
import com.smartbite.models.User;
import com.smartbite.utils.Constants;
import java.util.UUID;
public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Uri selectedImageUri=null;
    private String selectedRole=Constants.ROLE_CUSTOMER;
    private final ActivityResultLauncher<String> imagePickerLauncher=
        registerForActivityResult(new ActivityResultContracts.GetContent(),uri->{
            if(uri!=null){selectedImageUri=uri;binding.ivProfilePic.setImageURI(uri);}
        });
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding=ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth=FirebaseAuth.getInstance();db=FirebaseFirestore.getInstance();
        storageRef=FirebaseStorage.getInstance().getReference();
        setupClickListeners();
    }
    private void setupClickListeners(){
        binding.btnBack.setOnClickListener(v->onBackPressed());
        binding.ivProfilePic.setOnClickListener(v->imagePickerLauncher.launch("image/*"));
        binding.tvAddPhoto.setOnClickListener(v->imagePickerLauncher.launch("image/*"));
        binding.rgRole.setOnCheckedChangeListener((g,id)->{
            if(id==R.id.rbCustomer) selectedRole=Constants.ROLE_CUSTOMER;
            else if(id==R.id.rbRestaurant) selectedRole=Constants.ROLE_RESTAURANT;
            else if(id==R.id.rbAgent) selectedRole=Constants.ROLE_AGENT;
        });
        binding.btnRegister.setOnClickListener(v->{
            String name=binding.etName.getText().toString().trim();
            String email=binding.etEmail.getText().toString().trim();
            String phone=binding.etPhone.getText().toString().trim();
            String pass=binding.etPassword.getText().toString().trim();
            String conf=binding.etConfirmPassword.getText().toString().trim();
            if(validateInput(name,email,phone,pass,conf)) registerUser(name,email,phone,pass);
        });
        binding.tvLogin.setOnClickListener(v->{startActivity(new Intent(this,LoginActivity.class));finish();});
    }
    private boolean validateInput(String name,String email,String phone,String pass,String conf){
        boolean valid=true;
        if(TextUtils.isEmpty(name)){binding.tilName.setError("Name required");valid=false;}else binding.tilName.setError(null);
        if(TextUtils.isEmpty(email)||!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){binding.tilEmail.setError("Valid email required");valid=false;}else binding.tilEmail.setError(null);
        if(TextUtils.isEmpty(phone)||phone.length()<10){binding.tilPhone.setError("Valid phone required");valid=false;}else binding.tilPhone.setError(null);
        if(TextUtils.isEmpty(pass)||pass.length()<6){binding.tilPassword.setError("Password min 6 chars");valid=false;}else binding.tilPassword.setError(null);
        if(!pass.equals(conf)){binding.tilConfirmPassword.setError("Passwords don't match");valid=false;}else binding.tilConfirmPassword.setError(null);
        return valid;
    }
    private void registerUser(String name,String email,String phone,String pass){
        binding.btnRegister.setEnabled(false);binding.btnRegister.setText("Creating Account...");
        mAuth.createUserWithEmailAndPassword(email,pass)
            .addOnSuccessListener(r->{
                String uid=r.getUser().getUid();
                if(selectedImageUri!=null) uploadProfilePicAndSave(uid,name,email,phone);
                else saveUserToFirestore(uid,name,email,phone,"");
            })
            .addOnFailureListener(e->{
                binding.btnRegister.setEnabled(true);binding.btnRegister.setText("CREATE ACCOUNT");
                Toast.makeText(this,"Registration Failed: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            });
    }
    private void uploadProfilePicAndSave(String uid,String name,String email,String phone){
        StorageReference ref=storageRef.child("profile_pics/"+uid+".jpg");
        ref.putFile(selectedImageUri)
            .addOnSuccessListener(t->ref.getDownloadUrl()
                .addOnSuccessListener(uri->saveUserToFirestore(uid,name,email,phone,uri.toString())))
            .addOnFailureListener(e->saveUserToFirestore(uid,name,email,phone,""));
    }
    private void saveUserToFirestore(String uid,String name,String email,String phone,String pic){
        User user=new User(uid,name,email,phone,selectedRole);
        user.setProfilePic(pic);
        user.setReferralCode("SB"+uid.substring(0,6).toUpperCase());
        db.collection(Constants.COLLECTION_USERS).document(uid).set(user)
            .addOnSuccessListener(u->{
                Toast.makeText(this,"Account Created! 🎉",Toast.LENGTH_SHORT).show();
                redirectByRole();
            })
            .addOnFailureListener(e->Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show());
    }
    private void redirectByRole(){
        Intent intent;
        if(Constants.ROLE_RESTAURANT.equals(selectedRole)) intent=new Intent(this,RestaurantPanelActivity.class);
        else if(Constants.ROLE_AGENT.equals(selectedRole)) intent=new Intent(this,DeliveryAgentActivity.class);
        else intent=new Intent(this,HomeActivity.class);
        startActivity(intent);finishAffinity();
    }
}