package com.cxf.imooc.netty.handler;

import com.cxf.imooc.util.CommonUtil;
import com.cxf.imooc.util.Constants;
import com.cxf.imooc.netty.OnlineContainer;
import com.cxf.imooc.util.BeansUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author ：XueFF
 * @date ：Created in 2019/5/5 9:19
 * @description：文本消息处理
 */
public class TextWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private Logger log = LoggerFactory.getLogger(this.getClass()); // 日志对象

    private OnlineContainer onlineContainer;


    //private BeansUtils beansUtils;

    public TextWebSocketHandler() {
        onlineContainer = BeansUtils.getBean(OnlineContainer.class);
    }

    /*
    经过测试，在 ws 的 uri 后面不能传递参数，不然在 netty 实现 websocket 协议握手的时候会出现断开连接的情况。
   针对这种情况在 websocketHandler 之前做了一层 地址过滤，然后重写
   request 的 uri，并传入下一个管道中，基本上解决了这个问题。
    * */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null != msg && msg instanceof FullHttpRequest) {

            FullHttpRequest request = (FullHttpRequest) msg;
             log.info("调用 channelRead request.uri() [ {} ]", request.uri());
            String uri = request.uri();
             log.info("Origin [ {} ] [ {} ]", request.headers().get("Origin"), request.headers().get("Host"));
            String origin = request.headers().get("Origin");
            if (null == origin) {
                log.info("origin 为空 ");
                ctx.close();
            } else {
                if (null != uri && uri.contains(Constants.DEFAULT_WEB_SOCKET_LINK) && uri.contains("?")) {
                    String[] uriArray = uri.split("\\?");
                    if (null != uriArray && uriArray.length > 1) {
                        String[] paramsArray = uriArray[1].split("=");
                        if (null != paramsArray && paramsArray.length > 1) {
                            onlineContainer.putAll(paramsArray[1], ctx);
                        }
                    }
                    request.setUri(Constants.DEFAULT_WEB_SOCKET_LINK);
                }else {
                    log.info("不允许 [ {} ] 连接 强制断开", origin);
                    ctx.close();
                }
            }

        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws IOException {
        log.info("接收到客户端的消息:[{}]", msg.text());
        String text = (msg).text().replaceAll("\\s|\\t|\\r|\\n", "");
        text = CommonUtil.replaceBlank(text);
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> map = mapper.readValue(text, Map.class);

        // 如果是向客户端发送文本消息，则需要发送 TextWebSocketFrame 消息
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = inetSocketAddress.getHostName();
        String txtMsg = "[" + ip + "][" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] ==> " + map.toString();
        //TODO 这是发给自己
        ctx.channel().writeAndFlush(new TextWebSocketFrame(txtMsg));
        //tx.fireChannelRead(msg);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //移除map
        onlineContainer.removeAll(ctx.channel().id().asLongText());
        ctx.close();
        log.error("服务器发生了异常: [ {} ]", cause);
    }




    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 添加
        //log.info(" 客户端加入 [ {} ]", ctx.channel().id().asLongText());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 移除
        //log.info(" 离线 [ {} ] ", ctx.channel().id().asLongText());


        super.channelInactive(ctx);
        //移除map
        String key = onlineContainer.removeAll(ctx.channel().id().asLongText());
        ctx.close();
    }

}
