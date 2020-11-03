package com.unt.jerin.genreclassifier1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class PredictionsStoreDB extends SQLiteOpenHelper {


    public PredictionsStoreDB(Context context) {
        super(context, "PredictionData.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table PREDICTIONS (filename TEXT primary key, prediction TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists PREDICTIONS ");
    }

    public Boolean insertPredictionData(String filename, String prediction){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("filename", filename);
        contentValues.put("prediction", prediction);

        long result = db.insert("PREDICTIONS", null, contentValues);

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }


    public Boolean updatePredictionData(String filename, String prediction){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("filename", filename);
        contentValues.put("prediction", prediction);

        Cursor cursor = db.rawQuery("select * from PREDICTIONS where name = ?", new String[]{filename});

        if(cursor.getCount() > 0) {
            long result = db.update("PREDICTIONS", contentValues, "filename=?", new String[]{filename});

            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }else{
            return false;
        }
    }


    public Boolean deletePredictionData(String filename) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from PREDICTIONS where name = ?", new String[]{filename});

        if (cursor.getCount() > 0) {
            long result = db.delete("PREDICTIONS", "filename=?", new String[]{filename});

            if (result == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }


    public Boolean deleteAllPredictionData() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from PREDICTIONS ", null);

        if (cursor.getCount() > 0) {
            long result = db.delete("PREDICTIONS", null, null);

            if (result == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }


    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from PREDICTIONS order by filename desc ", null);
        return cursor;
    }


}
