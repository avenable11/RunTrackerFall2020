package edu.ivytech.runtrackerfall2020.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static edu.ivytech.runtrackerfall2020.database.RunDBSchema.LocationTable.*;

public class RunBaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "runs.db";
    public static final int DB_VERSION = 1;

    public RunBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + NAME + "(" +
                Cols.LOCATION_ID + " integer primary key autoincrement, " +
                Cols.LOCATION_LATITUDE + ", " +
                Cols.LOCATION_LONGITUDE + ", " +
                Cols.LOCATION_TIME + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

