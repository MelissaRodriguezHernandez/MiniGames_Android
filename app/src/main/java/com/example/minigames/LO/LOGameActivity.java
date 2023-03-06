package com.example.minigames.LO;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.minigames.Menu.HelpActivity;
import com.example.minigames.Menu.MenuActivity;
import com.example.minigames.Menu.ScoreActivity;
import com.example.minigames.Menu.SelectorActivity;
import com.example.minigames.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class LOGameActivity extends AppCompatActivity {

    private int rows, cols, initialLights;
    private boolean[][] lightsMatrix;
    private GridLayout gridLayout;
    private Button bSolution, info;
    private ImageButton reset_button;
    private int timeScore = 0;

    private int timeElapsed;
    private CountDownTimer countDownTimer;
    private  TextView timeTextView, clickTextView;
    private int click;
    private boolean gameStarted = false;
    private long startTime = 0;
    private final Handler handler = new Handler();
    private DBHelperLights dbHelperLights;


    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Lights Out");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logame);

        dbHelperLights = new DBHelperLights(LOGameActivity.this);

        // Obtener el número de filas y columnas de la cuadrícula de luces del intent
        Intent intent = getIntent();
        rows = intent.getIntExtra("rows", 5);
        cols = intent.getIntExtra("cols", 5);
        initialLights = intent.getIntExtra("initialLights",5);

        // Crear la matriz de bombillas con el método createLightsMatrix
        lightsMatrix = createLightsMatrix(rows, cols, initialLights);

        bSolution = findViewById(R.id.solution_button);
        bSolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solver();
            }
        });

        info = findViewById(R.id.info_button);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LOGameActivity.this);
                builder.setMessage(R.string.rules_Lo);
                builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Acción a realizar cuando se hace clic en el botón "Aceptar"
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        reset_button = findViewById(R.id.reset_button);
        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreate();
            }
        });

        // Obtener la referencia al GridLayout
        gridLayout = findViewById(R.id.grid_layout);

        // Configurar el GridLayout con el número de filas y columnas especificado
        gridLayout.setRowCount(rows);
        gridLayout.setColumnCount(cols);

        // Agregar botones al GridLayout para representar las bombillas
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Button button = new Button(this);
                button.setTag(new int[] { i, j });
                button.setBackgroundResource(lightsMatrix[i][j] ? R.drawable.light_on : R.drawable.light_off);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Manejar el evento de clic en la bombilla
                        click++;
                        String c = String.valueOf(click);
                        clickTextView.setText(c);
                        int[] position = (int[]) view.getTag();
                        int row = position[0];
                        int col = position[1];
                        toggleLights(row, col);
                    }
                });

                // Agregar un nuevo parámetro de diseño para que los botones se ajusten al tamaño del GridLayout
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                params.width = 0;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                button.setLayoutParams(params);

                gridLayout.addView(button);
            }
        }

        timeTextView = findViewById(R.id.time_text_view);
        clickTextView = findViewById(R.id.click_text_view);
        click = 0;
        String c = String.valueOf(click);
        clickTextView.setText(c);


    }

    // Función para alternar el estado de las bombillas adyacentes
    private void toggleLights(int row, int col) {
        lightsMatrix[row][col] = !lightsMatrix[row][col];
        if (row > 0) {
            lightsMatrix[row-1][col] = !lightsMatrix[row-1][col];
        }
        if (row < rows - 1) {
            lightsMatrix[row+1][col] = !lightsMatrix[row+1][col];
        }
        if (col > 0) {
            lightsMatrix[row][col-1] = !lightsMatrix[row][col-1];
        }
        if (col < cols - 1) {
            lightsMatrix[row][col+1] = !lightsMatrix[row][col+1];
        }
        updateLightsUI();

        if (!gameStarted) {
            gameStarted = true;
            startTime = System.currentTimeMillis();
            handler.postDelayed(updateTimerThread, 0);
        }
    }

    // Función para actualizar el estado de las bombillas en la UI
    private void updateLightsUI() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Button button = (Button) gridLayout.getChildAt(i * cols + j);
                button.setBackgroundResource(lightsMatrix[i][j] ? R.drawable.light_on : R.drawable.light_off);
            }
        }

        if (checkLightsOff() == true) {
            System.out.println("entra cuando estan apagadas");
            showDialogWinGame();
        }
    }

    // Función para crear la matriz de bombillas
    private boolean[][] createLightsMatrix(int rows, int cols, int initialLights) {
        boolean[][] lightsMatrix = new boolean[rows][cols];
        Random random = new Random();

        // Hacer dos clicks aleatorios para asegurarse de que haya al menos una solución
        for (int k = 0; k < 2; k++) {
            int i = random.nextInt(rows);
            int j = random.nextInt(cols);
            click(lightsMatrix, i, j);
        }

        // Encender un número aleatorio de bombillas iniciales, pero asegurarse de que al menos una esté encendida
        int count = 0;
        while (count < initialLights - 1) {
            int i = random.nextInt(rows);
            int j = random.nextInt(cols);
            if (!lightsMatrix[i][j]) {
                click(lightsMatrix, i, j);
                count++;
            }
        }

        // Encender una bombilla más para asegurarse de que hay al menos una posible solución
        int i = random.nextInt(rows);
        int j = random.nextInt(cols);
        if (!lightsMatrix[i][j]) {
            click(lightsMatrix, i, j);
        } else {
            // Si la bombilla ya está encendida, buscar una que esté apagada y encenderla
            for (int x = 0; x < rows; x++) {
                for (int y = 0; y < cols; y++) {
                    if (!lightsMatrix[x][y]) {
                        click(lightsMatrix, x, y);
                        return lightsMatrix;
                    }
                }
            }
        }

        return lightsMatrix;
    }

    // Función para realizar un click en la matriz de bombillas
    private void click(boolean[][] lightsMatrix, int i, int j) {
        int rows = lightsMatrix.length;
        int cols = lightsMatrix[0].length;
        lightsMatrix[i][j] = !lightsMatrix[i][j];
        if (i > 0) {
            lightsMatrix[i - 1][j] = !lightsMatrix[i - 1][j];
        }
        if (j > 0) {
            lightsMatrix[i][j - 1] = !lightsMatrix[i][j - 1];
        }
        if (i < rows - 1) {
            lightsMatrix[i + 1][j] = !lightsMatrix[i + 1][j];
        }
        if (j < cols - 1) {
            lightsMatrix[i][j + 1] = !lightsMatrix[i][j + 1];
        }
    }

    private boolean checkLightsOff() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (lightsMatrix[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    // Función para detener el cronómetro
    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private final Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            long timeInMilliseconds = System.currentTimeMillis() - startTime;
            int totalSeconds = (int) (timeInMilliseconds / 1000);
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            timeScore = totalSeconds;
            String timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            timeTextView.setText(timeText);

            handler.postDelayed(this, 1000);
        }
    };

    @SuppressLint("AppCompatCustomView")
    public void solver() {
        boolean[][] grid = new boolean[rows][cols];

        List<Coord> coords = new ArrayList<Coord>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j]) { // si la celda está activada
                    coords.add(new Coord(i, j)); // agregar sus coordenadas a la lista
                }
            }
        }

        GridInterface startGrid = GridUtils.getGridWithSomeActivatedCoords(rows, cols, coords);
        GridInterface finalGrid = GridUtils.getFullGrid(rows, cols);
        PatternInterface pattern = PatternUtils.getClassicPattern();

        Solver solver = new Solver(startGrid, finalGrid, pattern);
        Solutions solutions = solver.solve();
        System.out.println(solutions);

        // crear un contenedor para el TextView
        LinearLayout container = new LinearLayout(this);
        container.setGravity(Gravity.CENTER);
        container.addView(new TextView(this) {{
            setText(solutions.toString());
        }});

        // crear un ScrollView y agregar el contenedor
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(container);

        // mostrar las soluciones en un cuadro de diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Solutions");
        builder.setView(scrollView);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }




    private void showDialogWinGame(){
        stopTimer();
        handler.removeCallbacks(updateTimerThread); // Detener la actualización del tiempo
        gameStarted = false;
        timeElapsed = 0;
        startTime = 0;
        // Crear un diálogo personalizado
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.game_win_dialog_lo);
        dialog.setCanceledOnTouchOutside(false);

        //Obtener valores elementos
        Button exitButton = dialog.findViewById(R.id.exit_button);
        Button restartButton = dialog.findViewById(R.id.play_again_button);
        TextView textscoreTime = dialog.findViewById(R.id.win_score_time);

        textscoreTime.setText("Your time: "+ timeTextView.getText());
        TextView textscoreClicks = dialog.findViewById(R.id.win_score_click);
        textscoreClicks.setText("Your total clicks: "+clickTextView.getText());
        exitButton.setEnabled(true);
        restartButton.setEnabled(true);
        // Configurar el botón "Salir"
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameEditText = dialog.findViewById(R.id.player_name_input);
                String name = nameEditText.getText().toString().trim();
                dbHelperLights.insertScore(name, String.valueOf(timeTextView.getText()),String.valueOf(clickTextView.getText()), String.valueOf(rows), String.valueOf(timeScore) );
                Intent intent = new Intent(LOGameActivity.this, MenuActivity.class);
                startActivity(intent);
                dialog.dismiss();
                startTime = System.currentTimeMillis() - timeElapsed; // Reiniciar el tiempo
                handler.postDelayed(updateTimerThread, 0); // Iniciar la actualización del tiempo
            }
        });
        // Configurar el botón "Reiniciar"
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameEditText = dialog.findViewById(R.id.player_name_input);
                String name = nameEditText.getText().toString().trim();
                dbHelperLights.insertScore(name, String.valueOf(timeTextView.getText()),String.valueOf(clickTextView.getText()), String.valueOf(rows), String.valueOf(timeScore) );
                recreate();
                dialog.dismiss();
                startTime = System.currentTimeMillis() - timeElapsed; // Reiniciar el tiempo
                handler.postDelayed(updateTimerThread, 0); // Iniciar la actualización del tiempo
            }
        });
        // Mostrar el diálogo
        dialog.show();
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







