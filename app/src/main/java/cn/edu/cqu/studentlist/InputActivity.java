package cn.edu.cqu.studentlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteTransactionListener;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import cn.edu.cqu.studentlist.util.JSoupUtil;
import cn.edu.cqu.studentlist.util.SortUtil;

public class InputActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "InputActivity";
    static String INSERT_STROKE_SQL = "insert or ignore into stroke values('%s',%d)";

    String studentId, studentName, studentGrade, studentMajor, studentClass, studentGender;
    TextView inputId, inputName, inputGrade, inputMajor, inputClass;
    RadioGroup inputGender;
    Button submitButton;
    boolean isFromMain;
    String id;

    Context mContext;


    void init(){
        mContext = this;
        inputId = findViewById(R.id.input_id);
        inputName = findViewById(R.id.input_name);
        inputGrade = findViewById(R.id.input_grade);
        inputMajor = findViewById(R.id.input_major);
        inputClass = findViewById(R.id.input_class);
        inputGender = findViewById(R.id.input_gender);
        submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(this);
        inputGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.input_male) studentGender = "male";
                else studentGender = "female";
            }
        });
    }

    void getEditText(){
        studentId = inputId.getText().toString();
        studentName = inputName.getText().toString();
        studentGrade = inputGrade.getText().toString();
        studentMajor = inputMajor.getText().toString();
        studentClass = inputClass.getText().toString();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        init();

        if (getIntent().getExtras() != null && getIntent().getExtras().get("id") != null) {
            Log.i(TAG, "onCreate: 修改");
            isFromMain = true;
            fillView();
        }
        else isFromMain = false;
    }

    void fillView(){
        id = getIntent().getExtras().get("id").toString();
        Log.i(TAG, "fillView: id="+id);
        String sql = "select * from student where id='%s'";
        DatabaseHelper helper = new DatabaseHelper(this,"studentlist",null,1);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery(String.format(sql,id),null);
        if (cursor.moveToFirst()){
            do {
                studentName = cursor.getString(cursor.getColumnIndex("name"));
                studentGender = cursor.getString(cursor.getColumnIndex("gender"));
                studentGrade = cursor.getString(cursor.getColumnIndex("grade"));
                studentMajor = cursor.getString(cursor.getColumnIndex("major"));
                studentClass = cursor.getString(cursor.getColumnIndex("class"));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        inputId.setText(id);
        inputName.setText(studentName);
        inputGender.check(studentGender.equals("male") ? R.id.input_male : R.id.input_female);
        inputGrade.setText(studentGrade);
        inputMajor.setText(studentMajor);
        inputClass.setText(studentClass);

    }

    @Override
    public void onClick(View view) {
        getEditText();
        if (studentName == null || studentName.length() < 1){
            Toast.makeText(this, "还未填写姓名！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (studentGender == null || studentGender.length() < 1){
            Toast.makeText(this,"还未选择性别！",Toast.LENGTH_SHORT).show();
            return;
        }
        if (studentGrade == null || studentGrade.length() < 1){
            Toast.makeText(this, "还未填写年级！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (studentMajor == null || studentMajor.length() < 1){
            Toast.makeText(this,"还未填写专业！",Toast.LENGTH_SHORT).show();
            return;
        }
        if (studentClass == null || studentClass.length() < 1){
            Toast.makeText(this,"还未填写班级！",Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(){
            @Override
            public void run() {
                for (int i = 0; i < studentName.length(); i++){
                    JSoupUtil.insertStroke(String.valueOf(studentName.charAt(i)),mContext);
                }
            }
        }.start();


        DatabaseHelper helper =  new DatabaseHelper(this,"studentlist",null,1);
        SQLiteDatabase db = helper.getWritableDatabase();
        if (isFromMain) {
            String sql = "delete from student where id='%s'";
            db.execSQL(String.format(sql,id));
        }
        ContentValues values = new ContentValues();
        values.put("id",studentId);
        values.put("name",studentName);
        values.put("gender",studentGender);
        values.put("grade",studentGrade);
        values.put("major",studentMajor);
        values.put("class",studentClass);
        db.insert("student",null,values);
        db.close();
        String text2Make = "添加成功！";
        if (isFromMain) text2Make = "修改成功！";
        Toast.makeText(this,text2Make,Toast.LENGTH_SHORT).show();
        finish();
    }
}