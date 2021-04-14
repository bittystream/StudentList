package cn.edu.cqu.studentlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class StudentAdapter extends BaseAdapter {

    Context mContext;
    List<Student> mList;

    public StudentAdapter(Context context, List<Student> list) {
        mContext = context;
        mList = list;
    }


    @Override
    public int getCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        if (mList == null) return null;
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(mContext).inflate(R.layout.student_item,viewGroup,false);
        TextView studentId = view.findViewById(R.id.id),
                 studentName = view.findViewById(R.id.name),
                 studentGender = view.findViewById(R.id.gender),
                 studentGradeMajorClass = view.findViewById(R.id.grade_major_class);

        studentId.setText(mList.get(i).getId());
        studentName.setText(mList.get(i).getName());
        int genderId = mContext.getResources().getIdentifier(mList.get(i).getGender(),"string",mContext.getPackageName());
        studentGender.setText(mContext.getResources().getText(genderId));
        String gradeMajorClass = mList.get(i).getGrade()+"级"+mList.get(i).getMajor()+mList.get(i).getClassNumber()+"班";
        studentGradeMajorClass.setText(gradeMajorClass);

        return view;
    }
}
