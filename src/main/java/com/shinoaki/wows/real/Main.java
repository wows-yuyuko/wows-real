package com.shinoaki.wows.real;

import com.fasterxml.jackson.core.type.TypeReference;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.real.config.Config;
import com.shinoaki.wows.real.http.HttpService;
import com.shinoaki.wows.real.mqtt.MqttService;
import com.shinoaki.wows.real.timer.ServerTimer;
import com.shinoaki.wows.real.ws.server.WowsWebsocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;

/**
 * @author Xun
 * create or update time = 2023/7/20 16:46 星期四
 */
public class Main {
    public static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.setProperty("logback.configurationFile", "config/logback.xml");
        try {
            JsonUtils json = new JsonUtils();
            log.info("加载配置信息... ");
            var config = json.parse(readFileData(fileConfig("config.json")), new TypeReference<Config>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
            MqttService service = new MqttService(Config.mqttConfig(json));
            service.connect();
            if (config.ws()) {
                WowsWebsocket websocket = new WowsWebsocket();
                websocket.start(config.wsPort(), "/yuyuko");
            }
            if (config.http()) {
                HttpService httpService = new HttpService(new InetSocketAddress(config.httpPort()));
                log.info("http服务启动! port={}", config.httpPort());
                httpService.start();
            }
            log.info("初始化成功...");
            //服务配置
            ServerTimer timer = new ServerTimer();
            timer.updateConfig();
            timer.timerQueryAccountId();
            timer.timerSubTopic(service);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("线程异常!", e);
        } catch (Exception e) {
            log.error("服务运行异常!", e);
        }
    }


    public static File fileConfig(String fileName) {
        return new File(pathHome() + "config" + File.separator + fileName);
    }

    public static String readFileData(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            return new String(in.readAllBytes());
        }
    }

    /**
     * 项目根目录 File.separator 结尾
     *
     * @return
     */
    public static String pathHome() {
        return System.getProperty("user.dir") + File.separator;
    }

}
