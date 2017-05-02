package com.teezom.sqlHelpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "teezom.db";
    private static final int DATABASE_VERSION = 1;

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table library(" +
                "id integer primary key autoincrement, " +
                "tId integer, " +
                "UserId integer, " +
                "FileUrl text," +
                "type integer," +
                "tDate text," +
                "Amount double," +
                "VendorName text," +
                "ExpenseTypeName text," +
                "Hours integer," +
                "HourlyRate double," +
                "cDate text)"
        );

        db.execSQL("create table records(" +
                "id integer primary key autoincrement," +
                "progress_name text," +
                "progress_value integer," +
                "month text, " +
                "month_no int, " +
                "year int)"
        );

        db.execSQL(
                "create table transactions(" +
                        "id integer primary key autoincrement," +
                        "type text," +
                        "date int," +
                        "month int," +
                        "year int," +
                        "amount integer," +
                        "client_vendor text," +
                        "doc text)"
        );

        db.execSQL("create table appusers(" +
                "id integer primary key autoincrement, " +
                "username text, " +
                "email text, " +
                "password text)"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists records");
        db.execSQL("drop table if exists transactions");
        db.execSQL("drop table if exists appusers");
        db.execSQL("drop table if exists getuserlibrary");
        onCreate(db);
    }

}
