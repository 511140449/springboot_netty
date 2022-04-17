package com.lp.nettyserver.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author yangguang
 * @Datetime 2022/4/2 14:07
 */
@Aspect
@Component
public class MyLogAspect {
    private final Logger log = LoggerFactory.getLogger(MyLogAspect.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 其中@Pointcut声明了切点（这里的切点是我们自定义的注解类），
     */
    @Pointcut("@within(com.lp.nettyserver.annotation.MyLog)")
    private void pointcut() {}

    /**
    //当想获得注解里面的属性，可以直接注入改注解
    //方法可以带参数，可以同时设置多个方法用&&
     * @Before声明了通知内容，在具体的通知中，我们通过@annotation(logger)拿到了自定义的注解对象，
     * 所以就能够获取我们在使用注解时赋予的值了。
     * */
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        //类名
        String className=joinPoint.getSignature().getDeclaringType().getSimpleName();
        //方法名
        String modName= joinPoint.getSignature().getName();
        log.info("lp:{}",className);
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


    @After("pointcut()")
    public void after() {
        System.out.println("After");
    }

    //配置后置返回通知,使用在方法aspect()上注册的切入点
    @AfterReturning(pointcut = "pointcut()",returning = "rtnObj")
    public Object afterReturn(JoinPoint joinPoint,Object rtnObj){
        if(log.isInfoEnabled()){
            log.info("---------afterReturn " + joinPoint);
        }
        if(rtnObj==null){
            rtnObj = joinPoint.getThis();
        }
        return rtnObj;
    }

    //配置抛出异常后通知,使用在方法aspect()上注册的切入点
    @AfterThrowing(pointcut="pointcut()", throwing="ex")
    public void afterThrow(JoinPoint joinPoint, Exception ex){
        if(log.isInfoEnabled()){
            log.info("-----------afterThrow " + joinPoint + "\t" + ex.getMessage());
        }
    }
}
