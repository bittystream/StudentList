package cn.edu.cqu.studentlist;

public class Student {

    String _id; // 学号
    String _name; // 姓名
    String _gender; // 性别 male or female
    String _grade; // 年级
    String _major; // 专业
    String _class; // 班级

    public Student(String id, String name, String gender, String grade, String major, String c){
        _id = id;
        _name = name;
        _gender = gender;
        _grade = grade;
        _major = major;
        _class = c;
    }
    public String getId() { return _id;} // 学号排序
    public String getName() { return _name;} // 姓名（笔画）排序
    public String getGender() { return _gender;}
    public String getGrade() { return _grade;}
    public String getMajor() { return _major;}
    public String getClassNumber() { return _class;}
}
