package com.syy.security.chapter03.config;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 * @Description: 图形验证码配置
 * @Author: cuiweiman
 * @Since: 2021/5/26 下午5:36
 */
public class VerifyCodeConfig {

    // 验证码长度
    private static final int LENGTH = 4;

    // 生成验证码图片的宽度
    private static final int WIDTH = 100;

    // 生成验证码图片的高度
    private static final int HEIGHT = 50;

    private final String[] fontNames = {"宋体", "楷体", "隶书", "微软雅黑"};

    // 定义验证码图片的背景颜色为白色
    private final Color bgColor = new Color(255, 255, 255);
    private final Random random = new Random();
    private static final String CODES = "12356789abcdefghjkmnpqrstwxyzABCDEFGHJKMNPQRSTWXYZ";

    // 记录随机字符串
    private String text;

    /**
     * 获取一个随意颜色
     *
     * @return 随机颜色
     */
    private Color randomColor() {
        int red = random.nextInt(150);
        int green = random.nextInt(150);
        int blue = random.nextInt(150);
        return new Color(red, green, blue);
    }

    /**
     * 获取一个随机字体
     *
     * @return 随机字体
     */
    private Font randomFont() {
        String name = fontNames[random.nextInt(fontNames.length)];
        int style = random.nextInt(4);
        int size = random.nextInt(5) + 24;
        return new Font(name, style, size);
    }

    /**
     * 获取一个随机字符
     *
     * @return 随机字符
     */
    private char randomChar() {
        return CODES.charAt(random.nextInt(CODES.length()));
    }

    /**
     * 创建一个空白的BufferedImage对象
     *
     * @return 空白的BufferedImage对象
     */
    private BufferedImage createImage() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setColor(bgColor);// 设置验证码图片的背景颜色
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        return image;
    }

    public BufferedImage getImage() {
        BufferedImage image = createImage();
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            String s = randomChar() + "";
            sb.append(s);
            g2.setColor(randomColor());
            g2.setFont(randomFont());
            float x = i * WIDTH * 1.0f / 4;
            g2.drawString(s, x, HEIGHT - 15);
        }
        this.text = sb.toString();
        drawLine(image);
        return image;
    }

    /**
     * 绘制干扰线
     *
     * @param image 验证码图片
     */
    private void drawLine(BufferedImage image) {
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        int num = 5;
        for (int i = 0; i < num; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g2.setColor(randomColor());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    public String getText() {
        return text;
    }

    public static void output(BufferedImage image, OutputStream out) throws IOException {
        ImageIO.write(image, "JPEG", out);
    }

}
