package com.shinoaki.wows.real.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shinoaki.wows.api.utils.JsonUtils;
import com.shinoaki.wows.real.cache.SubUserCache;
import com.shinoaki.wows.real.service.WsService;
import com.shinoaki.wows.real.wows.PlayerBattleInfo;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Xun
 * create or update time = 2023/7/25 16:05 星期二
 */
public class MqttService {
    public static final Logger log = LoggerFactory.getLogger(MqttService.class);
    private static final JsonUtils json = new JsonUtils();
    private static final MemoryPersistence persistence = new MemoryPersistence();
    private static final MqttConnectionOptions connOpts = new MqttConnectionOptions();
    private MqttClient mqttClient = null;
    private final MqttConfig config;
    private final MyMqttMessageListener myMqttMessageListener;

    static {
        connOpts.setCleanStart(true);
        connOpts.setAutomaticReconnect(true);
    }

    public MqttService(MqttConfig config) {
        this.config = config;
        this.myMqttMessageListener = new MyMqttMessageListener();
    }


    public void connect() {
        if (mqttClient != null) {
            return;
        }
        connOpts.setUserName(config.getUserName());
        connOpts.setPassword(config.getPassword().getBytes(StandardCharsets.UTF_8));
        try {
            mqttClient = new MqttClient(config.getBroker(), config.getClientId(), persistence);
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void disconnected(MqttDisconnectResponse disconnectResponse) {
                    log.error("mqtt 连接断开...");
                }

                @Override
                public void mqttErrorOccurred(MqttException exception) {
                    log.error("mqtt error ", exception);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                }

                @Override
                public void deliveryComplete(IMqttToken token) {

                }

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    log.info("mqtt 建立连接!");
                    onMessage();
                }

                @Override
                public void authPacketArrived(int reasonCode, MqttProperties properties) {

                }
            });
            mqttClient.connect(connOpts);
        } catch (MqttException e) {
            log.error("mqtt 服务器连接异常!");
        }
        log.info("mqtt 服务器连接成功!");
    }

    public void onMessage() {
        List<Long> longs = SubUserCache.allAccountId();
        if (longs.isEmpty()) {
            return;
        }
        MqttSubscription[] subscriptions = new MqttSubscription[longs.size()];
        for (int i = 0; i < longs.size(); i++) {
            subscriptions[i] = new MqttSubscription(config.getTopic() + "/" + longs.get(i), 0);
        }
        IMqttMessageListener[] messageListeners = new IMqttMessageListener[]{
                myMqttMessageListener
        };
        try {
            mqttClient.subscribe(subscriptions, messageListeners);
        } catch (MqttException e) {
            log.error("订阅消息异常!", e);
        }
    }

    public static class MyMqttMessageListener implements IMqttMessageListener {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            try {
                PlayerBattleInfo battleInfo = json.parse(new String(message.getPayload()), PlayerBattleInfo.class);
                for (var info : battleInfo.infoList()) {
                    var map = SubUserCache.check(info.accountId());
                    if (!map.isEmpty()) {
                        WsService.playerGame(battleInfo, map);
                    }
                }
            } catch (JsonProcessingException e) {
                log.error("序列化消息异常!", e);
            }
        }
    }
}
