package cn.edu.cqu.studentlist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import cn.edu.cqu.studentlist.util.CSVUtil;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context mContext;

    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    private static final String CREATE_STUDENT = "create table student(" +
            "id varchar(30) primary key," +
            "name varchar(30) not null," +
            "gender varchar(6) not null," +
            "grade varchar(4) not null," +
            "major varchar(30) not null," +
            "class varchar(3) not null" +
            ");";
    private static final String CREATE_STROKE = "create table stroke(" +
            "character varchar(1) primary key," +
            "stroke_num integer not null" +
            ");";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_STUDENT);
        sqLiteDatabase.execSQL(CREATE_STROKE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
