package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import java.util.*;

@RestController
@RequestMapping("/api/admin/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);
    private static final String API_URL = "https://token-plan-cn.xiaomimimo.com/v1/chat/completions";
    private static final String API_KEY = "tp-cz50t3a0zh96fk4194g6ny2en5v23rul1g799rjv7cxe46vn";

    @PostMapping("/generate-description")
    public Result<?> generateDescription(@RequestBody Map<String, String> params) {
        String productName = params.getOrDefault("productName", "");
        String category = params.getOrDefault("category", "");
        String price = params.getOrDefault("price", "");

        if (productName.isEmpty()) {
            return Result.error("请输入商品名称");
        }

        String prompt = "你是一个校园电商平台的商品文案专家。请根据以下商品信息，生成一段吸引大学生购买的商品简介（50-100字）。\n" +
                "要求：\n" +
                "1. 语言活泼、贴近大学生生活\n" +
                "2. 突出商品的实用性和性价比\n" +
                "3. 适当使用emoji增加趣味性\n" +
                "4. 直接输出简介内容，不要加标题或前缀\n\n" +
                "商品名称：" + productName + "\n" +
                "所属分类：" + category + "\n" +
                "价格：¥" + price;

        try {
            RestTemplate restTemplate = new RestTemplate();
            // 设置超时时间：连接5秒，读取60秒（AI推理需要时间）
            ((org.springframework.http.client.SimpleClientHttpRequestFactory) restTemplate.getRequestFactory())
                    .setConnectTimeout(Duration.ofSeconds(5));
            ((org.springframework.http.client.SimpleClientHttpRequestFactory) restTemplate.getRequestFactory())
                    .setReadTimeout(Duration.ofSeconds(60));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + API_KEY);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", "mimo-v2.5-pro");
            body.put("temperature", 0.7);
            body.put("max_tokens", 1024);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new LinkedHashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            body.put("messages", messages);

            log.info("调用AI API, 商品: {}", productName);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(API_URL, HttpMethod.POST, request, Map.class);

            log.info("AI API响应: {}", response.getBody());

            if (response.getBody() != null) {
                Object choices = response.getBody().get("choices");
                if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                    Map<?, ?> firstChoice = (Map<?, ?>) ((List<?>) choices).get(0);
                    Object messageObj = firstChoice.get("message");
                    if (messageObj instanceof Map) {
                        String content = (String) ((Map<?, ?>) messageObj).get("content");
                        if (content != null && !content.trim().isEmpty()) {
                            return Result.success(content.trim());
                        }
                    }
                }

                // 如果没有从choices中提取到内容，返回整个响应供调试
                log.warn("无法从响应中提取内容, 响应: {}", response.getBody());
            }

            return Result.error("AI生成失败，请重试");
        } catch (Exception e) {
            log.error("AI服务调用异常", e);
            return Result.error("AI服务调用失败：" + e.getMessage());
        }
    }
}