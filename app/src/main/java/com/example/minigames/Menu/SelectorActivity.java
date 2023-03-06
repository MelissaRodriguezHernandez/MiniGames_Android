package com.example.minigames.Menu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.minigames.DM.DMGameActivity;
import com.example.minigames.LO.LevelActivity;
import com.example.minigames.R;

public class SelectorActivity extends AppCompatActivity {

    private ImageButton lo, dm;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Select a Game");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectgame);

        lo = findViewById(R.id.LOGame);
        lo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectorActivity.this, LevelActivity.class);
                startActivity(intent);
            }
        });

        dm = findViewById(R.id.DMGame);
        dm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(SelectorActivity.this, DMGameActivity.class);
                startActivity(intent1);
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Menu:
                Intent intent3 = new Intent(this, MenuActivity.class);
                startActivity(intent3);
                return true;
            case R.id.select_game:
                Intent intent = new Intent(this, SelectorActivity.class);
                startActivity(intent);
                return true;
            case R.id.scores:
                Intent intent1 = new Intent(this, ScoreActivity.class);
                startActivity(intent1);
                return true;
            case R.id.help:
                Intent intent2 = new Intent(this, HelpActivity.class);
                startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
