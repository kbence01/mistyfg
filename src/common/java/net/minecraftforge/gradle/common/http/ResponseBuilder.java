package net.minecraftforge.gradle.common.http;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;

import java.io.InputStreamReader;

public class ResponseBuilder<Response> {

    private HttpEntity entity;

    private int status;

    public ResponseBuilder(HttpEntity entity, int status) {
        this.entity = entity;
        this.status = status;
    }

    public Response toMono(Class<Response> clazz) {
        try {
            return new Gson().fromJson(new InputStreamReader(entity.getContent()), clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
