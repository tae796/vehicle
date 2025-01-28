package com.example.vehicle.aop;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Slf4j
@Aspect
@RequiredArgsConstructor
public class ControllerLoggingAspect {

    @Pointcut("execution(* com.example.vehicle.controller..*.*(..))")
    private void cut() {}

    @Before("cut()")
    private void whenRequestArrived(JoinPoint joinPoint) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String controllerName = joinPoint.getSignature().getDeclaringType().getName();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String lineSep = System.lineSeparator();
        StringBuilder logSb = new StringBuilder(lineSep);
        logSb.append("============= REQUEST START ===========").append(lineSep);
        logSb.append("Request Url    : ").append(request.getRequestURL()).append(lineSep);
        logSb.append("Request Method : ").append(request.getMethod()).append(lineSep);
        logSb.append("Controller     : ").append(controllerName).append(lineSep);
        logSb.append("Service     : ").append(signature.getMethod().getName()).append(lineSep);
        logSb.append("============= REQUEST END ===========").append(lineSep);
        log.info(logSb.toString());

    }
    @AfterReturning(value = "cut()", returning = "responseObj")
    private void whenRequestArrived(JoinPoint joinPoint, Object responseObj) throws Exception {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

        String lineSep = System.lineSeparator();
        StringBuilder logSb = new StringBuilder(lineSep);
        logSb.append("============= RESPONSE START ===========").append(lineSep);
        logSb.append("Response Status     : ").append(response.getStatus()).append(lineSep);
        logSb.append("Response Body     : ").append(responseObj).append(lineSep);
        logSb.append("============= RESPONSE END ===========").append(lineSep);
        log.info(logSb.toString());
    }

    @AfterThrowing(value = "cut()", throwing = "exception")
    private void whenRequestExceptionArrived(JoinPoint joinPoint, Exception exception) throws Throwable {
        String lineSep = System.lineSeparator();
        StringBuilder logSb = new StringBuilder(lineSep);
        logSb.append("+++++++++++++ !!ERROR!! START +++++++++++++").append(lineSep);
        logSb.append("Error Method     : ").append(joinPoint.getSignature()).append(lineSep);
        logSb.append("Error Message     : ").append(exception.getMessage()).append(lineSep);
        logSb.append("Exception Class Name     : ").append(exception.getClass().getSimpleName()).append(lineSep);
        logSb.append("------Detail Error Message------").append(lineSep);
        for(int i = 0; i< exception.getStackTrace().length; i++) {
            logSb.append('\t').append(exception.getStackTrace()[i]).append(lineSep);
        }
        logSb.append("+++++++++++++ !!ERROR!! END +++++++++++++").append(lineSep);
        log.error(logSb.toString());
    }

}
