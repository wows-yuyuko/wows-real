package com.shinoaki.wows.real.mqtt;

/**
 * @author Xun
 * create or update time = 2023/7/25 15:20 星期二
 */

public class MqttConfig {

    private String broker;

    private String clientId;

    private String userName;

    private String password;

    private String topic;

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
