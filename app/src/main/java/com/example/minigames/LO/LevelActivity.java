package com.example.minigames.LO;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.minigames.Menu.HelpActivity;
import com.example.minigames.Menu.MenuActivity;
import com.example.minigames.Menu.ScoreActivity;
import com.example.minigames.Menu.SelectorActivity;
import com.example.minigames.R;

public class LevelActivity extends AppCompatActivity {

    private Button easyButton, mediumButton, hardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Lights Out level selector");
        setContentView(R.layout.activity_level);

        easyButton = findViewById(R.id.easy_level);
        mediumButton = findViewById(R.id.medium_level);
        hardButton = findViewById(R.id.hard_level);

        easyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGameActivity(4, 4, 5, "easy");
            }
        });

        mediumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGameActivity(5, 5, 7, "medium");
            }
        });

        hardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGameActivity(6, 6, 10, "hard");
            }
        });
    }

    private void startGameActivity(int rows, int cols, int initialLights, String level) {
        Intent intent = new Intent(this, LOGameActivity.class);
        intent.putExtra("rows", rows);
        intent.putExtra("cols", cols);
        intent.putExtra("initialLights", initialLights);
        intent.putExtra("level", level);
        startActivity(intent);
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


