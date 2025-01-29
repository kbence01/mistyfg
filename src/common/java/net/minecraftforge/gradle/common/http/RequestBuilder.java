package net.minecraftforge.gradle.common.http;

public class RequestBuilder<RequestBody> {

    private String url;

    private String method;

    private RequestBody body;

    public RequestBuilder<RequestBody> url(String url) {
        this.url = url;
        return this;
    }

    public RequestBuilder<RequestBody> method(String method) {
        this.method = method;
        return this;
    }

    public RequestBuilder<RequestBody> body(RequestBody body) {
        this.body = body;
        return this;
    }

    public ResponseBuilder retrieve() {
        return HttpFactory.performRequest(this);
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public RequestBody getBody() {
        return body;
    }

}
