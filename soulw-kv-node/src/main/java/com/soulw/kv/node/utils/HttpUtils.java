package com.soulw.kv.node.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpStatus;
import org.springframework.util.StreamUtils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 21:33
 */
@Slf4j
public class HttpUtils {

    /**
     * 默认客户端
     */
    public static final HttpClient CLIENT = initDefaultClient();
    private static final int CONNECT_TIMEOUT = Integer.parseInt(System.getProperty("httputils.connection.timeout", "10000"));
    private static final int READ_WRITE_TIMEOUT = Integer.parseInt(System.getProperty("httputils.read.write.timeout", "10000"));
    private static final int MAX_CONN = Integer.parseInt(System.getProperty("httputils.max.conn", "200"));
    private static final Map<String, String> DEFAULT_HEADER = ImmutableMap.<String, String>builder()
            .put("Accept", "*/*")
            .put("Connection", "keep-alive")
            .put("Cache-Control", "no-cache")
            .put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36")
            .build();
    private static final int SYSTEM_ERROR = -1;
    private static final int DEFAULT_BUFF = 1024;

    /**
     * GET请求
     *
     * @return 结果字符串
     */
    public static ResponseVO post(String url, Object data, Map<String, String> customHeader) {
        Preconditions.checkNotNull(url, "request url is null");
        Preconditions.checkNotNull(data, "request data is null");

        HttpPost post = new HttpPost(url);

        DEFAULT_HEADER.forEach(post::addHeader);
        if (MapUtils.isNotEmpty(customHeader)) {
            customHeader.forEach(post::addHeader);
        }

        if (!post.containsHeader(HttpHeaders.CONTENT_TYPE)) {
            post.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        }

        if (log.isInfoEnabled()) {
            log.info("HttpUtils.post() url: {}, data: {}, headers: {}", url, data, post.getAllHeaders());
        }

        if (data instanceof String) {
            post.setEntity(new StringEntity((String) data, StandardCharsets.UTF_8));
        } else {
            post.setEntity(new StringEntity(JSON.toJSONString(data), StandardCharsets.UTF_8));
        }
        return resolveResponse(post);
    }

    private static ResponseVO resolveResponse(HttpUriRequest request) {
        try {
            HttpResponse resp = CLIENT.execute(request);
            StatusLine status = resp.getStatusLine();
            int statusCode = status.getStatusCode();
            boolean isError = HttpStatus.valueOf(statusCode).isError();

            ByteArrayOutputStream out = new ByteArrayOutputStream(DEFAULT_BUFF);
            if (!isError) {
                HttpEntity entity = resp.getEntity();
                InputStream inStream = entity.getContent();
                StreamUtils.copy(inStream, out);
            }

            ResponseVO respVO = new ResponseVO(out.toByteArray(), statusCode, status.getReasonPhrase(), !isError);
            if (log.isInfoEnabled()) {
                log.info("HttpUtils.getResponse() status:{}, statusMsg:{}, data:{}",
                        respVO.getStatus(), respVO.getStatusMsg(), respVO.getBodyStr());
            }
            return respVO;
        } catch (Throwable e) {
            log.error("request error", e);
            return new ResponseVO(null, SYSTEM_ERROR, e.getMessage(), false);
        }
    }

    /**
     * 响应结果
     *
     * @author Soulw
     */
    @Value
    public static class ResponseVO {
        byte[] body;
        int status;
        String statusMsg;
        boolean success;

        public String getBodyStr() {
            if (isSuccess()) {
                return new String(body, StandardCharsets.UTF_8);
            }
            return null;
        }

    }


    /**
     * 创建默认客户端
     *
     * @return 客户端
     */
    private static HttpClient initDefaultClient() {
        try {
            SSLContext sslContext = SSLContext.getInstance("tls");
            sslContext.init(new KeyManager[0], new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, null);
            return HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setConnectTimeout(CONNECT_TIMEOUT)
                            .setSocketTimeout(READ_WRITE_TIMEOUT)
                            .setConnectionRequestTimeout(CONNECT_TIMEOUT)
                            .build())
                    .setMaxConnPerRoute(32)
                    .setMaxConnTotal(MAX_CONN)
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier((s, sslSession) -> true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("init http client error", e);
        }
    }

}
