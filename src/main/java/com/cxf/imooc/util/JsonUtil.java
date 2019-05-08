package com.cxf.imooc.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * @author ：XueFF
 * @date ：Created in 2019/5/7 10:17
 * @description：自定义Json工具类
 */

public class JsonUtil {
    public static ObjectMapper mapper = new ObjectMapper();

    static {
        // 转换为格式化的json
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // 如果json中有新增的字段并且是实体类类中不存在的，不报错
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }




    public static String ObjectToJson(Object object){
        String json = null;
        try {
            json = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static <T> T JsonToObject(String json, Class<T>valueType){
        T obj = null;
        try {
            obj  = mapper.readValue(json,valueType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static Map<String ,Object> JsonToMap(String json){
        Map<String ,Object> map = null;
        try {
            map  = mapper.readValue(json,Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
