package cn.edu.cqu.studentlist.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import cn.edu.cqu.studentlist.DatabaseHelper;

import static android.content.ContentValues.TAG;

public class JSoupUtil {
    static String url = "https://hanyu.baidu.com/zici/s?wd=%s";
    // 防止重复插入
    static String sql = "insert or ignore into stroke values('%s',%d)";
    public static int getStroke(String character){
        try {
            Log.i(TAG, "getStroke: "+character+" await");
            Document doc = Jsoup.connect(String.format(url,character)).timeout(60000).get();
            // 有时候网络不好或者频繁访问的时候会导致无法进入网页，解决方法就是多连接几次。。
            while (doc.getElementById("stroke_count") == null) {
                doc = Jsoup.connect(String.format(url,character)).timeout(600000).get();
            }
            Elements elements = doc.getElementById("stroke_count").getElementsByTag("span");
            Element element = elements.get(0);
            return Integer.parseInt(element.text());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 插入单个字
    public static void insertStroke(String character, Context context){
        DatabaseHelper helper = new DatabaseHelper(context,"studentlist",null,1);
        SQLiteDatabase db = helper.getWritableDatabase();
        int strokeNum = getStroke(character);
        db.execSQL(String.format(sql,character,strokeNum));
        db.close();
        Log.i(TAG, "insertStroke: insert "+character+" stroke="+strokeNum);
    }

    // 初始化时批量插入
    public static void insertStroke(Set<String> characterSet, Context context){
        List<String> characterList = new ArrayList<>(characterSet);
        DatabaseHelper helper = new DatabaseHelper(context,"studentlist",null,1);
        SQLiteDatabase db = helper.getWritableDatabase();
        for (int i = 0; i < characterSet.size(); i++){
            String character = characterList.get(i);
            int strokeNum = getStroke(character);
            db.execSQL(String.format(sql,character,strokeNum));
            Log.i(TAG, "insertStroke: insert "+character+" stroke="+strokeNum);
        }
        db.close();
    }
//    public static void main(String [] args) throws IOException {
//        System.out.println(getStroke("黑"));
//    }
}
