package com.hmdp;

import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class TokenGeneratorTest {

    @Autowired
    private IUserService userService;

    @Test
    public void generateTokens() throws Exception {
        List<String> phones = readPhonesFromCsv("massive_phones.csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("tokens.csv"))) {
            writer.write("token\n");

            for (String phone : phones) {
                // 1. 先发送验证码
                userService.sendCode(phone, null);

                // 2. 执行登录
                LoginFormDTO form = new LoginFormDTO();
                form.setPhone(phone);
                form.setCode("123456"); // 必须与sendCode存入Redis的验证码一致

                Result result = userService.login(form, null);

                /*// 3. 严格校验状态码
                if (result.getCode() == 200) {
                    // 获取实际的token字段（根据你的Result结构调整）
                    String token = (String) result.getData();
                    writer.write(token + "\n");
                    System.out.println("成功生成：" + phone);
                } else {
                    System.err.println("失败：" + phone + "，原因：" + result.getMsg());
                }*/
                String token = (String) result.getData();
                writer.write(token + "\n");
            }
        }
    }


    private List<String> readPhonesFromCsv(String filename) throws IOException {
        List<String> phones = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine(); // 跳过标题
            String line;
            while ((line = reader.readLine()) != null) {
                phones.add(line.trim());
            }
        }
        return phones;
    }
}
