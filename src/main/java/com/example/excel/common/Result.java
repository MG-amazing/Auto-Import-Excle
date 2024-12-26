package com.example.excel.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(
        description = "成功标志"
    )
    private boolean success = true;
    @Schema(
        description = "是否响应结果解密/ SM4/CBC"
    )
    private boolean crypto = false;
    @Schema(
        description = "返回处理消息"
    )
    private String message = "";
    @Schema(
        description = "返回代码"
    )
    private Integer code = 0;
    @Schema(
        description = "返回数据对象 data"
    )
    private T result;
    @Schema(
        description = "时间戳"
    )
    private long timestamp = System.currentTimeMillis();

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result() {

    }

    public Result<T> success(String message) {
        this.message = message;
        this.code = 200;
        this.success = true;
        return this;
    }

    public static <T> Result<T> OK() {
        Result<T> r = new Result();
        r.setSuccess(true);
        r.setCode(200);
        return r;
    }

    public static <T> Result<T> OK(T data) {
        Result<T> r = new Result();
        r.setSuccess(true);
        r.setCode(200);
        r.setResult(data);
        return r;
    }

    public static <T> Result<T> OK(String msg, T data) {
        Result<T> r = new Result();
        r.setSuccess(true);
        r.setCode(200);
        r.setMessage(msg);
        r.setResult(data);
        return r;
    }

    public static <T> Result<T> error(String msg, T data) {
        Result<T> r = new Result();
        r.setSuccess(false);
        r.setCode(500);
        r.setMessage(msg);
        r.setResult(data);
        return r;
    }

    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> r = new Result();
        r.setCode(code);
        r.setMessage(msg);
        r.setSuccess(false);
        return r;
    }

    public Result<T> error500(String message) {
        this.message = message;
        this.code = 500;
        this.success = false;
        return this;
    }
}
