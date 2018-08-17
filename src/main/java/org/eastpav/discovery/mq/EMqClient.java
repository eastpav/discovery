package org.eastpav.discovery.mq;

import com.sun.scenario.effect.impl.prism.PrImage;
import lombok.extern.slf4j.Slf4j;
import org.eastpav.discovery.util.SSLUtil;
import org.eclipse.paho.client.mqttv3.*;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import java.util.*;

/**
 * @描述: EMQ 客户端
 * @作者: 张尧
 * @创建时间: 2018-08-16 11:08:11
 */
@Slf4j
public class EMqClient implements MqClient {
    private String host;
    private int port;
    private String username;
    private String password;
    private String clientId;

    private boolean sslEnable;
    private SSLContext context;

    private MqttClient client;
    private Set<String> topicSet;
    private MessageListener listener;

    private boolean initialized;

    public EMqClient() {
        this("localhost", 1883);
    }

    public EMqClient(String host, int port) {
        this(host, port, Long.toString(new Random().nextLong()));
    }

    public EMqClient(String host, int port, String clientId) {
        this(host, port, null, null, clientId);
    }

    public EMqClient(String host, int port, String username, String password) {
        this(host, port, username, password, Long.toString(new Random().nextLong()));
    }

    public EMqClient(String host, int port, String username, String password, String clientId) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.clientId = clientId;
        topicSet = new HashSet<>();
    }

    @Override
    public void addTopic(String topic) {
        topicSet.add(topic);
    }

    @Override
    public void doConnect() throws Exception {
        if(initialized) {
            return;
        }

        String uri;

        if(sslEnable) {
            uri = "ssl://" + host + ":" + port;
        } else {
            uri = "tcp://" + host + ":" + port;
        }

        client = new MqttClient(uri, clientId, null);
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setConnectionTimeout(10);
        mqttConnectOptions.setKeepAliveInterval(10);
        mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        if(username != null && password != null) {
            mqttConnectOptions.setUserName(username);
            mqttConnectOptions.setPassword(password.toCharArray());
        }

        if (sslEnable) {
            mqttConnectOptions.setSocketFactory(context.getSocketFactory());
        }

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                log.info("{}connect to {} success", reconnect ? "re" : "", serverURI);
                if(reconnect) {
                    // setCleanSession 为true，断连后需要重新订阅
                    resubscribe();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                log.info("connect lost: {}", cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (listener != null) {
                    listener.onMessage(topic, message.getPayload());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        client.connect(mqttConnectOptions);
        if (!topicSet.isEmpty()) {
            for (String t : topicSet) {
                client.subscribe(t, 0);
            }
        }

        initialized = true;
    }

    @Override
    public boolean subscribe(String topic) {
        if(!initialized) {
            log.error("subscribe {} failed:not initialize", topic);
            return false;
        }

        topicSet.add(topic);
        try {
            client.subscribe(topic, 0);
        } catch (MqttException e) {
            log.error("subscribe {} failed:{}", topic, e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public void send(String topic, byte[] data) {
        if(client != null) {
            try {
                client.publish(topic, data, 0, false);
            } catch (MqttException e) {
                log.error("send failed:{}", e.getMessage());
            }
        } else {
            log.warn("send failed:not initialize");
        }
    }

    @Override
    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void setSSL(String keyPath, String keyPassword, String trustPath, String trustPassword) throws Exception {
        context = SSLUtil.getSSLContext(keyPath, keyPassword, trustPath, trustPassword);
        sslEnable = true;
    }

    @Override
    public void close() {
        if(client == null) {
            return;
        }

        try {
            client.disconnect();
            client.close();
            initialized = false;
        } catch (MqttException e) {
            log.error("close failed:{}", e.getMessage());
        }
    }


    private void resubscribe() {
        try {
            if (!topicSet.isEmpty()) {
                for (String t : topicSet) {
                    client.subscribe(t, 0);
                }
            }
        } catch (MqttException e) {
            log.error("resubscribe failed:{}", e.getMessage());
        }
    }
}
