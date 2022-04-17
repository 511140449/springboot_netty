package com.lp.nettyserver.data.service;

import com.lp.nettyserver.data.entity.Student;

import java.util.List;

/**
 * @author yangguang
 * @DateTime 2022/4/6 11:11
 */
public interface IStudentService {
    int insertStudent(Student student);

    int updateStudent(Student student);

    int removeStudent(Long id);

    Student findOne(Student student);

    List<Student> findlike(Student student);

    List<Student> findmore(Student student);

    List<Student> findtime(Student student);

    List<Student> findtimeByPage(Student student);

}
