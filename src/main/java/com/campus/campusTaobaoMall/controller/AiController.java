package com.campus.campusTaobaoMall.controller;

import com.campus.campusTaobaoMall.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import java.util.*;

/**
 * =====================================================================
 * AI商品文案生成控制器
 * =====================================================================
 *
 * 【功能说明】
 * 调用AI接口（Mimo模型），根据商品名称、分类和价格自动生成吸引大学生的商品简介
 *
 * 【技术原理】
 * 1. 接收前端发送的商品信息（名称、分类、价格）
 * 2. 构建Prompt提示词，告诉AI要生成什么样的文案
 * 3. 通过RestTemplate调用AI API（https://token-plan-cn.xiaomimimo.com）
 * 4. 解析AI响应，提取生成的文案
 * 5. 返回给前端，自动填入商品描述框
 *
 * 【接口信息】
 * 路径：POST /api/admin/ai/generate-description
 * 请求体：{ productName, category, price }
 * 响应：{ code: 200, data: "生成的文案" }
 *
 * 【核心算法】
 * - Prompt工程：通过精心设计的提示词控制AI输出质量
 * - 异常处理：超时设置、错误捕获、友好提示
 * =====================================================================
 */
@RestController
@RequestMapping("/api/admin/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    /**
     * AI接口地址
     * 使用Mimo模型（小米自研大语言模型）
     */
    private static final String API_URL = "https://token-plan-cn.xiaomimimo.com/v1/chat/completions";

    /**
     * AI接口密钥
     * 用于身份验证，防止未授权调用
     */
    private static final String API_KEY = "tp-cz50t3a0zh96fk4194g6ny2en5v23rul1g799rjv7cxe46vn";

    /**
     * =====================================================================
     * AI一键生成商品简介
     * =====================================================================
     *
     * 【功能】根据商品信息调用AI生成商品简介文案
     *
     * 【请求方式】POST
     * 【请求路径】/api/admin/ai/generate-description
     *
     * 【请求参数】（JSON格式）
     * - productName: 商品名称（必填）
     * - category: 分类名称（选填）
     * - price: 价格（选填）
     *
     * 【响应结果】
     * - 成功：{ code: 200, data: "生成的文案" }
     * - 失败：{ code: 500, message: "错误信息" }
     *
     * 【执行流程】
     * 1. 接收前端参数
     * 2. 验证输入（商品名称不能为空）
     * 3. 构建Prompt提示词
     * 4. 调用AI接口
     * 5. 解析响应，提取文案
     * 6. 返回结果
     * =====================================================================
     */
    @PostMapping("/generate-description")
    public Result<?> generateDescription(@RequestBody Map<String, String> params) {
        // =====================================================================
        // 第一步：接收前端参数
        // =====================================================================
        // getOrDefault：获取Map中的值，如果不存在就用默认值（空字符串）
        // 避免空指针异常（NullPointerException）
        String productName = params.getOrDefault("productName", "");  // 商品名称
        String category = params.getOrDefault("category", "");        // 分类名称
        String price = params.getOrDefault("price", "");              // 价格

        // =====================================================================
        // 第二步：验证输入
        // =====================================================================
        // 商品名称是必填项，AI需要根据它生成文案
        if (productName.isEmpty()) {
            return Result.error("请输入商品名称");
        }

        // =====================================================================
        // 第三步：构建Prompt提示词
        // =====================================================================
        // Prompt是告诉AI"你要做什么"的指令
        // 好的Prompt能显著提升AI输出质量
        String prompt = "你是一个校园电商平台的商品文案专家。请根据以下商品信息，生成一段吸引大学生购买的商品简介（50-100字）。\n" +
                "要求：\n" +
                "1. 语言活泼、贴近大学生生活\n" +
                "2. 突出商品的实用性和性价比\n" +
                "3. 适当使用emoji增加趣味性\n" +
                "4. 直接输出简介内容，不要加标题或前缀\n\n" +
                "商品名称：" + productName + "\n" +
                "所属分类：" + category + "\n" +
                "价格：¥" + price;

        // =====================================================================
        // 第四步：调用AI接口
        // =====================================================================
        try {
            // 4.1 创建RestTemplate（HTTP客户端）
            RestTemplate restTemplate = new RestTemplate();

            // 4.2 设置超时时间
            // 连接超时10秒：检测网络是否通畅
            // 读取超时120秒：给AI充足时间生成文案
            // 这段代码创建了一个HTTP客户端，设置了两个超时：10秒内连不上就报错，120秒内AI没返回结果就报错。
            ((org.springframework.http.client.SimpleClientHttpRequestFactory) restTemplate.getRequestFactory())
                    .setConnectTimeout(Duration.ofSeconds(10));
            ((org.springframework.http.client.SimpleClientHttpRequestFactory) restTemplate.getRequestFactory())
                    .setReadTimeout(Duration.ofSeconds(120));

            // 4.3 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);  // 告诉服务器：我发送的是JSON
            headers.set("Authorization", "Bearer " + API_KEY);  // 身份验证：携带API密钥

            // 4.4 构建请求体
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", "mimo-v2.5-pro");    // 使用的AI模型
            body.put("temperature", 0.7);          // 创造性（0=保守，1=发散）
            body.put("max_tokens", 1024);          // 最大输出长度（约500字）

            // 4.5 构建消息列表
            // AI接口采用对话格式，需要传入消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new LinkedHashMap<>();
            message.put("role", "user");           // 角色：用户（不是AI）
            message.put("content", prompt);        // 消息内容：我们构建的Prompt
            messages.add(message);
            body.put("messages", messages);        // 把消息列表放入请求体

            // =====================================================================
            // 4.6 发送请求（核心代码）
            // =====================================================================

            // 4.6.1 打包请求：将请求体和请求头封装成HttpEntity对象
            // HttpEntity = 请求体(body) + 请求头(headers)
            // 相当于把"快递箱子里的东西"和"快递单信息"打包在一起
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            //  泛型<Map<String, Object>> 表示请求体是Map类型
            //  body = { model, temperature, max_tokens, messages }
            //  headers = { Content-Type: JSON, Authorization: Bearer xxx }

            // 4.6.2 发送请求：通过RestTemplate发送POST请求到AI接口
            // exchange方法参数说明：
            //   参数1 API_URL     = 请求地址：https://token-plan-cn.xiaomimimo.com/v1/chat/completions
            //   参数2 HttpMethod.POST = 请求方法：POST（用于提交数据）
            //   参数3 request     = 请求实体：包含请求头和请求体
            //   参数4 Map.class   = 响应类型：把服务器返回的JSON转成Java的Map对象
            //
            // 返回值ResponseEntity<Map>包含：
            //   - 状态码（200=成功，500=失败）
            //   - 响应头（服务器返回的头信息）
            //   - 响应体（服务器返回的数据，即AI生成的文案）
            //
            // 执行过程：
            //   前端 → 发送POST请求 → AI服务器 → 等待3-10秒 → 接收响应 → 返回ResponseEntity
            ResponseEntity<Map> response = restTemplate.exchange(API_URL, HttpMethod.POST, request, Map.class);

            log.info("AI API响应: {}", response.getBody());

            // =====================================================================
            // 第五步：解析AI响应
            // =====================================================================
            // AI返回的响应结构：
            // {
            //   "choices": [
            //     {
            //       "message": {
            //         "role": "assistant",
            //         "content": "🔥 超大容量小米充电宝..."
            //       }
            //     }
            //   ]
            // }
            if (response.getBody() != null) {
                // 5.1 获取choices数组
                // 这段代码就是从AI返回的JSON中，一层一层往里找，最终提取出content字段（AI生成的文案），然后返回给前端
                Object choices = response.getBody().get("choices");

                // 5.2 验证choices是否存在且不为空
                if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                    // 5.3 获取第一个choice
                    Map<?, ?> firstChoice = (Map<?, ?>) ((List<?>) choices).get(0);

                    // 5.4 获取message对象
                    Object messageObj = firstChoice.get("message");

                    // 5.5 验证message是否存在
                    if (messageObj instanceof Map) {
                        // 5.6 提取content字段（AI生成的文案）
                        String content = (String) ((Map<?, ?>) messageObj).get("content");

                        // 5.7 验证内容是否存在且不为空
                        if (content != null && !content.trim().isEmpty()) {
                            return Result.success(content.trim());  // 返回成功结果
                        }
                    }
                }

                // 如果无法提取内容，记录警告日志
                log.warn("无法从响应中提取内容, 响应: {}", response.getBody());
            }

            // 如果响应为空或格式错误，返回失败
            return Result.error("AI生成失败，请重试");

        } catch (Exception e) {
            // =====================================================================
            // 异常处理
            // =====================================================================
            // 捕获所有异常：网络错误、超时、AI服务不可用等
            log.error("AI服务调用异常", e);
            return Result.error("AI服务调用失败：" + e.getMessage());
        }
    }
}
