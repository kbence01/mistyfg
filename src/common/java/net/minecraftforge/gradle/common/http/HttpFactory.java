package net.minecraftforge.gradle.common.http;

import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpFactory {

    public static ResponseBuilder performRequest(RequestBuilder request) {
        switch (request.getMethod()) {
            case "POST":
                return performPost(request);

            case "GET":
                return performGet(request);
        }
        return null;
    }

    public static ResponseBuilder performPost(RequestBuilder request) {
        try {
            final HttpPost post = new HttpPost(request.getUrl());

            final StringEntity requestEntity = new StringEntity(new Gson().toJson(request.getBody()));
            System.out.println(new Gson().toJson(request.getBody()));
            post.setEntity(requestEntity);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-Type", "application/json");

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = (CloseableHttpResponse) client
                         .execute(post)) {

                return new ResponseBuilder(response.getEntity(), response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResponseBuilder performGet(RequestBuilder request) {
        throw new RuntimeException("not implemented yet");
    }

}
