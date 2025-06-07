package com.fennel.aceinterview.question.exception;

import com.baomidou.mybatisplus.extension.api.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
// @RestControllerAdvice: 这是一个组合注解，相当于 @ControllerAdvice + @ResponseBody。
// 它表示这个类是用于增强 Controller 的，并且所有 @ExceptionHandler 方法的返回值
// 都会被自动序列化为HTTP响应体（通常是JSON）。
// 如果你的Controller本身已经使用了 @RestController，或者你希望全局控制响应体，那么 @RestControllerAdvice 很合适。
// 如果你只想提供建议而不自动添加 @ResponseBody，可以使用 @ControllerAdvice。
@RestControllerAdvice // (推荐用于RESTful API)
public class GlobalExceptionHandler {

    /**
     * 处理所有未被其他 @ExceptionHandler 捕获的 RuntimeException。
     * 这是通用的运行时异常处理器，通常对应服务器内部错误。
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 设置HTTP响应状态码为500
    public R handleRuntimeException(RuntimeException e) {
        log.error("捕获到运行时异常: {}", e.getMessage(), e); // 记录详细的错误堆栈信息
        // 向前端返回一个通用的、用户友好的错误信息
        // 不要直接暴露 e.getMessage() 给用户，除非你确定它是安全的且用户可理解的
        return R.failed("系统繁忙，请稍后再试！"); // 假设 R.failed(String msg)
    }

    /**
     * 处理由于 @Validated 注解校验失败抛出的 MethodArgumentNotValidException。
     * 这通常发生在Controller方法的请求体参数校验失败时。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设置HTTP响应状态码为400
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("参数校验失败: {}", e.getMessage());
        // 从异常中提取所有校验错误信息，并将它们拼接起来
        String errorMessages = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return R.failed("请求参数不合法: " + errorMessages);
    }
}
