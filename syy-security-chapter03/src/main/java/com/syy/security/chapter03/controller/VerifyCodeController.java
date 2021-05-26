package com.syy.security.chapter03.controller;

import com.syy.security.chapter03.config.VerifyCodeConfig;
import com.syy.security.chapter03.constants.NormalConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @Description: 验证码 接口
 * @Author: cuiweiman
 * @Since: 2021/5/26 下午5:42
 */
@RestController
public class VerifyCodeController {

    @GetMapping("/verifyCode")
    public void code(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        VerifyCodeConfig vc = new VerifyCodeConfig();
        BufferedImage image = vc.getImage();
        String text = vc.getText();
        // 将 验证码存储到 session 中
        HttpSession session = req.getSession();
        session.setAttribute(NormalConstants.SESSION_KEY_VERIFY_CODE, text);

        VerifyCodeConfig.output(image, resp.getOutputStream());
    }

}
