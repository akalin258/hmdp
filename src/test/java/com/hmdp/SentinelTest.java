package com.hmdp;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class SentinelTest {
    @Test
    public void testHello(){
        FlowRule rule = new FlowRule();
        rule.setResource("hello");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(20);
        // 需要将规则添加到管理器
        List<FlowRule> rules = new ArrayList<>();
        rules.add(rule);
        FlowRuleManager.loadRules(rules); //
        while(true) {
            try(Entry entry = SphU.entry("hello")) { // 尝试访问资源
                System.out.println("hello sentinel"); // 允许访问时的输出
            } catch (Exception e) {
                System.out.println("blocked");       // 被限流时的输出
            }
        }

    }

}
