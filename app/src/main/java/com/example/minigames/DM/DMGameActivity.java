package com.example.minigames.DM;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.minigames.Menu.HelpActivity;
import com.example.minigames.Menu.MenuActivity;
import com.example.minigames.Menu.ScoreActivity;
import com.example.minigames.Menu.SelectorActivity;
import com.example.minigames.R;

import java.util.Arrays;
import java.util.Random;

public class DMGameActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private TextView scoreTextView, scoreTextViewBest;
    private int[][] board;
    private int score;
    private GestureDetector gestureDetector;
    private ImageButton reset_button;
    private Button rules, button;
    private boolean over;
    private DBHelperDM dbHelperDM ;

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("2048");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dmgame);

        rules = findViewById(R.id.button_rule);
        rules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DMGameActivity.this);
                builder.setMessage(R.string.rule_2048);
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

        dbHelperDM = new DBHelperDM(DMGameActivity.this);

        over = false;

        // Inicializa los elementos de la UI
        scoreTextView = findViewById(R.id.scoreTextView);
        scoreTextViewBest = findViewById(R.id.scoreTextViewBest);

        String bestScore = String.valueOf(dbHelperDM.getHighScore());
        scoreTextViewBest.setText("Best: "+bestScore);

        // Inicializa el juego
        board = new int[4][4];
        score = 0;

        // Carga el estado anterior si existe
        if (savedInstanceState != null) {
            int[] flattenedBoard = savedInstanceState.getIntArray("board");
            int index = 0;
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    board[i][j] = flattenedBoard[index++];
                }
            }
            score = savedInstanceState.getInt("score");
        } else {
            // Genera dos fichas iniciales de forma aleatoria
            addRandomTile();
            addRandomTile();
        }


        // Actualiza la interfaz de usuario con el estado actual del juego
        updateUI();

        // Inicializa el detector de gestos
        gestureDetector = new GestureDetector(this, this);

        reset_button = findViewById(R.id.reset_button_R);
        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGame();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int[] flattenedBoard = new int[board.length * board[0].length];
        int index = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                flattenedBoard[index++] = board[i][j];
            }
        }
        outState.putIntArray("board", flattenedBoard);
        outState.putInt("score", score);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // Detecta el gesto de deslizar en una dirección determinada y actualiza el juego
        float deltaX = e2.getX() - e1.getX();
        float deltaY = e2.getY() - e1.getY();
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (deltaX > 0) {
                slideRight();
            } else {
                slideLeft();
            }
        } else {
            if (deltaY > 0) {
                slideDown();
            } else {
                slideUp();
            }
        }

        // Actualiza la UI con el nuevo estado del juego
        updateUI();

        return true;
    }


    private void slideLeft() {
        boolean canMove = canSlideLeft();

        for (int i = 0; i < board.length; i++) {
            int[] row = board[i];
            int[] mergedRow = mergeTiles(row); // Fusiona cualquier par de fichas que puedan ser fusionados en esta fila
            if (!isSameArray(row, mergedRow)) {
                // La fila ha cambiado, por lo que se puede mover hacia la izquierda
                canMove = true;
                // Crea una nueva matriz temporal para la fila y copia todos los elementos no nulos de la fila original
                int[] newRow = new int[board.length];
                int index = 0;
                for (int j = 0; j < board.length; j++) {
                    if (mergedRow[j] != 0) {
                        newRow[index] = mergedRow[j];
                        index++;
                    }
                }
                // Actualiza la fila original con la nueva matriz temporal y agrega ceros al final
                for (int j = 0; j < board.length; j++) {
                    if (j < index) {
                        row[j] = newRow[j];
                    } else {
                        row[j] = 0;
                    }
                }
            }
        }
        if (canMove ) {
            updateUI();
            addRandomTile();

        }
    }


    private void slideRight() {
        boolean canSlide = canSlideRight(); // Bandera para verificar si se puede realizar el movimiento
        // Itera sobre cada fila del tablero
        for (int i = 0; i < board.length; i++) {
            int[] row = board[i];
            int[] reversedRow = reverseArray(row);
            int[] mergedRow = mergeTiles(reversedRow);
            int[] reversedMergedRow = reverseArray(mergedRow);

            // Si la fila cambió, significa que se puede realizar el movimiento
            if (!Arrays.equals(row, reversedMergedRow)) {
                canSlide = true;
            }

            // Actualiza la fila en el tablero
            for (int j = 0; j < row.length; j++) {
                row[j] = (j < reversedMergedRow.length) ? reversedMergedRow[j] : 0;
            }
        }

        // Si se pudo realizar el movimiento, agrega una nueva ficha y actualiza la UI
        if (canSlide ) {
            updateUI();
            addRandomTile();

        }
    }


    private void slideUp() {
        boolean canSlide = canSlideUp(); // Bandera para verificar si se puede realizar el movimiento
        // Itera sobre cada columna del tablero
        for (int j = 0; j < board[0].length; j++) {
            int[] column = getColumn(j);
            int[] mergedColumn = mergeTiles(column);

            // Si la columna cambió, significa que se puede realizar el movimiento
            if (!Arrays.equals(column, mergedColumn)) {
                canSlide = true;
            }

            // Actualiza la columna en el tablero
            for (int i = 0; i < column.length; i++) {
                board[i][j] = (i < mergedColumn.length) ? mergedColumn[i] : 0;
            }
        }

        // Si se pudo realizar el movimiento, agrega una nueva ficha y actualiza la UI
        if (canSlide ) {
            updateUI();
            addRandomTile();

        }

    }

    private void slideDown() {
        boolean canSlide = canSlideDown();

        // Itera sobre cada columna del tablero
        for (int j = 0; j < board[0].length; j++) {
            int[] column = getColumn(j);
            int[] reversedColumn = reverseArray(column);
            int[] mergedColumn = mergeTiles(reversedColumn);
            int[] reversedMergedColumn = reverseArray(mergedColumn);

            // Si la columna cambió, significa que se puede realizar el movimiento
            if (!Arrays.equals(column, reversedMergedColumn)) {
                canSlide = true;
            }

            // Actualiza la columna en el tablero
            for (int i = 0; i < column.length; i++) {
                board[i][j] = (i < reversedMergedColumn.length) ? reversedMergedColumn[i] : 0;
            }
        }

        // Si se pudo realizar el movimiento, agrega una nueva ficha y actualiza la UI
        if (canSlide ) {
            updateUI();
            addRandomTile();

        }

    }

    private int[] getColumn(int j) {
        int[] column = new int[board.length];
        for (int i = 0; i < board.length; i++) {
            column[i] = board[i][j];
        }
        return column;
    }

    @SuppressLint("StringFormatInvalid")
    private int[] mergeTiles(int[] tiles) {
        int[] mergedTiles = new int[tiles.length];
        int index = 0;
        try {

            for (int i = 0; i < tiles.length; i++) {
                if (tiles[i] != 0) {
                    if (index > 0 && tiles[i] == mergedTiles[index - 1]) {
                        // Fusiona las fichas si son iguales
                        mergedTiles[index - 1] *= 2;
                        score += mergedTiles[index - 1]; // Actualiza el puntaje
                        String s = String.valueOf(score);
                        scoreTextView.setText("Score: " + s);
                        scoreTextView.invalidate();
                        scoreTextView.requestLayout();
                        updateUI(); // Actualiza el texto del score

                    } else {
                        // Agrega la ficha al nuevo conjunto fusionado
                        mergedTiles[index] = tiles[i];
                        index++;
                    }
                }
            }
        }catch (RuntimeException e){
            System.out.println(e);
            return null;
        }
        return mergedTiles;
    }

    private int[] reverseArray(int[] array) {
        int[] reversedArray = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            reversedArray[i] = array[array.length - 1 - i];
        }
        return reversedArray;
    }

    private void addRandomTile() {
        // Encuentra una posición vacía aleatoria en el tablero de juego
        int row, column;
            do {
                Random random = new Random();
                row = random.nextInt(4);
                column = random.nextInt(4);
            } while (board[row][column] != 0);

            // Agrega una ficha nueva al tablero de juego
            board[row][column] = (Math.random() < 0.9) ? 2 : 4;

    }



    private boolean canSlideLeft() {
        for (int i = 0; i < board.length; i++) {
            int[] row = board[i];
            int[] mergedRow = mergeTiles(row);
            if (!isSameArray(row, mergedRow)) {
                return true;
            }
        }
        return false;
    }


    private boolean canSlideRight() {
        for (int i = 0; i < board.length; i++) {
            int[] row = board[i];
            int[] reversedRow = reverseArray(row);
            int[] mergedRow = mergeTiles(reversedRow);
            int[] reversedMergedRow = reverseArray(mergedRow);
            if (!isSameArray(row, reversedMergedRow)) {
                return true;
            }
        }
        return false;
    }

    private boolean canSlideUp() {

        for (int j = 0; j < board[0].length; j++) {
            int[] column = getColumn(j);
            int[] mergedColumn = mergeTiles(column);
            if (!isSameArray(column, mergedColumn)) {
                return true;
            }
        }
        return false;
    }

    private boolean canSlideDown() {
        for (int j = 0; j < board[0].length; j++) {
            int[] column = getColumn(j);
            int[] mergedColumn = mergeTiles(column);
            if (!isSameArray(column, mergedColumn)) {
                return true;
            }
        }
        return false;
    }


    private boolean isSameArray(int[] array1, int[] array2) {
        if (array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }


    @SuppressLint("StringFormatInvalid")
    private void updateUI() {
        over = isGameOver();
        if(!over){
            // Actualiza el marcador de puntuación
            String s = String.valueOf(score);
            scoreTextView.setText("Score: "+s);

            // Actualiza el tablero de juego en la UI
            TableLayout boardLayout = findViewById(R.id.boardLayout);
            boardLayout.removeAllViews();
            for (int i = 0; i < board.length; i++) {
                TableRow row = new TableRow(this);
                for (int j = 0; j < board[i].length; j++) {
                    TextView cell = new TextView(this);
                    cell.setText((board[i][j] != 0) ? String.valueOf(board[i][j]) : "");
                    cell.setTextSize(24);
                    cell.setPadding(8, 8, 8,8);
                    // Cambia el color de fondo según el valor de la ficha
                    int colorId;
                    switch (board[i][j]) {
                        case 2:
                            colorId = R.drawable.tile_2;
                            break;
                        case 4:
                            colorId = R.drawable.tile_4;
                            break;
                        case 8:
                            colorId = R.drawable.tile_8;
                            break;
                        case 16:
                            colorId = R.drawable.tile_16;
                            break;
                        case 32:
                            colorId = R.drawable.tile_32;
                            break;
                        case 64:
                            colorId = R.drawable.tile_64;
                            break;
                        case 128:
                            colorId = R.drawable.tile_128;
                            break;
                        case 256:
                            colorId = R.drawable.tile_256;
                            break;
                        case 512:
                            colorId = R.drawable.tile_512;
                            break;
                        case 1024:
                            colorId = R.drawable.tile_1024;
                            break;
                        case 2048:
                            colorId = R.drawable.tile_2048;
                            break;
                        default:
                            colorId = R.drawable.fondo_tile;
                            break;
                    }
                    cell.setBackgroundResource(colorId);

                    // Añade la celda a la fila actual
                    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
                    layoutParams.setMargins(8, 8, 8, 8);
                    row.addView(cell, layoutParams);
                }
                // Añade la fila actual al tablero de juego
                boardLayout.addView(row);
            }
        }else{
            showGameOverDialog();
        }
        if(isGameWon() == true){
            showWinDialog();
        }
    }

    private boolean isGameOver() {
        // Verifica si hay celdas vacías
        for (int[] row : board) {
            for (int tile : row) {
                if (tile == 0) {
                    return false;
                }
            }
        }

        // Verifica si hay combinaciones posibles entre las fichas
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                int currentTile = board[i][j];
                // Verifica si se puede fusionar con la ficha de la izquierda
                if (j > 0 && currentTile == board[i][j-1]) {
                    return false;
                }
                // Verifica si se puede fusionar con la ficha de la derecha
                if (j < board[0].length-1 && currentTile == board[i][j+1]) {
                    return false;
                }
                // Verifica si se puede fusionar con la ficha de arriba
                if (i > 0 && currentTile == board[i-1][j]) {
                    return false;
                }
                // Verifica si se puede fusionar con la ficha de abajo
                if (i < board.length-1 && currentTile == board[i+1][j]) {
                    return false;
                }
            }
        }

        // Si no se encontró ninguna celda vacía ni combinaciones posibles, entonces el juego ha terminado
        return true;
    }

    @SuppressLint("SetTextI18n")
    private void resetGame() {
        String bestScore = String.valueOf(dbHelperDM.getHighScore());
        if(bestScore != null){
            scoreTextViewBest.setText("Best: "+bestScore);
        }
        // Reinicia el tablero de juego y el marcador de puntuación
        board = new int[4][4];
        score = 0;

        // Genera dos fichas iniciales de forma aleatoria
        addRandomTile();
        addRandomTile();

        // Actualiza la UI con el nuevo estado del juego
        updateUI();
    }

    @SuppressLint("SetTextI18n")
    private void showGameOverDialog() {
        // Crear un diálogo personalizado
        Dialog dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.game_over_dialog_dm);
        // Obtener los elementos del diálogo
        Button exitButton = dialog.findViewById(R.id.exit_button);
        Button restartButton = dialog.findViewById(R.id.play_again_button);
        TextView textscore = dialog.findViewById(R.id.game_over_score);
        textscore.setText("Your score was: "+score);
        exitButton.setEnabled(true);
        restartButton.setEnabled(true);

        // Configurar el botón "Salir"
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameEditText = dialog.findViewById(R.id.player_name_input);
                String name = nameEditText.getText().toString().trim();
                dbHelperDM.insertScore(name, score);
                Intent intent = new Intent(DMGameActivity.this, MenuActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        // Configurar el botón "Reiniciar"
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameEditText = dialog.findViewById(R.id.player_name_input);
                String name = nameEditText.getText().toString().trim();
                dbHelperDM.insertScore(name, score);
                resetGame();
                dialog.dismiss();
            }
        });
        // Mostrar el diálogo
        dialog.show();
    }

    private void showWinDialog(){
        // Crear un diálogo personalizado
        Dialog dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.game_win_dialog_dm);
        // Obtener los elementos del diálogo
        Button exitButton = dialog.findViewById(R.id.exit_button);
        Button restartButton = dialog.findViewById(R.id.play_again_button);
        TextView textscore = dialog.findViewById(R.id.game_over_score);
        textscore.setText("Your score was: "+score);
        exitButton.setEnabled(true);
        restartButton.setEnabled(true);

        // Configurar el botón "Salir"
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameEditText = dialog.findViewById(R.id.player_name_input);
                String name = nameEditText.getText().toString().trim();
                dbHelperDM.insertScore(name, score);
                Intent intent = new Intent(DMGameActivity.this, MenuActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        // Configurar el botón "Reiniciar"
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameEditText = dialog.findViewById(R.id.player_name_input);
                String name = nameEditText.getText().toString().trim();
                dbHelperDM.insertScore(name, score);
                resetGame();
                dialog.dismiss();
            }
        });
        // Mostrar el diálogo
        dialog.show();
    }

    private boolean isGameWon() {
        for (int[] ints : board) {
            for (int j = 0; j < ints.length; j++) {
                if (ints[j] == 2048) {
                    return true;
                }
            }
        }
        return false;
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









