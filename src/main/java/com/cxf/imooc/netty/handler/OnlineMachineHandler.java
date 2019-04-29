package com.cxf.imooc.netty.handler;

import com.cxf.imooc.entity.MachineRealTimeLocation;
import com.cxf.imooc.service.LocationRedisService;
import com.cxf.imooc.util.CommonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.*;

import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author ：yuifeng
 * @date ：Created in 2019/4/24 21:53
 * @description：在线桩机处理器
 * @version:
 */
public class OnlineMachineHandler extends  SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static Logger logger = LoggerFactory.getLogger(OnlineMachineHandler.class);
    private static String MACHINE_KEY = "south:machine:Location:";
    private static String MACHINE_KEYS_NAME = "south:machine:Location:keys";
    private static final int EXPIRE_MINUTES = 2 ;

//    private static String MACHINE_KEY = "machineLoc:";
//    private static String MACHINE_KEYS_NAME = "machineLoc:keys";

    //用于记录和管理所有客户端的channel
    private   ChannelGroup clients ;



    private RedisTemplate<String, String> redisCacheTemplate;

    private LocationRedisService locationRedisService;


    public OnlineMachineHandler(RedisTemplate<String, String> redisCacheTemplate ,LocationRedisService locationRedisService,ChannelGroup group){
        this.redisCacheTemplate = redisCacheTemplate;
        this.locationRedisService = locationRedisService;
        this.clients = group;
    }


    public OnlineMachineHandler(LocationRedisService locationRedisService,ChannelGroup group){

        this.locationRedisService = locationRedisService;
        this.clients = group;
    }

    private ObjectMapper mapper  = new ObjectMapper();




    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //是否握手成功，升级为 Websocket 协议
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            // 握手成功，移除 HttpRequestHandler，因此将不会接收到任何消息
            // 并把握手成功的 Channel 加入到 ChannelGroup 中
//            Long nowSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));    //秒
//            Long beforeSecond = nowSecond - EXPIRE_MINUTES * 60 ;
//            Map<String, Object> locations = getMachineRealTimeLocations(nowSecond, beforeSecond);

            String msgToClient = mapper.writeValueAsString(locationRedisService.getMachineRealTimeLocations());
            ctx.channel().writeAndFlush((new TextWebSocketFrame("["+getDateTimeString() +"]: "+msgToClient)));
            clients.add(ctx.channel());
        }


    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

        if (!(msg instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", msg.getClass().getName()));
        }

        // 1.获取上传的消息
        String text = ((TextWebSocketFrame)msg).text().replaceAll("\\s*", "");
        String dateTimeStr = getDateTimeString();
        logger.info("[{}]客户端传过来的消息:{}",dateTimeStr,text);
        if (text.isEmpty()){
            ctx.channel().writeAndFlush(new TextWebSocketFrame("["+dateTimeStr +"]:"+"内容不能为空"));
            return;
        }


        // 2.尝试转换为MachineRealTimeLocation对象

        MachineRealTimeLocation location;
        try {
            location = mapper.readValue(text, MachineRealTimeLocation.class);
            //判断必要字段是否为空
            boolean fieldIsNull = CommonUtil.checkObjFieldIsNull(location,"taskId","type");
            if(fieldIsNull){
                ctx.channel().writeAndFlush(new TextWebSocketFrame("["+dateTimeStr +"]:"+"json参数不完整"));
                return;
            }
        } catch (IOException e) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("["+dateTimeStr +"]:"+"json转换异常"));
            return;
        }

        // 3.1获取过期时间
        Long nowSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));    //秒
        //Long beforeSecond = nowSecond - EXPIRE_MINUTES * 60 ;

        // 3.2保存到redis
        //saveToRedis(text, location.getId(), nowSecond);
        locationRedisService.saveToRedis(text, location.getId());
        // 4.获取最新的有效数据
//        Map<String,Object> returnMap = getMachineRealTimeLocations( nowSecond, beforeSecond);
//        String msgToClient = mapper.writeValueAsString(returnMap);
        String msgToClient = locationRedisService.getMachineRealTimeLocationsJson();

        // 5.把消息刷到所有客户端
        clients.writeAndFlush(new TextWebSocketFrame("["+getDateTimeString() +"]:"+msgToClient));
    }

    private String getDateTimeString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now(ZoneOffset.of("+8")).format(formatter);
    }


    /**
     * 当客户端连接服务端后，获取客户端的channel，并放到channelGroup中进行管理
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("channel added：{}",ctx.channel().id().asShortText());

        //clients.add(ctx.channel());

    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channel in active：{}",ctx.channel().id().asShortText());
        ctx.channel().eventLoop().schedule(() -> {
            logger.info("sout....");
        },6, TimeUnit.SECONDS);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //当触发handlerRemoved，channel会自动移除对应客户端的channel
//        clients.remove(ctx.channel());
        //logger.info("channel long id：{}",ctx.channel().id().asLongText());
        logger.info("channel removed, short id：{}",ctx.channel().id().asShortText());
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 根据时间区间获取位置信息并封装到一个map中
     * @param nowSecond
     * @param beforeSecond
     * @return
     * @throws IOException
     */
    private Map<String,Object> getMachineRealTimeLocations( Long nowSecond, Long beforeSecond) throws IOException {
        // 4.读取最新的数据返回给客户端
        Set<String> keys = redisCacheTemplate.opsForZSet().rangeByScore(MACHINE_KEYS_NAME, beforeSecond, nowSecond);
        List<MachineRealTimeLocation> locationsList = new ArrayList<>();
        for (String key:keys) {
            String locationJson = redisCacheTemplate.opsForValue().get(key);
            MachineRealTimeLocation location1 = mapper.readValue(locationJson, MachineRealTimeLocation.class);
            locationsList.add(location1);
        }

        Map<String,Object> returnMap = new HashMap<>();
        if (locationsList.size() == 0){
            return returnMap;
        }
        returnMap.put("activeMachine",locationsList);

        return returnMap;
    }

    /**
     *
     * @param text
     * @param id
     * @param nowSecond
     */
    private void saveToRedis(String text, String id, Long nowSecond) {
        // 3.2通过json格式校验，开始执行保存,保存前判断是存在记录，若存在则删除
        String locationKey = MACHINE_KEY + id;
//        String tempLocationJson = redisCacheTemplate.opsForValue().get(locationKey);
//        if (tempLocationJson != null && tempLocationJson != ""){
//            redisCacheTemplate.delete(locationKey);
//        }
        // 3.3保存location 与 对应的key

        redisCacheTemplate.opsForValue().set(locationKey,text, EXPIRE_MINUTES,TimeUnit.MINUTES);    //保存记录

        redisCacheTemplate.opsForZSet().add(MACHINE_KEYS_NAME,locationKey,nowSecond);               //保存键
    }

}
