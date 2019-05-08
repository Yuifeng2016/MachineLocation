package com.cxf.imooc.service;

import com.cxf.imooc.dto.MachineDto;
import com.cxf.imooc.entity.MachineRealTimeLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ：XueFF
 * @date ：Created in 2019/4/27 21:20
 * @description：实时位置在redis中操作业务类
 */
@Service
public class LocationRedisService {
    private static Logger logger = LoggerFactory.getLogger(LocationRedisService.class);
    private static String MACHINE_KEY = "south:machine:Location:";
    private static String MACHINE_KEYS_NAME = "south:machine:Location:keys";

    @Value("${webSocket.expire-minutes}")
    private  int expireMinutes = 5;


    @Autowired
    private RedisTemplate<String, String> redisCacheTemplate;

    private ObjectMapper mapper  = new ObjectMapper();


    /**
     * 保存位置记录
     * @param text
     * @param id
     */
    public void saveToRedis(String text, String id) {

        // 3.2通过json格式校验，开始执行保存,保存前判断是存在记录，若存在则删除
        String locationKey = MACHINE_KEY + id;
        logger.info(locationKey);
//        String tempLocationJson = redisCacheTemplate.opsForValue().get(locationKey);
//        if (tempLocationJson != null && tempLocationJson != ""){
//            redisCacheTemplate.delete(locationKey);
//        }
        // 3.3保存location 与 对应的key
        // 3.1获取过期时间
        Long nowSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));    //秒
        redisCacheTemplate.opsForValue().set(locationKey,text, expireMinutes,TimeUnit.MINUTES);    //保存记录

        redisCacheTemplate.opsForZSet().add(MACHINE_KEYS_NAME,locationKey,nowSecond);               //保存键
    }

    /**
     * 查询位置记录实际逻辑
     * @return
     */
    public  List<MachineRealTimeLocation> getMachineRealTimeLocationList()  {
        // 3.1获取过期时间
        LocalDateTime localDateTime =LocalDateTime.now();
        Long nowSecond = localDateTime.toEpochSecond(ZoneOffset.of("+8"));    //秒
        //logger.info("过期时间{}分钟",expireMinutes);
        Long beforeSecond = nowSecond - expireMinutes * 60 ;
        // 4.读取最新的数据返回给客户端
        Set<String> keys = redisCacheTemplate.opsForZSet().rangeByScore(MACHINE_KEYS_NAME, beforeSecond, nowSecond);
        List<MachineRealTimeLocation> locationsList = new ArrayList<>();
        for (String key:keys) {
            String locationJson = redisCacheTemplate.opsForValue().get(key);
            if (locationJson == null){
                logger.info("locationJson is null");
                continue;
            }
            MachineRealTimeLocation location1 = null;
            try {
                location1 = mapper.readValue(locationJson, MachineRealTimeLocation.class);
                locationsList.add(location1);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return locationsList;
    }

    /**
     * 根据用户名查询位置记录
     * @param username
     * @return
     */
    public  List<MachineRealTimeLocation> getMachineRealTimeLocationListByUserId(String username)  {
        List<MachineRealTimeLocation> list = getMachineRealTimeLocationList().stream().filter(location->{
            return location.getUsername().equalsIgnoreCase(username);
        }).collect(Collectors.toList());


        return list;
    }

    /**
     * 根据项目id查询位置记录,同时也需要用户名
     * @param programId
     * @param programId
     * @return
     */
    public  List<MachineRealTimeLocation> getMachineRealTimeLocationListByProgramId(String username,String programId)  {
        List<MachineRealTimeLocation> list = getMachineRealTimeLocationList();
        list = list.stream().filter(location-> location.getProgramId().equalsIgnoreCase(programId)).collect(Collectors.toList()); //查询指定项目
        list = list.stream().filter(location -> location.getMember().contains(username)).collect(Collectors.toList()); //判断用户是否在该项目


        return list;
    }


    /**
     * 根据时间区间获取位置信息并封装到一个map中
     * @param params 过滤参数
     * @return
     */
    public Map<String,Object> getMachineRealTimeLocations(Map<String,String> params)  {
        List<MachineRealTimeLocation> locationList= null;
        if (params == null){
            //查询所有
            locationList = getMachineRealTimeLocationList();
        }else {
            //此处通过项目id来查询
            if (params.get("programId")!= null){

                locationList = getMachineRealTimeLocationListByProgramId(params.get("userId"),params.get("programId"));
            }else if (params.get("userId")!=null){
                locationList = getMachineRealTimeLocationListByUserId(params.get("userId"));
            }

        }

        Map<String,Object> returnMap = new HashMap<>();
        if (locationList.size() == 0){
            return returnMap;
        }
        List<MachineDto> locationDto = locationList.stream().map(location ->{
            MachineDto dto = new MachineDto();
            BeanUtils.copyProperties(location,dto);
            return dto;
        }).collect(Collectors.toList());
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        returnMap.put("serverTime",localDateTime.format(formatter));
        returnMap.put("activeMachine",locationDto);
        //logger.info(returnMap.toString());
        return returnMap;
    }


    /**
     * 查询指定的记录,并转化为Json字符串
     * @param params
     * @return
     */
    public String  getMachineRealTimeLocationsJson(Map<String,String> params){

        String msgToClient = null;
        try {
            msgToClient = mapper.writeValueAsString(getMachineRealTimeLocations(params));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return msgToClient;
    }

}
