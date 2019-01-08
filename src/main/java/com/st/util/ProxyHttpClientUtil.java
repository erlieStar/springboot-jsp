package com.st.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProxyHttpClientUtil {

    public static final String CHARSET_UTF8 = "UTF-8";

    public static final String PROXY_IP = "";

    public static final int PROXY_PORT = 1;
    public static final boolean USE_PROXY = false;

    // 使用ResponseHandler接口处理响应，HttpClient使用ResponseHandler会自动管理连接的释放，解决了对连接的释放管理
    private static ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
        // 自定义响应处理
        @Override
        public String handleResponse(HttpResponse response)
                throws ClientProtocolException, IOException {

            int code = response.getStatusLine().getStatusCode();

            // 如果不是200，则返回状态码
            // if (HttpStatus.SC_OK != code) {
            if (code < 200 || code >= 500) {

                return String.valueOf(code);
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                Charset charset = ContentType.getOrDefault(entity).getCharset();
                if (null != charset) {
                    return new String(EntityUtils.toByteArray(entity), charset);
                } else {
                    return new String(EntityUtils.toByteArray(entity));
                }

            } else {
                return null;
            }
        }
    };

    protected static Logger logger = LoggerFactory.getLogger(ProxyHttpClientUtil.class);

    /**
     * 释放HttpClient连接
     *
     * @param hrb
     *            请求对象
     * @param httpclient
     *            对象
     */
    public static void abortConnection(final HttpUriRequest hrb,
            final CloseableHttpClient httpclient) {
        if (hrb != null) {
            hrb.abort();
        }
        if (httpclient != null) {
            // httpclient.getConnectionManager().shutdown();
            try {
                // logger.debug("closing httpclient ...");
                httpclient.close();
            } catch (IOException e) {
                logger.error("failed to close httpclient", e);
            }

        }
    }

    /**
     * Get方式提交,URL中包含查询参数
     *
     * @param url
     *            提交地址
     * @return 响应消息
     */
    public static String get(String url) throws Exception {
        return get(url, null, CHARSET_UTF8);
    }

    /**
     * Get方式提交,URL中不包含查询参数
     *
     * @param url
     *            提交地址
     * @param params
     *            查询参数集, 键/值对
     * @return 响应消息
     */
    public static String get(String url, Map<String, String> params) throws Exception {
        return get(url, params, CHARSET_UTF8);
    }

    /**
     * Get方式提交,URL中不包含查询参数
     *
     * @param url
     *            提交地址
     * @param params
     *            查询参数集, 键/值对
     * @param charset
     *            参数提交编码集
     * @return 响应消息
     */
    public static String get(String url, Map<String, String> params, String charset)
            throws Exception {
        if (url == null || StringUtils.isEmpty(url)) {
            return null;
        }
        List<NameValuePair> qparams = getParamsList(params);
        if (qparams != null && qparams.size() > 0) {
            charset = (charset == null ? CHARSET_UTF8 : charset);
            String formatParams = URLEncodedUtils.format(qparams, charset);
            url = (url.indexOf("?")) < 0 ? (url + "?" + formatParams)
                    : (url.substring(0, url.indexOf("?") + 1) + formatParams);
        }
        CloseableHttpClient httpclient = getHttpClient(charset);
        HttpGet hg = new HttpGet(url);
        // 发送请求，得到响应
        String responseStr = null;
        try {
            responseStr = httpclient.execute(hg, responseHandler);
        } catch (ClientProtocolException e) {
            throw new Exception("客户端连接协议错误", e);
        } catch (IOException e) {
            throw new Exception("IO操作异常", e);
        } finally {
            abortConnection(hg, httpclient);
        }
        return responseStr;
    }

    public static HttpClientConnectionManager getConnectionManager() {

        // ConnectionSocketFactory plainsf = null;
        LayeredConnectionSocketFactory sslsf = null;

        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();

        PlainConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        registryBuilder.register("http", plainsf);

        try {

            // Trust own CA and all self-signed certs
            SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                    return true;
                }
            }).build();

            HostnameVerifier allowAllHostnameVerifier = NoopHostnameVerifier.INSTANCE;
            sslsf = new SSLConnectionSocketFactory(sslcontext, allowAllHostnameVerifier);

            registryBuilder.register("https", sslsf);

        } catch (Throwable e) {

            logger.error("https ssl init failed", e);
        }

        Registry<ConnectionSocketFactory> r = registryBuilder.build();

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(r);
        connManager.setMaxTotal(100);// 连接池最大并发连接数
        connManager.setDefaultMaxPerRoute(100);// 单路由最大并发数
        return connManager;

    }

    /**
     * 获取HttpClient实例
     *
     * @param charset
     *            参数编码集, 可空
     * @return HttpClient 对象
     */
    public static CloseableHttpClient getHttpClient(final String charset) {
        return getHttpClientBuilder(charset).build();

    }

    public static HttpClientBuilder getHttpClientBuilder(final String charset) {

        HttpClientBuilder builder = HttpClients.custom();

        Charset chartset = charset == null ? Charset.forName(CHARSET_UTF8)
                : Charset.forName(charset);
        ConnectionConfig.Builder connBuilder = ConnectionConfig.custom().setCharset(chartset);

        RequestConfig.Builder reqBuilder = RequestConfig.custom();
        reqBuilder.setExpectContinueEnabled(false);
        reqBuilder.setSocketTimeout(10 * 60 * 1000);
        reqBuilder.setConnectTimeout(10 * 60 * 1000);
        reqBuilder.setMaxRedirects(10);

        if (USE_PROXY) {
            logger.info("using proxy {}:{} to request api", PROXY_IP, String.valueOf(PROXY_PORT));
            HttpHost proxy = new HttpHost(PROXY_IP, PROXY_PORT);
            reqBuilder.setProxy(proxy);
        }

        ServiceUnavailableRetryStrategy serviceUnavailableRetryStrategy = new DefaultServiceUnavailableRetryStrategy(
                3, 3000);
        builder.setServiceUnavailableRetryStrategy(serviceUnavailableRetryStrategy);
        // 模拟浏览器，解决一些服务器程序只允许浏览器访问的问题
        builder.setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");

        builder.setDefaultRequestConfig(reqBuilder.build());
        builder.setDefaultConnectionConfig(connBuilder.build());
        builder.setConnectionManager(getConnectionManager());

        return builder;

    }

    /**
     * 将传入的键/值对参数转换为NameValuePair参数集
     *
     * @param paramsMap
     *            参数集, 键/值对
     * @return NameValuePair参数集
     */
    public static List<NameValuePair> getParamsList(Map<String, String> paramsMap) {
        if (paramsMap == null || paramsMap.size() == 0) {
            return null;
        }
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> map : paramsMap.entrySet()) {
            params.add(new BasicNameValuePair(map.getKey(), map.getValue()));
        }
        return params;
    }

    /**
     * Post方式提交,URL中不包含提交参数
     *
     * @param url
     *            提交地址
     * @param params
     *            提交参数集, 键/值对
     * @return 响应消息
     */
    public static String post(String url, Map<String, String> params) {
        return post(url, params, HttpClientUtil.CHARSET_UTF8);
    }

    /**
     * Post方式提交,URL中不包含提交参数
     *
     * @param url
     *            提交地址
     * @param params
     *            提交参数集, 键/值对
     * @param charset
     *            参数提交编码集
     * @return 响应消息
     */
    public static String post(String url, Map<String, String> params, String charset) {
        if (url == null || StringUtils.isEmpty(url)) {
            return null;
        }
        // 创建HttpClient实例
        CloseableHttpClient httpclient = getHttpClient(charset);
        UrlEncodedFormEntity formEntity = null;
        try {
            if (charset == null || StringUtils.isEmpty(charset)) {
                if (null != params && params.size() > 0) {
                    formEntity = new UrlEncodedFormEntity(getParamsList(params));
                }
            } else {
                if (null != params && params.size() > 0) {
                    formEntity = new UrlEncodedFormEntity(getParamsList(params), charset);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("不支持的编码集", e);
        }
        HttpPost hp = new HttpPost(url);

        if (null != formEntity) {
            hp.setEntity(formEntity);
        }

        // 发送请求，得到响应
        String responseStr = null;
        try {
            responseStr = httpclient.execute(hp, responseHandler);
        } catch (ClientProtocolException e) {
            throw new RuntimeException("客户端连接协议错误", e);
        } catch (IOException e) {
            throw new RuntimeException("IO操作异常", e);
        } finally {
            abortConnection(hp, httpclient);
        }
        return responseStr;
    }

    public static String post(String url, Map<String, String> params, String charset,
            boolean isSecure) {
        String result = null;

        result = post(url, params, charset);

        return result;
    }

}
