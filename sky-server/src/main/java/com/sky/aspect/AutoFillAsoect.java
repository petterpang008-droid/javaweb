package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAsoect {
    //切入点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) &&@annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }
    //前置通知 为公共字段赋值
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("进行公共字段赋值");

        //获取被拦截方法数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名
        AutoFill autoFill=signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的AutoFill注解
        OperationType operationType = autoFill.value();//获取注解中的操作类型

        //获取方法的实体参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            log.info("方法没有参数");
            return;
        }
        Object entity = args[0];

        //为公共属性赋值--反射
        LocalDateTime now = LocalDateTime.now();
        Long currentId= BaseContext.getCurrentId();

        if (operationType==OperationType.INSERT){
            //4个公共字段
            try {
                Method setCreateTime = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else if (operationType==OperationType.UPDATE){
            //两个
            try {
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }




    }

}
