package com.unfbx.chatgpt.interceptor;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.unfbx.chatgpt.exception.BaseException;
import com.unfbx.chatgpt.exception.CommonError;
import com.unfbx.chatgpt.function.KeyStrategyFunction;

@Slf4j
public class AzureOpenAiAuthInterceptor extends OpenAiAuthInterceptor {
    /**
     * 请求头处理
     */
    public AzureOpenAiAuthInterceptor() {
        super.setWarringConfig(null);
    }

    /**
     * 构造方法
     *
     * @param warringConfig 所有的key都失效后的告警参数配置
     */
    public AzureOpenAiAuthInterceptor(Map warringConfig) {
        super.setWarringConfig(warringConfig);
    }

    /**
     * 拦截器鉴权
     *
     * @param chain Chain
     * @return Response对象
     * @throws IOException
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        return chain.proceed(auth(super.getKey(), original));
    }

    /**
     * key失效或者禁用后的处理逻辑
     * 默认不处理
     *
     * @param apiKey 返回新的api keys集合
     * @return 新的apiKey集合
     */
    @Override
    protected List<String> onErrorDealApiKeys(String apiKey) {
        return super.getApiKey();
    }

    @Override
    protected void noHaveActiveKeyWarring() {
        log.error("--------> [告警] 没有可用的key！！！");
    }

    /**
     * AZURE的鉴权处理方法
     *
     * @param key      api key
     * @param original 源请求体
     * @return 请求体
     */
    public Request auth(String key, Request original) {
        return original.newBuilder()
                .header("api-key", key)
                .header(Header.CONTENT_TYPE.getValue(), ContentType.JSON.getValue())
                .method(original.method(), original.body())
                .build();
    }
}
