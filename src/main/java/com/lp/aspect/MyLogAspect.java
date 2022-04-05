package com.lp.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lp.netty.service.DiscardService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author yangguang
 * @DateTime 2022/4/2 14:07
 */
@Aspect
@Component
public class MyLogAspect {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 其中@Pointcut声明了切点（这里的切点是我们自定义的注解类），
     * @Before声明了通知内容，在具体的通知中，我们通过@annotation(logger)拿到了自定义的注解对象，
     * 所以就能够获取我们在使用注解时赋予的值了。
     */
    @Pointcut("@annotation(com.lp.annotation.MyLog)")
    private void pointcut() { }

    /**
     * 定制一个环绕通知
     * @param joinPoint
     */
    @Around("pointcut()")
    public void advice(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Around Begin");
        joinPoint.proceed();//执行到这里开始走进来的方法体（必须声明）
        System.out.println("Around End");
    }

    //当想获得注解里面的属性，可以直接注入改注解
    //方法可以带参数，可以同时设置多个方法用&&
//    @Before("pointcut()")
//    public void record(JoinPoint joinPoint) {
//        System.out.println("Before");
//    }
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        //类名
        String className=joinPoint.getSignature().getDeclaringType().getSimpleName();
        //方法名
        String modName= joinPoint.getSignature().getName();
        Class pojo = joinPoint.getSignature().getDeclaringType();

        Logger log = LoggerFactory.getLogger(pojo);
        log.info("lp");
        //参数
        Object[] args = joinPoint.getArgs();
        StringBuffer result = new StringBuffer();
        result.append("["+className+"]");
        result.append("["+modName+"]");
        Arrays.stream(args).forEach(arg->{
            try {
                result.append("["+OBJECT_MAPPER.writeValueAsString(arg)+"]");
            } catch (JsonProcessingException e) {

            }
        });
        System.out.println(result.toString());
    }



    @After("pointcut()")
    public void after() {
        System.out.println("After");
    }
}
