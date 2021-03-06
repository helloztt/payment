package me.jiangcai.payment.test;

import com.google.common.base.Predicate;
import me.jiangcai.demo.project.DatasourceConfig;
import me.jiangcai.demo.project.DemoProjectConfig;
import me.jiangcai.demo.project.entity.DemoTradeOrder;
import me.jiangcai.demo.project.repository.DemoTradeOrderRepository;
import me.jiangcai.lib.test.SpringWebTest;
import me.jiangcai.payment.PaymentForm;
import me.jiangcai.payment.service.PaymentService;
import me.jiangcai.payment.test.page.IndexPage;
import me.jiangcai.payment.test.page.PayPage;
import org.apache.commons.lang.RandomStringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import java.math.BigDecimal;

/**
 * @author CJ
 */
@ContextConfiguration(classes = {DatasourceConfig.class, DemoProjectConfig.class})
@WebAppConfiguration
public abstract class PaymentTest extends SpringWebTest {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private DemoTradeOrderRepository demoTradeOrderRepository;
    private int waitSeconds;

    /**
     * 测试订单流程
     *
     * @param formName 支付方式名称
     */
    protected void testOrderFor(String formName) throws Exception {
        testOrderFor(formName, true, 10);
    }

    /**
     * 测试订单流程
     *
     * @param formName    支付方式名称
     * @param mockPay     是否模拟支付
     * @param waitSeconds 等待支付完成最长时间
     */
    protected void testOrderFor(String formName, boolean mockPay, int waitSeconds) throws Exception {
        PayPage payPage = makeOrderFor(formName, mockPay);
        this.waitSeconds = waitSeconds;

        // 点击支付 然后等待 即可进入支付成功页面
        if (mockPay)
            payPage.makePay();

        assertPaySuccess();

        payPage = makeOrderFor(formName, mockPay);

        // 模拟支付
        DemoTradeOrder order = demoTradeOrderRepository.getOne(payPage.orderId());
        if (mockPay)
            paymentService.mockPay(order);

        assertPaySuccess();
    }

    protected void testOrderFor(Class<? extends PaymentForm> form) {

    }

    protected void assertPaySuccess() {
        new WebDriverWait(driver, waitSeconds)
                .until((Predicate<WebDriver>) input
                        -> input != null && input.getTitle().equals("订单支付成功"));
    }

    protected PayPage makeOrderFor(String formName) {
        return makeOrderFor(formName, true);
    }

    protected PayPage makeOrderFor(String formName, boolean mockPay) {
        driver.get("http://localhost/");
        IndexPage indexPage = initPage(IndexPage.class);

        double value = 100d + (double) random.nextInt(200) + random.nextDouble();
        // 便宜点……
        if (!mockPay)
            value = 1d + random.nextDouble();
        BigDecimal amount = BigDecimal.valueOf(value);
        String name = "模拟商品" + RandomStringUtils.randomAlphabetic(6);

        return indexPage.makeOrder(name, amount, formName);
    }

}
