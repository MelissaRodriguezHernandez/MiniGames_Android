package com.example.minigames.Menu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.minigames.DM.DBHelperDM;
import com.example.minigames.LO.DBHelperLights;
import com.example.minigames.R;

import java.util.ArrayList;

public class ScoreActivity extends AppCompatActivity {

    private Spinner scoreSelector;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Scores : Select a game");
        setContentView(R.layout.activity_score);

        DBHelperDM dbHelper = new DBHelperDM(ScoreActivity.this);
        DBHelperLights dbHelperLights = new DBHelperLights(ScoreActivity.this);

        ArrayList<String> scoresListDM = dbHelper.getScoresWithRanking();
        ArrayList<String> scoreListLO = dbHelperLights.getScoresWithRanking();

         ListView scoreDMList = findViewById(R.id.recycler_view_scores_dm);
         ListView scoresLOList = findViewById(R.id.recycler_view_scores_lo);

        //ScoreSSpinner
        scoreSelector = findViewById(R.id.score_selector);
        String[] elementos = {"2048", "Lights Out"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner, elementos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scoreSelector.setAdapter(adapter);

        scoreSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtener la opción seleccionada
                String selectedGame = parent.getItemAtPosition(position).toString();

                // Hacer algo dependiendo de la opción seleccionada
                if (selectedGame.equals("2048")) {
                    scoreDMList.setVisibility(View.VISIBLE);
                    scoresLOList.setVisibility(View.INVISIBLE);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ScoreActivity.this, R.layout.list, scoresListDM);
                    scoreDMList.setAdapter(adapter);

                } else if (selectedGame.equals("Lights Out")) {
                    scoreDMList.setVisibility(View.INVISIBLE);
                    scoresLOList.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> adapter1 = new ArrayAdapter<>(ScoreActivity.this, R.layout.list_lo, scoreListLO);
                    scoresLOList.setAdapter(adapter1);


                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nada que hacer
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


