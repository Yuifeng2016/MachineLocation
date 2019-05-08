package com.cxf.imooc.service;

import com.cxf.imooc.dto.JsonRootBean;
import com.cxf.imooc.dto.PilingTsProjectEntity;
import com.cxf.imooc.util.JsonUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ：XueFF
 * @date ：Created in 2019/5/7 10:03
 * @description：Http业务类
 */
@Component
public class HttpService {
    private static Logger logger = LoggerFactory.getLogger(HttpService.class);

    // 创建 HttpClient 客户端
    private static CloseableHttpClient httpClient = HttpClients.createDefault();


    public String getProgramId(Map<String,String> param){
        // 创建 HttpPost 请求
        HttpPost httpPost = new HttpPost("http://120.77.224.230:9090/piling/get_piling_project_by_id");
        // 设置长连接
        httpPost.setHeader("Connection", "keep-alive");
        // 设置代理（模拟浏览器版本）
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
        // 创建 HttpPost 参数
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
//        params.add(new BasicNameValuePair("page_num", "1"));
//        params.add(new BasicNameValuePair("num_per_page", "10"));
        params.add(new BasicNameValuePair("project_id", param.get("taskId")));
        params.add(new BasicNameValuePair("token", param.get("token")));
        params.add(new BasicNameValuePair("user_name", param.get("username")));
        CloseableHttpResponse httpResponse = null;
        String programId = null;
        try {
            // 设置 HttpPost 参数
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            // 输出请求结果
            String response = EntityUtils.toString(httpEntity);

            JsonRootBean bean = JsonUtil.JsonToObject(response, JsonRootBean.class);
            PilingTsProjectEntity task = bean.getPilingTsProjectEntity();
            logger.info(task.getPid());
            programId = task.getPid();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 无论如何必须关闭连接
        finally {
            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return programId;
    }



}
