package com.smartbite.activities;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.smartbite.databinding.ActivityProfileBinding;
public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding=ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if(binding.toolbar!=null){setSupportActionBar(binding.toolbar);binding.toolbar.setNavigationOnClickListener(v->onBackPressed());}
    }
}