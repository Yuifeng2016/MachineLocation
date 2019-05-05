package com.cxf.imooc.netty;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ：XueFF
 * @date ：Created in 2019/5/5 9:22
 * @description：存储在线ws用户的容器
 */
@Component
public class OnlineContainer {

    private Logger log = LoggerFactory.getLogger(this.getClass()); // 日志对象
    /**
     * <session,ChannelHandlerContext>
     **/
    private Map<String, ChannelHandlerContext> onlineUserMap = new ConcurrentHashMap<>();

    /**
     * <userId,sessionId>
     **/
    private Map<String, String> userMap = new ConcurrentHashMap<>();




    public Map<String, String> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, String> userMap) {
        this.userMap = userMap;
    }

    public Map<String, ChannelHandlerContext> getOnlineUserMap() {
        return onlineUserMap;
    }

    public void setOnlineUserMap(Map<String, ChannelHandlerContext> onlineUserMap) {
        this.onlineUserMap = onlineUserMap;
    }




    /***
     * 根据userId得到通道
     * */
    public ChannelHandlerContext getChannelHandlerContextByUserId(String userId) {
        return onlineUserMap.getOrDefault(userMap.getOrDefault(userId, ""), null);
    }

    /***
     * 添加session信息
     * */
    public void putAll(String userId, ChannelHandlerContext ctx) {
        userMap.put(userId, ctx.channel().id().asLongText());
        onlineUserMap.put(ctx.channel().id().asLongText(), ctx);
        log.info("用户 [ {} ] 上线", userId);
    }


    /***
     * 删除session信息
     * */
    public String removeAll(String sessionId) {
        //如果存在则删除
        String key = null;
        if (userMap.containsValue(sessionId)) {

            for (Map.Entry<String, String> entry : userMap.entrySet()) {
                if (null != entry.getValue() && entry.getValue().equals(sessionId)) {
                    key = entry.getKey();
                    break;
                }
            }
            if (null != key) {
                log.info("用户 [ {} ] 离线 ", key);
                userMap.remove(key);
            }
            onlineUserMap.remove(sessionId);
        }
        return key;
    }
}

