package com.linweili.miaosha.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CodeUtil {
    private static int width = 90;
    private static int height = 20;
    private static int codeCount = 4;
    private static int xx = 15;
    private static int fontHeight = 18;
    private static int codeY = 16;
    private static char[] codeSequence = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9'
    };

    /**
     * 生成一个Map集合
     * code为生成的验证码
     * codePic为生成的验证码BufferedImage对象
     */
    public static Map<String, Object> generateCodeAndPic() {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bufferedImage.getGraphics();
        Random random = new Random();
        //填充白色
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        //设置字体
        Font font = new Font("Fixedsys", Font.BOLD, fontHeight);
        graphics.setFont(font);
        //画边框
        graphics.setColor(Color.BLACK);
        graphics.drawRect(0, 0, width - 1, height - 1);

        //随机生成40条干扰线
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < 30; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            graphics.drawLine(x, y, x + xl, y + yl);
        }

        StringBuffer randomCode = new StringBuffer();
        int red = 0; int green = 0; int blue = 0;
        //随机生成codeCount个数字的验证码
        for (int i = 0; i < codeCount; i++) {
            //得到随机产生的验证码数字
            String code = String.valueOf(codeSequence[random.nextInt(36)]);
            //为数字产生随机的颜色分量，构造颜色值
            red = random.nextInt(255);
            green = random.nextInt(255);
            blue = random.nextInt(255);

            //用随机产生的颜色将验证码绘制到图像中
            graphics.setColor(new Color(red, green, blue));
            graphics.drawString(code, (i + 1) * xx, codeY);

            //将数字拼接进入验证码
            randomCode.append(code);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("code", randomCode);
        map.put("codePic", bufferedImage);
        return map;
    }
}
