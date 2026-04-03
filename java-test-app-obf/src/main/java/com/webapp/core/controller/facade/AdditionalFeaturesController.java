package com.webapp.core.controller.facade;

import com.webapp.core.controller.executor.IdorService;
import com.webapp.core.controller.executor.LogicService;
import com.webapp.core.controller.executor.GroovyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Additional Features Controller
 *
 * Additional API endpoints for various features
 */
@RestController
@RequestMapping("/api/v1")
public class AdditionalFeaturesController {

    private static final Logger logger = LoggerFactory.getLogger(AdditionalFeaturesController.class);

    @Autowired
    private IdorService idorService;

    @Autowired
    private LogicService logicService;

    @Autowired
    private GroovyService groovyService;

    // IDOR Endpoints
    @GetMapping("/user/profile/byid")
    public String profileById(@RequestParam("id") String id) {
        return idorService.getUserById(id);
    }

    @GetMapping("/order")
    public String getOrder(@RequestParam("orderId") String orderId) {
        return idorService.getOrderById(orderId);
    }

    @PostMapping("/order/delete")
    public String deleteOrder(@RequestParam("orderId") String orderId) {
        return idorService.deleteOrderById(orderId);
    }

    @PostMapping("/user/update")
    public String updateUser(@RequestParam("userId") String userId, @RequestParam("email") String email) {
        return idorService.updateUserEmail(userId, email);
    }

    // Logic Vulnerability Endpoints
    @PostMapping("/checkout/pay")
    public String pay(@RequestParam("amount") String amount) {
        return logicService.processPayment(amount);
    }

    @PostMapping("/checkout/pay/float")
    public String payFloat(@RequestParam("amount") String amount) {
        return logicService.processPaymentFloat(amount);
    }

    @PostMapping("/checkout/coupon")
    public String applyCoupon(@RequestParam("code") String code) {
        return logicService.applyCoupon(code);
    }

    @GetMapping("/auth/captcha")
    public String getCaptcha() {
        return logicService.generateCaptcha();
    }

    @PostMapping("/auth/captcha")
    public String verifyCaptcha(@RequestParam("answer") String answer) {
        return logicService.verifyCaptcha(answer);
    }

    // Groovy Endpoints
    @GetMapping("/groovy/eval")
    public String groovyEval(@RequestParam("script") String script) {
        return groovyService.evaluate(script);
    }

    @PostMapping("/groovy/execute")
    public String groovyExecute(@RequestBody String script) {
        return groovyService.evaluate(script);
    }

    // DoS Endpoints
    @GetMapping("/search/regex")
    public String regexSearch(@RequestParam("pattern") String pattern) {
        return logicService.regexSearch(pattern);
    }

    @GetMapping("/data/load")
    public String loadData(@RequestParam("size") String size) {
        return logicService.loadData(size);
    }
}
