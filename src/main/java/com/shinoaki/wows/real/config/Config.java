package com.shinoaki.wows.real.config;

import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.real.Main;
import com.shinoaki.wows.real.mqtt.MqttConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author Xun
 * create or update time = 2023/7/26 18:21 星期三
 */
public record Config(int wsPort) {


    public static MqttConfig mqttConfig(JsonUtils json) throws IOException {
        File file = Main.fileConfig("mqtt.json");
        if (file.exists()) {
            return json.parse(Main.readFileData(file), MqttConfig.class);
        } else {
            MqttConfig c = new MqttConfig();
            c.setBroker("tcp://124.222.76.59:1883");
            c.setClientId("yuyuko-" + UUID.randomUUID());
            c.setUserName("yuyuko-user");
            c.setPassword("yuyuko");
            c.setTopic("yuyuko/wows/real");
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(json.toJson(c).getBytes(StandardCharsets.UTF_8));
            }
            return c;
        }
    }
}
