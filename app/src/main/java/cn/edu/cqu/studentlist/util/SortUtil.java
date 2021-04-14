package cn.edu.cqu.studentlist.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.cqu.studentlist.DatabaseHelper;
import cn.edu.cqu.studentlist.Student;

import static android.content.ContentValues.TAG;

public class SortUtil {
    public static void sortByIdAsc(List<Student> list){
        Collections.sort(list, new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                return Integer.parseInt(s1.getId())-Integer.parseInt(s2.getId());
            }
        });
    }
    public static void sortByIdDes(List<Student> list){
        Collections.sort(list, new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                return Integer.parseInt(s2.getId())-Integer.parseInt(s1.getId());
            }
        });
    }

    public static void sortByClassAsc(List<Student> list){
        Collections.sort(list, new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                String grade1 = s1.getGrade(), grade2 = s2.getGrade(),
                        major1 = s1.getMajor(), major2 = s2.getMajor(),
                        class1 = s1.getClassNumber(), class2 = s2.getClassNumber();
                if (!grade1.equals(grade2)) return grade1.compareTo(grade2);
                if (!major1.equals(major2)) return major1.compareTo(major2);
                return class1.compareTo(class2);
            }
        });
    }
    public static void sortByClassDes(List<Student> list){
        Collections.sort(list, new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                String grade1 = s1.getGrade(), grade2 = s2.getGrade(),
                        major1 = s1.getMajor(), major2 = s2.getMajor(),
                        class1 = s1.getClassNumber(), class2 = s2.getClassNumber();
                if (!grade1.equals(grade2)) return grade2.compareTo(grade1);
                if (!major1.equals(major2)) return major2.compareTo(major1);
                return class2.compareTo(class1);
            }
        });
    }

    // 按姓名笔画排序！从第一个字开始比较
    // 添加一个学生的信息的时候就把他名字里的所有字的笔画从网上下载（如果在数据库中没有查询到相应的字的笔画
    // 此处是直接开始比较
    static class StrokeComparator implements Comparator<Student> {
        String sql = "select stroke_num from stroke where character='%s'";
        Context mContext;
        boolean mIsAsc;
        Map<String,Integer> strokeMap = new HashMap<>();
        public StrokeComparator(Context context, boolean isAsc){
            mContext = context;
            mIsAsc = isAsc;
            DatabaseHelper helper = new DatabaseHelper(mContext,"studentlist",null,1);
            SQLiteDatabase db = helper.getReadableDatabase();
            String rawSQL = "select * from stroke";
            Cursor cursor = db.rawQuery(rawSQL,null);
            if (cursor.moveToFirst()){
                do{
                    String character = cursor.getString(cursor.getColumnIndex("character"));
                    int stroke_num = cursor.getInt(cursor.getColumnIndex("stroke_num"));
                    strokeMap.put(character,stroke_num);
                } while(cursor.moveToNext());
            }
            Log.i(TAG, "StrokeComparator: done into map");
            cursor.close();
            db.close();
        }

        @Override
        public int compare(Student s1, Student s2) {
            int len = Math.min(s1.getName().length(), s2.getName().length());
            for (int i = 0; i < len; i++){
                int stroke1 = strokeMap.get(String.valueOf(s1.getName().charAt(i)));
                int stroke2 = strokeMap.get(String.valueOf(s2.getName().charAt(i)));
                if (stroke1 != stroke2) {
                    return mIsAsc ? (stroke1 - stroke2) : (stroke2 - stroke1);
                }
            }
            return 0;
        }
    }
    public static void sortByNameAsc(List<Student> list, Context context){
        Collections.sort(list,new StrokeComparator(context,true));
    }
    public static void sortByNameDes(List<Student> list, Context context){
        Collections.sort(list, new StrokeComparator(context,false));
    }

}
