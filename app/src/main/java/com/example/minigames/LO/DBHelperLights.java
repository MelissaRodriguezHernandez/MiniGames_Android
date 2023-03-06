package com.example.minigames.LO;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelperLights extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "scores_lights";
    private static final String TABLE_NAME = "scores_lights";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_CLICKS = "clicks";
    private static final String COLUMN_DIFFICULTY = "difficulty";
    private static final String COLUMN_SECONDS = "seconds";


    public DBHelperLights(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear la tabla
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_TIME + " TEXT,"
                + COLUMN_CLICKS + " INTEGER,"
                + COLUMN_DIFFICULTY + " TEXT,"
                + COLUMN_SECONDS+ " INTEGER" +")";
        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Actualizar la tabla si es necesario
        String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    public void insertScore(String name, String time, String clicks, String difficulty, String seconds) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_CLICKS, clicks);
        values.put(COLUMN_DIFFICULTY, difficulty);
        values.put(COLUMN_SECONDS, seconds);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    @SuppressLint("Range")
    public ArrayList<String> getScoresWithRanking() {
        ArrayList<String> scores = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_DIFFICULTY + " ASC, " + COLUMN_SECONDS + " ASC", null);

        if (cursor.moveToFirst()) {
            int previousDifficulty = -1;

            do {
                int difficulty = cursor.getInt(cursor.getColumnIndex(COLUMN_DIFFICULTY));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                String time = cursor.getString(cursor.getColumnIndex(COLUMN_TIME));
                int clicks = cursor.getInt(cursor.getColumnIndex(COLUMN_CLICKS));
                String scoreString = name + ", " + time + ", " + clicks;

                if (previousDifficulty != difficulty) {
                    scores.add("");
                    switch (difficulty) {
                        case 4:
                            scores.add("Easy");
                            break;
                        case 5:
                            scores.add("Medium");
                            break;
                        case 6:
                            scores.add("Hard");
                            break;
                    }
                    scores.add("");
                    previousDifficulty = difficulty;

                }
                scores.add(scoreString);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return scores;
    }


}

