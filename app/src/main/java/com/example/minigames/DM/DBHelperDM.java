package com.example.minigames.DM;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelperDM extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "scores_db";
    private static final String TABLE_NAME = "scores";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SCORE = "score";

    public DBHelperDM(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear la tabla
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_SCORE + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Actualizar la tabla si es necesario
        String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    public void insertScore(String name, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_SCORE, score);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public int getHighScore() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_SCORE}, null, null, null, null, COLUMN_SCORE + " DESC", "1");

        int highScore = -1;
        if (cursor.moveToFirst()) {
            highScore = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return highScore;
    }

    @SuppressLint("Range")
    public ArrayList<String> getScoresWithRanking() {
        ArrayList<String> scores = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " +
                "(SELECT COUNT(*) + 1 FROM " + TABLE_NAME + " AS a WHERE a." + COLUMN_SCORE + " > b." + COLUMN_SCORE + ") AS rank, " +
                COLUMN_NAME + ", " +
                COLUMN_SCORE +
                " FROM " + TABLE_NAME + " b " +
                "ORDER BY " + COLUMN_SCORE + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                int rank = cursor.getInt(cursor.getColumnIndex("rank"));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                int score = cursor.getInt(cursor.getColumnIndex(COLUMN_SCORE));
                String scoreString = rank + ". " + name + ": " + score;
                scores.add(scoreString);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return scores;
    }
}
