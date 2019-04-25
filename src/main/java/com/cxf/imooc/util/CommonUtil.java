package com.cxf.imooc.util;

import com.cxf.imooc.netty.handler.ChatHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author ：XueFF
 * @date ：Created in 2019/4/25 15:46
 * @description：通用工具类
 */
public class CommonUtil {
    private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);
    /**
     * 判断对象及值是否为空
     * @param obj
     * @return
     */
    public static boolean isEmpty(Object obj) {
        if (null == obj) {
            return true;
        }
        if (obj instanceof String) {
            return ((String) obj).trim().isEmpty();
        }

        return !((obj instanceof Number)
                || (obj instanceof Date)
        );
    }

    /**
     * 判断多个对象及值是否为空
     * @param objects
     * @return
     */
    public static boolean isHasEmpty(Object... objects) {
        boolean b = false;
        //循环判断是否有空值，若有直接返回
        for (Object object: objects) {
            if(isEmpty(object)){
                b = true;

            }
        }
        return b;
    }


    public static boolean checkObjFieldIsNull(Object obj,String ... exclusions) throws IllegalAccessException {

        boolean flag = false;
        List<String> list = Arrays.asList(exclusions);
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            logger.info(f.getName());
            Object fileObj = f.get(obj);
            if (list.contains(f.getName())){
                continue;
            }
            if (fileObj == null || fileObj.equals("")) {
                flag = true;
                return flag;
            }
        }
        return flag;
    }

}
