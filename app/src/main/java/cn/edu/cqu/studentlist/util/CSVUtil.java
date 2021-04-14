package cn.edu.cqu.studentlist.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import cn.edu.cqu.studentlist.DatabaseHelper;
import cn.edu.cqu.studentlist.MainActivity;

import static android.content.ContentValues.TAG;

public class CSVUtil {
    public static Set<String> readCSV(final Context context){
        try {
            Set<String> characterSet = new HashSet<String>();
            DatabaseHelper helper = new DatabaseHelper(context,"studentlist",null,1);
            SQLiteDatabase db = helper.getWritableDatabase();
            InputStream inputStream = context.getResources().getAssets().open("student_list.csv");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"GBK");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine(); // 先读第一行的列名
            while((line = bufferedReader.readLine())!=null){
                String sql = "insert or ignore into student values('%s','%s','%s','%s','%s','%s')";
//                Log.i(TAG, "readCSV: line "+line);
                String [] l = line.split(",");
                String grade = "20"+l[1].substring(0,2);
                String major = l[1].substring(2,5);
                String classNum = l[1].substring(5);
                String id = l[2];
                String name = l[3];
                String gender = l[4].equals("男") ? "male" : "female";
                db.execSQL(String.format(sql,id,name,gender,grade,major,classNum));
                for (int i = 0; i < name.length(); i++){
                    if (String.valueOf(name.charAt(i)).equals("·")) continue;
                    characterSet.add(String.valueOf(name.charAt(i)));
                }
            }
            db.close();
            return characterSet;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
