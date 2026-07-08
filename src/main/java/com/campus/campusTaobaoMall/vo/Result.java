package com.campus.campusTaobaoMall.vo;

import lombok.Data;

/**
 * 统一响应格式类 — 所有接口返回给前端的数据都用这个格式
 *
 * 返回格式：{code: 状态码, message: 提示信息, data: 数据}
 * 前端统一判断 code === 200 表示成功
 */
@Data
public class Result<T> {
    private Integer code;      // 状态码：200=成功，500=失败，401=未登录
    private String message;    // 提示信息："success" 或 "错误原因"
    private T data;            // 返回的数据（泛型，可以是任意类型）

    /**
     * 成功时调用 — 返回数据
     * 用法：Result.success(product) 或 Result.success(list)
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);          // 状态码200表示成功
        result.setMessage("success"); // 成功提示
        result.setData(data);         // 放入实际数据
        return result;
    }

    /**
     * 失败时调用 — 返回错误提示（状态码默认500）
     * 用法：Result.error("用户名已存在")
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);          // 状态码500表示服务器错误
        result.setMessage(message);   // 错误原因
        return result;
    }

    /**
     * 失败时调用 — 自定义状态码 + 错误提示
     * 用法：Result.error(401, "未登录，请先登录")
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);         // 自定义状态码（401/403等）
        result.setMessage(message);   // 错误原因
        return result;
    }
}
