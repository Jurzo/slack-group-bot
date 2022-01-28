package hh.slackbot.Slackbot.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.util.ResourceUtils;

public class SlackTemplateUtils {

    public static String getTemplate(String filename) {
        String filePath = String.format("classpath:slack/json/%s", filename);
        try {
            File file = ResourceUtils.getFile(filePath);
            InputStream in = new FileInputStream(file);
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            in.close();
            return content;
        } catch (IOException e) {
            //TODO: handle exception
        }

        return null;
    }
}
