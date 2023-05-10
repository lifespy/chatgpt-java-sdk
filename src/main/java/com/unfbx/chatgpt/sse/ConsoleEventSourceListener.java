package com.unfbx.chatgpt.sse;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;

/**
 * 描述： sse
 *
 * @author https:www.unfbx.com
 * 2023-02-28
 */
@Slf4j
public class ConsoleEventSourceListener extends EventSourceListener {

    private CountDownLatch countDownLatch;

    public ConsoleEventSourceListener(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("OpenAI建立sse连接...");
    }

    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        log.info("OpenAI返回数据：{}", data);
        ChatCompletionResponse response = JSONUtil.toBean(data, ChatCompletionResponse.class);
        String finishReason = response.getChoices().get(0).getFinishReason();
        if (data.equals("[DONE]") || StrUtil.equals(finishReason, "stop")) {
            log.info("OpenAI返回数据结束了");
            countDownLatch.countDown();
        }
    }

    @Override
    public void onClosed(EventSource eventSource) {
        log.info("OpenAI关闭sse连接...");
    }

    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        if(Objects.isNull(response)){
            log.error("OpenAI  sse连接异常:{}", t);
            eventSource.cancel();
            return;
        }
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", body.string(), t);
        } else {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", response, t);
        }
        eventSource.cancel();
    }
}
