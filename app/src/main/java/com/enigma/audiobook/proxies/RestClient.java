package com.enigma.audiobook.proxies;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import com.enigma.audiobook.backend.utils.SerDe;
import com.enigma.audiobook.utils.ALog;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class RestClient {
    static String TAG = "RestClient";
    static SerDe serDe = new SerDe();
    static final String URL_FORMAT = "http://%s/%s";
    private final CloseableHttpClient client;

    public RestClient() {
        client = HttpClientBuilder.create().build();
    }

    public <T> T doPost(String hostAndPort, String path, String jsonStr, Class<T> clazz) {
        String uriStr = String.format(URL_FORMAT, hostAndPort, path);

        URI uri = URI.create(uriStr);
        HttpPost request = new HttpPost(uri);
        request.setEntity(new StringEntity(jsonStr, APPLICATION_JSON));

        try (CloseableHttpResponse response = client.execute(request)) {

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException(String.format("unable to make rest request, host:%s, path:%s",
                        hostAndPort, path));
            }

            String jsonResponse = EntityUtils.toString(response.getEntity());

            return serDe.fromJson(jsonResponse, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HeaderAndEntity doPut(String url, byte[] data) {
        URI uri = URI.create(url);
        HttpPut request = new HttpPut(uri);
        request.setEntity(new ByteArrayEntity(data));
        ALog.i(TAG, "executing http request:" + url);
        try (CloseableHttpResponse response = client.execute(request)) {
            ALog.i(TAG, "http response:" + response);
            ALog.i(TAG, "http response status:" + response.getStatusLine());
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException(String.format("unable to make rest request, url:%s",
                        url));
            }
            List<Header> headers = Arrays.asList(response.getAllHeaders());
            String jsonResponse = response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity());

            return new HeaderAndEntity(headers, jsonResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class HeaderAndEntity {
        final List<Header> headers;
        final String jsonResponse;

        public HeaderAndEntity(List<Header> headers, String jsonResponse) {
            this.headers = headers;
            this.jsonResponse = jsonResponse;
        }

        public List<Header> getHeaders() {
            return headers;
        }

        public String getJsonResponse() {
            return jsonResponse;
        }

        @Override
        public String toString() {
            return "HeaderAndEntity{" +
                    "headers=" + headers +
                    ", jsonResponse='" + jsonResponse + '\'' +
                    '}';
        }
    }
}
