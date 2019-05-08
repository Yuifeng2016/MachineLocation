package com.cxf.imooc.netty.handler;

import com.cxf.imooc.util.CommonUtil;
import com.cxf.imooc.util.Constants;
import com.cxf.imooc.netty.OnlineContainer;
import com.cxf.imooc.util.BeansUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ：XueFF
 * @date ：Created in 2019/5/5 9:19
 * @description：文本消息处理
 */
public class TextWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private Logger log = LoggerFactory.getLogger(this.getClass()); // 日志对象

    private OnlineContainer onlineContainer;
    private WebSocketServerHandshaker handshaker;

    private ChannelHandlerContext ctx;
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
            handleHttpRequest(ctx,request);

            log.info("调用 channelRead request.uri() [ {} ]", request.uri());
            String uriStr = request.uri();
            URI uri = new URI(uriStr);
            


             log.info("Origin [ {} ] [ {} ]", request.headers().get("Origin"), request.headers().get("Host"));
            String origin = request.headers().get("Origin");
            if (null != origin) {
                if (null != uriStr && uriStr.contains(Constants.DEFAULT_WEB_SOCKET_LINK) && uriStr.contains("?")) {
                    String query = uri.getRawQuery();
                    String[] uriArray = uriStr.split("\\?");

                    Map<String,String> paramsMap = new HashMap<>();

                    if (null != query && query != ""){
                        String[] params = query.split("&");
                        for (String param: params) {
                            String[] paramPair = param.split("=");
                            paramsMap.put(paramPair[0],paramPair[1]);
                        }
                        log.info("参数键值对：{}",paramsMap);
                        if (paramsMap.size()!= 0){

                            onlineContainer.putAll(paramsMap.get("userId"), ctx,paramsMap);
                        }
                    }

                    request.setUri(Constants.DEFAULT_WEB_SOCKET_LINK);
                }else {
                    log.info("不允许 [ {} ] 连接 强制断开", origin);
                    ctx.close();
                }
            } /*else {
                log.info("origin 为空 ");
                ctx.close();
            }*/

        }

        //super.channelRead(ctx, msg);
    }




    /**
     * 处理Http请求，完成WebSocket握手<br/>
     * 注意：WebSocket连接第一次请求使用的是Http
     * @param ctx
     * @param request
     * @throws Exception
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 如果HTTP解码失败，返回HHTP异常
        if (!request.getDecoderResult().isSuccess() || (!"websocket".equals(request.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        // 正常WebSocket的Http连接请求，构造握手响应返回
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://" + request.headers().get(HttpHeaders.Names.HOST), null, false);
        handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null) { // 无法处理的websocket版本
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else { // 向客户端发送websocket握手,完成握手
            handshaker.handshake(ctx.channel(), request);
            // 记录管道处理上下文，便于服务器推送数据到客户端
            this.ctx = ctx;
        }
    }

    /**
     * Http返回
     * @param ctx
     * @param request
     * @param response
     */
    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
        // 返回应答给客户端
        if (response.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(response.getStatus().toString(), CharsetUtil.UTF_8);
            response.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(response, response.content().readableBytes());
        }

        // 如果是非Keep-Alive，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(response);
        if (!HttpHeaders.isKeepAlive(request) || response.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
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
