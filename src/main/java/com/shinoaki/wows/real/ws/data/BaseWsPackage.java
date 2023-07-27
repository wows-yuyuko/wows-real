package com.shinoaki.wows.real.ws.data;

/**
 * @author Xun
 * create or update time = 2023/7/25 18:38 星期二
 */
public class BaseWsPackage<T> {
    private final int code;
    private final String path;
    private final long time;

    private final T data;

    public BaseWsPackage(String path, T data) {
        this.code = 200;
        this.path = path;
        this.time = System.currentTimeMillis();
        this.data = data;
    }

    public BaseWsPackage(WsPathType path, T data) {
        this.code = 200;
        this.path = path.name();
        this.time = System.currentTimeMillis();
        this.data = data;
    }

    public BaseWsPackage(int code, WsPathType path, T data) {
        this.code = code;
        this.path = path.name();
        this.time = System.currentTimeMillis();
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public long getTime() {
        return time;
    }

    public T getData() {
        return data;
    }

    public int getCode() {
        return code;
    }
}
