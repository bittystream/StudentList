package cn.edu.cqu.studentlist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.edu.cqu.studentlist.util.CSVUtil;

import cn.edu.cqu.studentlist.util.JSoupUtil;
import cn.edu.cqu.studentlist.util.SortUtil;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    // 是否已经初始化过了
    static boolean isInitialized = false;

    Context mContext;
    FloatingActionButton fab;
    View tab;
    TextView sortById, sortByName, sortByClass, sortByIdAsc, sortByIdDes, sortByNameAsc, sortByNameDes, sortByClassAsc, sortByClassDes;
    ListView listView;
    List<Student> list;
    Set<String> characterSet;
    StudentAdapter adapter;
    int colorUnselected, colorSelected, colorNotAllowed;
    int sortedById, sortedByName, sortedByClass; // 0 - 未按该规则排序； 1 - 按该规则升序排序； 2 - 按该规则降序排序
    int position; // 长按产生上下文菜单的item的位置序号
    Handler handler;

    static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg)
        {
            Log.i(TAG, "handleMessage: handler received msg from "+msg.obj);
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.enableSortByName();
                // 数据库初始化完成过后才可以点击按姓名排序
                isInitialized = true;
            }
        }
    }


    void init(){
        mContext = this;
        handler = new MyHandler(this);
        FrameLayout frameLayout = findViewById(R.id.frame_layout);
        tab = getLayoutInflater().inflate(R.layout.sort_tab,null);
        frameLayout.addView(tab,0);

        colorSelected = getResources().getColor(R.color.colorPrimary,null);
        colorUnselected = Color.GRAY;
        colorNotAllowed = Color.parseColor("#c0c0c0");

        sortedById = 1;
        sortedByName = 0;
        sortedByClass = 0;

        sortById = findViewById(R.id.sort_by_id);
        sortByIdAsc = findViewById(R.id.sort_by_id_asc);
        sortByIdDes = findViewById(R.id.sort_by_id_des);
        sortByClass = findViewById(R.id.sort_by_class);
        sortByClassAsc = findViewById(R.id.sort_by_class_asc);
        sortByClassDes = findViewById(R.id.sort_by_class_des);
        sortByName = findViewById(R.id.sort_by_name);
        sortByNameAsc = findViewById(R.id.sort_by_name_asc);
        sortByNameDes = findViewById(R.id.sort_by_name_des);
        listView = findViewById(R.id.list_view);
        fab = findViewById(R.id.fab);


        sortById.setTextColor(colorSelected);
        sortByIdAsc.setTextColor(colorSelected);

        fab.setOnClickListener(this);
        for (int i = 0; i < ((ViewGroup)tab).getChildCount(); i++){
            ((ViewGroup)tab).getChildAt(i).setOnClickListener(this);
        }
    }

    void disableSortByName(){
        sortByName.setTextColor(colorNotAllowed);
        sortByNameAsc.setTextColor(colorNotAllowed);
        sortByNameDes.setTextColor(colorNotAllowed);
        sortByName.setEnabled(false);
        sortByNameAsc.setEnabled(false);
        sortByNameDes.setEnabled(false);
    }

    void enableSortByName(){
        sortByName.setTextColor(colorUnselected);
        sortByNameAsc.setTextColor(colorUnselected);
        sortByNameDes.setTextColor(colorUnselected);
        sortByName.setEnabled(true);
        sortByNameAsc.setEnabled(true);
        sortByNameDes.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        if (!isInitialized){
            Log.i(TAG, "onCreate: have not been initialized yet");
            disableSortByName();
            characterSet = CSVUtil.readCSV(this);
            new Thread(){
                @Override
                public void run() {
                    JSoupUtil.insertStroke(characterSet,mContext);
                    Message msg = new Message();
                    msg.obj = "INITIALIZING DB";
                    handler.sendMessage(msg);
                }
            }.start();
        }

        list = new ArrayList<Student>();
        adapter = new StudentAdapter(this,list);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectData();
        Log.i(TAG, "onResume: "+list.size());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,0,0,"修改");
        menu.add(0,1,0,"删除");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        position = info.position;
        if (item.getItemId() == 1){
            // 删除
            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("确认删除？");
            builder.setItems(new String[]{"是", "否"}, new DeleteOnClickListener());
            builder.show();
        }
        else{
            // 修改
            Intent intent = new Intent(MainActivity.this,InputActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("id",list.get(position).getId());
            intent.putExtras(bundle);
            startActivity(intent);
        }
        return super.onContextItemSelected(item);
    }

    class DeleteOnClickListener implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == 0) deleteData(position);
        }
    }

    void deleteData(int position){
        Student student = list.get(position);
        String id = student.getId();
        String sql = "delete from student where id=" + id;
        DatabaseHelper helper = new DatabaseHelper(this,"studentlist",null,1);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(sql);
        db.close();
        list.remove(position);
        adapter.notifyDataSetChanged();
        Toast.makeText(this,"删除成功",Toast.LENGTH_SHORT).show();
    }

    void selectData(){
        DatabaseHelper helper = new DatabaseHelper(this,"studentlist",null,1);
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "select * from student";
        String id, name, gender, grade, major, classNum;
        Cursor cursor = db.rawQuery(sql,null);
        list.clear();
        if (cursor.moveToFirst()){
            do {
                id = cursor.getString(cursor.getColumnIndex("id"));
                name = cursor.getString(cursor.getColumnIndex("name"));
                gender = cursor.getString(cursor.getColumnIndex("gender"));
                grade = cursor.getString(cursor.getColumnIndex("grade"));
                major = cursor.getString(cursor.getColumnIndex("major"));
                classNum = cursor.getString(cursor.getColumnIndex("class"));
                list.add(new Student(id,name,gender,grade,major,classNum));
            } while(cursor.moveToNext());
        }
        else Log.i(TAG, "selectData: cursor is null");
        cursor.close();
        db.close();
        SortUtil.sortByIdAsc(list);
        clearSortTextColor();
        sortById.setTextColor(colorSelected);
        sortByIdAsc.setTextColor(colorSelected);
        adapter.notifyDataSetChanged();
    }

    void clearSortTextColor(){
        sortById.setTextColor(colorUnselected);
        sortByIdAsc.setTextColor(colorUnselected);
        sortByIdDes.setTextColor(colorUnselected);
        sortByClass.setTextColor(colorUnselected);
        sortByClassAsc.setTextColor(colorUnselected);
        sortByClassDes.setTextColor(colorUnselected);
        if (isInitialized) enableSortByName();
        else disableSortByName();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sort_by_id:
            case R.id.sort_by_id_asc:
            case R.id.sort_by_id_des:
                // 按学号排序
                clearSortTextColor();
                sortById.setTextColor(colorSelected);
                if (sortedById == 0){
                    sortedById = 1;
                    sortedByClass = 0;
                    sortedByName = 0;
                    sortByIdAsc.setTextColor(colorSelected);
                    SortUtil.sortByIdAsc(list);
                }
                else if (sortedById == 1){
                    sortedById = 2;
                    sortByIdDes.setTextColor(colorSelected);
                    SortUtil.sortByIdDes(list);
                }
                else{
                    sortedById = 1;
                    sortByIdAsc.setTextColor(colorSelected);
                    SortUtil.sortByIdAsc(list);
                }
                break;
            case R.id.sort_by_class:
            case R.id.sort_by_class_asc:
            case R.id.sort_by_class_des:
                // 按班级排序
                clearSortTextColor();
                sortByClass.setTextColor(colorSelected);
                if (sortedByClass == 0){
                    sortedByClass = 1;
                    sortedById = 0;
                    sortedByName = 0;
                    sortByClassAsc.setTextColor(colorSelected);
                    SortUtil.sortByClassAsc(list);
                }
                else if (sortedByClass == 1){
                    sortedByClass = 2;
                    sortByClassDes.setTextColor(colorSelected);
                    SortUtil.sortByClassDes(list);
                }
                else{
                    sortedByClass = 1;
                    sortByClassAsc.setTextColor(colorSelected);
                    SortUtil.sortByClassAsc(list);
                }
                break;
            case R.id.sort_by_name:
            case R.id.sort_by_name_asc:
            case R.id.sort_by_name_des:
                // 按姓名笔画排序
                clearSortTextColor();
                sortByName.setTextColor(colorSelected);
                if (sortedByName == 0){
                    sortedByName = 1;
                    sortedByClass = 0;
                    sortedById = 0;
                    sortByNameAsc.setTextColor(colorSelected);
                    SortUtil.sortByNameAsc(list,this);
                }
                else if (sortedByName == 1){
                    sortedByName = 2;
                    sortByNameDes.setTextColor(colorSelected);
                    SortUtil.sortByNameDes(list,this);
                }
                else{
                    sortedByName = 1;
                    sortByNameAsc.setTextColor(colorSelected);
                    SortUtil.sortByNameAsc(list,this);
                }
                break;
            case R.id.fab:
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
                break;
        }
        adapter.notifyDataSetChanged();
    }
}