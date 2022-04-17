package com.lp.nettyserver.controller;

import com.lp.nettyserver.data.entity.Student;
import com.lp.nettyserver.data.service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yangguang
 * @DateTime 2022/4/6 11:26
 */
@RestController
@RequestMapping("student")
public class MongoDbController {
    @Autowired
    private IStudentService studentService;

    /** 查询信息列表 */
    @PostMapping("/list")
    @ResponseBody
    public List<Student> list(Student student) {
        List<Student> students = studentService.findtimeByPage(student);
        return students;
    }

    /** 查询信息列表 */
    @PostMapping("/add")
    @ResponseBody
    public int add(Student student) {
        int i = studentService.insertStudent(student);
        return i;
    }
}
