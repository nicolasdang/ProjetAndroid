package com.theyangui.projetandroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static org.junit.Assert.*;

/**
 * Created by Nicolas on 17/01/2018.
 */


public class modelTest extends SQLiteOpenHelper{
    String taf = "ModelTest";
    public modelTest(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}