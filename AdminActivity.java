package com.smartbite.activities;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.smartbite.databinding.ActivityAdminBinding;
public class AdminActivity extends AppCompatActivity {
    private ActivityAdminBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding=ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if(binding.toolbar!=null){setSupportActionBar(binding.toolbar);binding.toolbar.setNavigationOnClickListener(v->onBackPressed());}
    }
}