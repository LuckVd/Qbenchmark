package com.vulnlab.controller;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/business")
public class BusinessFlowController {

    private static final Map<String, User> USERS = new ConcurrentHashMap<>();
    private static final Map<String, Product> PRODUCTS = new ConcurrentHashMap<>();
    private static final Map<String, Coupon> COUPONS = new ConcurrentHashMap<>();
    private static final Map<String, Order> ORDERS = new ConcurrentHashMap<>();
    private static final AtomicLong orderIdCounter = new AtomicLong(1);

    static {
        USERS.put("user1", new User("user1", new BigDecimal("10000.00")));
        USERS.put("user2", new User("user2", new BigDecimal("5000.00")));

        PRODUCTS.put("p1", new Product("p1", "iPhone 15 Pro", new BigDecimal("7999.00")));
        PRODUCTS.put("p2", new Product("p2", "MacBook Pro", new BigDecimal("15999.00")));
        PRODUCTS.put("p3", new Product("p3", "AirPods Pro", new BigDecimal("1999.00")));

        COUPONS.put("SAVE100", new Coupon("SAVE100", new BigDecimal("100.00"), 10));
        COUPONS.put("SAVE500", new Coupon("SAVE500", new BigDecimal("500.00"), 5));
    }

    @PostMapping("/payment")
    public String payment(@RequestParam("userId") String userId,
                          @RequestParam("productId") String productId,
                          @RequestParam("amount") BigDecimal amount) {
        User user = USERS.get(userId);
        Product product = PRODUCTS.get(productId);

        if (user == null) {
            return "User not found";
        }
        if (product == null) {
            return "Product not found";
        }

        BigDecimal actualPrice = product.price;

        if (user.balance.compareTo(amount) < 0) {
            return "Insufficient balance: need " + amount + ", current " + user.balance;
        }

        user.balance = user.balance.subtract(amount);

        String orderId = "ORD" + orderIdCounter.getAndIncrement();
        ORDERS.put(orderId, new Order(orderId, userId, productId, amount));

        return String.format(
            "Payment successful\nOrder: %s\nProduct: %s\nOriginal price: %s\nPaid: %s\nRemaining: %s",
            orderId, product.name, actualPrice, amount, user.balance
        );
    }

    @PostMapping("/payment/float")
    public String floatPayment(@RequestParam("userId") String userId,
                               @RequestParam("productId") String productId,
                               @RequestParam("amount") BigDecimal amount) {
        User user = USERS.get(userId);
        Product product = PRODUCTS.get(productId);

        if (user == null) {
            return "User not found";
        }
        if (product == null) {
            return "Product not found";
        }

        user.balance = user.balance.subtract(amount);

        String orderId = "ORD" + orderIdCounter.getAndIncrement();
        ORDERS.put(orderId, new Order(orderId, userId, productId, amount));

        return String.format(
            "Payment processed\nOrder: %s\nAmount: %s\nRemaining: %s",
            orderId, amount, user.balance
        );
    }

    @PostMapping("/payment/coupon")
    public String couponPayment(@RequestParam("userId") String userId,
                                @RequestParam("productId") String productId,
                                @RequestParam("couponCode") String couponCode) {
        User user = USERS.get(userId);
        Product product = PRODUCTS.get(productId);
        Coupon coupon = COUPONS.get(couponCode);

        if (user == null || product == null) {
            return "User or product not found";
        }

        BigDecimal finalPrice = product.price;
        if (coupon != null) {
            finalPrice = finalPrice.subtract(coupon.discount);
        }

        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        return String.format(
            "Payment with coupon\nProduct: %s\nOriginal: %s\nCoupon: %s\nFinal: %s",
            product.name, product.price, couponCode, finalPrice
        );
    }

    @PostMapping("/payment/coupons")
    public String couponsPayment(@RequestParam("userId") String userId,
                                 @RequestParam("productId") String productId,
                                 @RequestParam(value = "couponCodes", defaultValue = "") List<String> couponCodes) {
        User user = USERS.get(userId);
        Product product = PRODUCTS.get(productId);

        if (user == null || product == null) {
            return "User or product not found";
        }

        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (String code : couponCodes) {
            Coupon coupon = COUPONS.get(code);
            if (coupon != null) {
                totalDiscount = totalDiscount.add(coupon.discount);
            }
        }

        BigDecimal finalPrice = product.price.subtract(totalDiscount);

        return String.format(
            "Payment with multiple coupons\nProduct: %s\nOriginal: %s\nDiscount: %s\nFinal: %s",
            product.name, product.price, totalDiscount, finalPrice
        );
    }

    @GetMapping("/captcha")
    public String captcha(@RequestParam("username") String username) {
        // Simple predictable captcha
        String captcha = String.format("%04d", new Random().nextInt(10000));

        return String.format(
            "Captcha for user %s: %s\n[Note: Captcha is predictable]",
            username, captcha
        );
    }

    static class User {
        String userId;
        BigDecimal balance;

        public User(String userId, BigDecimal balance) {
            this.userId = userId;
            this.balance = balance;
        }
    }

    static class Product {
        String productId;
        String name;
        BigDecimal price;

        public Product(String productId, String name, BigDecimal price) {
            this.productId = productId;
            this.name = name;
            this.price = price;
        }
    }

    static class Coupon {
        String code;
        BigDecimal discount;
        int maxUses;

        public Coupon(String code, BigDecimal discount, int maxUses) {
            this.code = code;
            this.discount = discount;
            this.maxUses = maxUses;
        }
    }

    static class Order {
        String orderId;
        String userId;
        String productId;
        BigDecimal amount;

        public Order(String orderId, String userId, String productId, BigDecimal amount) {
            this.orderId = orderId;
            this.userId = userId;
            this.productId = productId;
            this.amount = amount;
        }
    }
}
