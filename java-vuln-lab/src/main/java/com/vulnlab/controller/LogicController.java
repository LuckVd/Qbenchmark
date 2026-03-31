package com.vulnlab.controller;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 业务逻辑漏洞演示
 *
 * 漏洞说明：
 * - 业务逻辑漏洞是由于应用程序业务流程设计缺陷导致的安全问题
 * - 与技术漏洞不同，这类漏洞源于业务逻辑本身的缺陷
 * - 危险等级: 中危 (Medium)
 *
 * 漏洞类型：
 * 1. 支付逻辑漏洞: 金额篡改、支付绕过、负数金额
 * 2. 验证码漏洞: 可预测、可复用、可绕过
 * 3. 竞态条件: 并发请求导致的重复消费
 * 4. 优惠券滥用: 重复使用、叠加使用
 *
 * 常见场景：
 * - 支付金额可篡改
 * - 优惠券可重复使用
 * - 验证码可预测
 * - 支付验证可绕过
 * - 并发竞态导致重复扣款
 *
 * 修复方案：
 * 1. 服务端验证所有业务逻辑
 * 2. 使用事务保证数据一致性
 * 3. 限制验证码使用次数和时效
 * 4. 实施幂等性检查防止重复操作
 * 5. 使用加密和签名保护敏感数据
 *
 * @author VulnLab
 */
@RestController
@RequestMapping("/logic")
public class LogicController {

    // 模拟数据库
    private static final Map<String, User> USERS = new ConcurrentHashMap<>();
    private static final Map<String, Product> PRODUCTS = new ConcurrentHashMap<>();
    private static final Map<String, Coupon> COUPONS = new ConcurrentHashMap<>();
    private static final Map<String, Order> ORDERS = new ConcurrentHashMap<>();
    private static final Map<String, Captcha> CAPTCHAS = new ConcurrentHashMap<>();

    private static final AtomicLong orderIdCounter = new AtomicLong(1);

    // 初始化测试数据
    static {
        USERS.put("user1", new User("user1", new BigDecimal("10000.00")));
        USERS.put("user2", new User("user2", new BigDecimal("5000.00")));

        PRODUCTS.put("p1", new Product("p1", "iPhone 15 Pro", new BigDecimal("7999.00")));
        PRODUCTS.put("p2", new Product("p2", "MacBook Pro", new BigDecimal("15999.00")));
        PRODUCTS.put("p3", new Product("p3", "AirPods Pro", new BigDecimal("1999.00")));

        COUPONS.put("SAVE100", new Coupon("SAVE100", new BigDecimal("100.00"), 10));
        COUPONS.put("SAVE500", new Coupon("SAVE500", new BigDecimal("500.00"), 5));
        COUPONS.put("PERCENT20", new Coupon("PERCENT20", new BigDecimal("0.20"), 100, true));
    }

    // ==================== 支付逻辑漏洞 ====================

    /**
     * 支付逻辑漏洞 - 金额篡改
     *
     * 漏洞代码：客户端传来的金额直接使用，未验证
     *
     * 攻击请求:
     * POST /logic/payment?userId=user1&productId=p1&amount=0.01
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @param amount 支付金额（客户端可控）
     * @return 支付结果
     */
    @PostMapping("/payment")
    public String payment(@RequestParam("userId") String userId,
                          @RequestParam("productId") String productId,
                          @RequestParam("amount") BigDecimal amount) {
        User user = USERS.get(userId);
        Product product = PRODUCTS.get(productId);

        if (user == null) {
            return "用户不存在";
        }
        if (product == null) {
            return "商品不存在";
        }

        // 漏洞代码：直接使用客户端传来的金额，未验证与商品价格是否一致
        BigDecimal actualPrice = product.price;

        // 检查余额（基于篡改后的金额）
        if (user.balance.compareTo(amount) < 0) {
            return "余额不足: 需要 " + amount + "，当前余额 " + user.balance;
        }

        // 扣款（使用客户端传来的金额！）
        user.balance = user.balance.subtract(amount);

        // 创建订单
        String orderId = "ORD" + orderIdCounter.getAndIncrement();
        ORDERS.put(orderId, new Order(orderId, userId, productId, amount));

        return String.format(
            "=== 支付成功 ===\n" +
            "订单号: %s\n" +
            "商品: %s\n" +
            "商品原价: %s\n" +
            "支付金额: %s\n" +
            "节省: %s\n" +
            "剩余余额: %s\n" +
            "\n[!] 漏洞: 支付金额由客户端控制，可篡改！",
            orderId, product.name, actualPrice, amount, actualPrice.subtract(amount), user.balance
        );
    }

    /**
     * 支付逻辑漏洞 - 负数金额
     *
     * 漏洞代码：未验证金额为正数
     *
     * 攻击请求:
     * POST /logic/payment/signed?userId=user1&productId=p1&amount=-1000
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @param amount 支付金额（可为负数）
     * @return 支付结果
     */
    @PostMapping("/payment/signed")
    public String signedPayment(@RequestParam("userId") String userId,
                                 @RequestParam("productId") String productId,
                                 @RequestParam("amount") BigDecimal amount) {
        User user = USERS.get(userId);
        Product product = PRODUCTS.get(productId);

        if (user == null) {
            return "用户不存在";
        }
        if (product == null) {
            return "商品不存在";
        }

        // 漏洞代码：未验证金额为正数
        // 恶意用户可以传入负数金额，导致余额增加！

        // 扣款（如果 amount 为负，balance 会增加！）
        user.balance = user.balance.subtract(amount);

        // 创建订单
        String orderId = "ORD" + orderIdCounter.getAndIncrement();
        ORDERS.put(orderId, new Order(orderId, userId, productId, amount));

        return String.format(
            "=== 支付成功 ===\n" +
            "订单号: %s\n" +
            "商品: %s\n" +
            "支付金额: %s\n" +
            "当前余额: %s\n" +
            "\n[!] 漏洞: 负数金额会导致余额增加！",
            orderId, product.name, amount, user.balance
        );
    }

    /**
     * 支付逻辑漏洞 - 精度绕过
     *
     * 漏洞代码：使用浮点数进行金额计算
     *
     * @param userId 用户ID
     * @param amount 金额
     * @return 支付结果
     */
    @PostMapping("/payment/float")
    public String floatPayment(@RequestParam("userId") String userId,
                                @RequestParam("amount") double amount) {
        User user = USERS.get(userId);

        if (user == null) {
            return "用户不存在";
        }

        // 漏洞代码：使用 double 进行金额计算，可能产生精度问题
        double newBalance = user.balance.doubleValue() - amount;

        // 精度丢失可能导致少扣款
        user.balance = BigDecimal.valueOf(newBalance);

        return String.format(
            "=== 支付成功（浮点数） ===\n" +
            "支付金额: %s\n" +
            "剩余余额: %s\n" +
            "\n[!] 警告: 使用浮点数可能导致精度问题",
            amount, user.balance
        );
    }

    /**
     * 优惠券漏洞 - 重复使用
     *
     * 漏洞代码：未标记优惠券已使用
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @param couponCode 优惠券代码
     * @return 支付结果
     */
    @PostMapping("/payment/coupon")
    public String couponPayment(@RequestParam("userId") String userId,
                                 @RequestParam("productId") String productId,
                                 @RequestParam("couponCode") String couponCode) {
        User user = USERS.get(userId);
        Product product = PRODUCTS.get(productId);
        Coupon coupon = COUPONS.get(couponCode);

        if (user == null || product == null || coupon == null) {
            return "参数错误";
        }

        BigDecimal finalPrice = product.price;

        // 漏洞代码：没有检查优惠券是否已被使用
        // 没有记录使用次数
        if (coupon.isPercent) {
            finalPrice = finalPrice.multiply(BigDecimal.ONE.subtract(coupon.value));
        } else {
            finalPrice = finalPrice.subtract(coupon.value);
        }

        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        // 扣款
        if (user.balance.compareTo(finalPrice) < 0) {
            return "余额不足";
        }

        user.balance = user.balance.subtract(finalPrice);

        return String.format(
            "=== 支付成功（使用优惠券） ===\n" +
            "商品: %s\n" +
            "原价: %s\n" +
            "优惠券: %s\n" +
            "最终价格: %s\n" +
            "剩余余额: %s\n" +
            "\n[!] 漏洞: 优惠券可以无限次重复使用！",
            product.name, product.price, couponCode, finalPrice, user.balance
        );
    }

    /**
     * 优惠券漏洞 - 叠加使用
     *
     * 漏洞代码：允许多个优惠券叠加
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @param coupons 优惠券列表（逗号分隔）
     * @return 支付结果
     */
    @PostMapping("/payment/coupons")
    public String multipleCoupons(@RequestParam("userId") String userId,
                                    @RequestParam("productId") String productId,
                                    @RequestParam("coupons") String coupons) {
        User user = USERS.get(userId);
        Product product = PRODUCTS.get(productId);

        if (user == null || product == null) {
            return "参数错误";
        }

        BigDecimal finalPrice = product.price;
        StringBuilder usedCoupons = new StringBuilder();

        // 漏洞代码：允许叠加使用多个优惠券
        String[] couponArray = coupons.split(",");
        for (String code : couponArray) {
            Coupon coupon = COUPONS.get(code.trim());
            if (coupon != null) {
                if (coupon.isPercent) {
                    finalPrice = finalPrice.multiply(BigDecimal.ONE.subtract(coupon.value));
                } else {
                    finalPrice = finalPrice.subtract(coupon.value);
                }
                usedCoupons.append(code).append(" ");
            }
        }

        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        user.balance = user.balance.subtract(finalPrice);

        return String.format(
            "=== 支付成功（叠加优惠券） ===\n" +
            "商品: %s\n" +
            "原价: %s\n" +
            "使用优惠券: %s\n" +
            "最终价格: %s\n" +
            "剩余余额: %s\n" +
            "\n[!] 漏洞: 优惠券可以叠加使用，甚至导致免费！",
            product.name, product.price, usedCoupons, finalPrice, user.balance
        );
    }

    // ==================== 验证码漏洞 ====================

    /**
     * 验证码漏洞 - 可预测的验证码
     *
     * 漏洞代码：验证码使用简单的递增模式
     *
     * @param userId 用户ID
     * @return 验证码
     */
    @GetMapping("/captcha/generate")
    public String generateCaptcha(@RequestParam("userId") String userId) {
        // 漏洞代码：验证码是时间戳的后4位，非常容易预测
        String captcha = String.valueOf(System.currentTimeMillis() % 10000);

        CAPTCHAS.put(userId, new Captcha(userId, captcha, System.currentTimeMillis() + 300000));

        return String.format(
            "=== 验证码已生成 ===\n" +
            "用户ID: %s\n" +
            "验证码: %s\n" +
            "\n[!] 漏洞: 验证码基于时间戳，可预测！",
            userId, captcha
        );
    }

    /**
     * 验证码漏洞 - 可复用的验证码
     *
     * 漏洞代码：验证码验证后未失效
     *
     * @param userId 用户ID
     * @param captcha 验证码
     * @return 验证结果
     */
    @PostMapping("/captcha/verify")
    public String verifyCaptcha(@RequestParam("userId") String userId,
                                  @RequestParam("captcha") String captcha) {
        Captcha storedCaptcha = CAPTCHAS.get(userId);

        if (storedCaptcha == null) {
            return "验证码不存在或已过期";
        }

        // 漏洞代码：验证后没有删除验证码，可以重复使用
        if (storedCaptcha.code.equals(captcha) && System.currentTimeMillis() < storedCaptcha.expireTime) {
            return "验证码正确！\n[!] 漏洞: 验证码可以无限次重复使用！";
        }

        return "验证码错误";
    }

    /**
     * 验证码漏洞 - 固定验证码
     *
     * 漏洞代码：特定用户使用固定验证码
     *
     * @param userId 用户ID
     * @param captcha 验证码
     * @return 验证结果
     */
    @PostMapping("/captcha/admin")
    public String adminCaptcha(@RequestParam("userId") String userId,
                                @RequestParam("captcha") String captcha) {
        // 漏洞代码：admin 用户永远使用固定验证码 1234
        if ("admin".equals(userId)) {
            if ("1234".equals(captcha)) {
                return "管理员验证码正确！\n[!] 漏洞: admin 使用固定验证码！";
            }
        }

        return "验证码错误";
    }

    /**
     * 验证码漏洞 - 穷举绕过
     *
     * 漏洞代码：4位纯数字验证码，只有10000种可能
     *
     * @param userId 用户ID
     * @param captcha 验证码
     * @return 验证结果
     */
    @PostMapping("/captcha/bruteforce")
    public String bruteForceCaptcha(@RequestParam("userId") String userId,
                                     @RequestParam("captcha") String captcha) {
        // 漏洞代码：4位纯数字验证码，容易被暴力破解
        // 没有频率限制

        if (captcha == null || captcha.length() != 4 || !captcha.matches("\\d+")) {
            return "验证码格式错误";
        }

        // 模拟验证（实际应该与存储的验证码比较）
        // 这里假设正确的验证码是当前时间的后4位
        String correctCaptcha = String.valueOf(System.currentTimeMillis() % 10000);

        if (captcha.equals(correctCaptcha)) {
            return "验证码正确";
        }

        return "验证码错误，可以尝试暴力破解（只有10000种可能）\n[!] 漏洞: 没有频率限制，可以暴力破解！";
    }

    // ==================== 安全版本 ====================

    /**
     * 安全版本 - 服务端验证价格
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 支付结果
     */
    @PostMapping("/safe/payment")
    public String safePayment(@RequestParam("userId") String userId,
                               @RequestParam("productId") String productId) {
        User user = USERS.get(userId);
        Product product = PRODUCTS.get(productId);

        if (user == null || product == null) {
            return "参数错误";
        }

        // 安全代码：从服务端获取商品价格，不信任客户端
        BigDecimal amount = product.price;

        if (user.balance.compareTo(amount) < 0) {
            return "余额不足";
        }

        user.balance = user.balance.subtract(amount);

        return String.format(
            "=== 支付成功（安全版本） ===\n" +
            "商品: %s\n" +
            "支付金额: %s\n" +
            "剩余余额: %s\n" +
            "\n[✓] 金额由服务端验证，不可篡改",
            product.name, amount, user.balance
        );
    }

    /**
     * 安全版本 - 一次性验证码
     *
     * @param userId 用户ID
     * @param captcha 验证码
     * @return 验证结果
     */
    @PostMapping("/safe/captcha")
    public String safeCaptcha(@RequestParam("userId") String userId,
                                @RequestParam("captcha") String captcha) {
        Captcha storedCaptcha = CAPTCHAS.get(userId);

        if (storedCaptcha == null) {
            return "验证码不存在";
        }

        if (System.currentTimeMillis() > storedCaptcha.expireTime) {
            CAPTCHAS.remove(userId);
            return "验证码已过期";
        }

        if (storedCaptcha.code.equals(captcha)) {
            // 安全代码：验证后删除验证码，防止重复使用
            CAPTCHAS.remove(userId);
            return "验证码正确（验证码已失效）\n[✓] 验证码一次性使用";
        }

        return "验证码错误";
    }

    /**
     * 信息端点
     *
     * @return 测试信息
     */
    @GetMapping("/info")
    public String info() {
        return String.format(
            "业务逻辑漏洞演示%n" +
            "======================%n" +
            "Java Version: %s%n" +
            "OS: %s %s%n" +
            "%n" +
            "支付逻辑漏洞端点:%n" +
            "- POST /logic/payment?userId=user1&productId=p1&amount=0.01%n" +
            "- POST /logic/payment/signed?userId=user1&productId=p1&amount=-1000%n" +
            "- POST /logic/payment/float?userId=user1&amount=0.1%n" +
            "- POST /logic/payment/coupon?userId=user1&productId=p1&couponCode=SAVE100%n" +
            "- POST /logic/payment/coupons?userId=user1&productId=p1&coupons=SAVE100,SAVE500%n" +
            "%n" +
            "验证码漏洞端点:%n" +
            "- GET /logic/captcha/generate?userId=user1%n" +
            "- POST /logic/captcha/verify?userId=user1&captcha=1234%n" +
            "- POST /logic/captcha/admin?userId=admin&captcha=1234%n" +
            "- POST /logic/captcha/bruteforce?userId=user1&captcha=0000%n" +
            "%n" +
            "安全端点:%n" +
            "- POST /logic/safe/payment?userId=user1&productId=p1%n" +
            "- POST /logic/safe/captcha?userId=user1&captcha=1234",
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            System.getProperty("os.version")
        );
    }

    // ==================== 数据模型 ====================

    static class User {
        String userId;
        BigDecimal balance;

        User(String userId, BigDecimal balance) {
            this.userId = userId;
            this.balance = balance;
        }
    }

    static class Product {
        String productId;
        String name;
        BigDecimal price;

        Product(String productId, String name, BigDecimal price) {
            this.productId = productId;
            this.name = name;
            this.price = price;
        }
    }

    static class Coupon {
        String code;
        BigDecimal value;
        int maxUses;
        boolean isPercent;

        Coupon(String code, BigDecimal value, int maxUses) {
            this.code = code;
            this.value = value;
            this.maxUses = maxUses;
            this.isPercent = false;
        }

        Coupon(String code, BigDecimal value, int maxUses, boolean isPercent) {
            this.code = code;
            this.value = value;
            this.maxUses = maxUses;
            this.isPercent = isPercent;
        }
    }

    static class Order {
        String orderId;
        String userId;
        String productId;
        BigDecimal amount;

        Order(String orderId, String userId, String productId, BigDecimal amount) {
            this.orderId = orderId;
            this.userId = userId;
            this.productId = productId;
            this.amount = amount;
        }
    }

    static class Captcha {
        String userId;
        String code;
        long expireTime;

        Captcha(String userId, String code, long expireTime) {
            this.userId = userId;
            this.code = code;
            this.expireTime = expireTime;
        }
    }
}
