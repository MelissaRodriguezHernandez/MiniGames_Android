package com.example.minigames;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TableRow;

import androidx.appcompat.app.AppCompatActivity;

import com.example.minigames.Menu.MenuActivity;
import com.example.minigames.databinding.ActivitySplashBinding;


public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.anim2);
        binding.TextViewTopTitle.startAnimation(anim2);

        Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.anim1);
        LayoutAnimationController controller = new LayoutAnimationController(anim1);
        for(int i=0;i<binding.TableLayout01.getChildCount();i++){
            TableRow row = (TableRow) binding.TableLayout01.getChildAt(i);
            row.setLayoutAnimation(controller);
        }

        Animation anim3 = AnimationUtils.loadAnimation(this, R.anim.anim3);
        binding.TextViewBottomTitle.startAnimation(anim3);
        anim3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                startActivity(intent);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }
}