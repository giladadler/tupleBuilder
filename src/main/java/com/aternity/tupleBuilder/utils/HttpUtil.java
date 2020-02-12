package com.aternity.tupleBuilder.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpUtil {

    public static String getContentFromUrl(String url) throws Exception
    {
        String result = "";
        try {
            url = url.replaceAll(" ", "%20");
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient = (DefaultHttpClient) WebClientDevWrapper.wrapClient(httpclient);
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

        } catch (Exception e) {
            result = e.getLocalizedMessage();
        }

        return result;
    }
}
