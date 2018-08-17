package org.eastpav.discovery.util;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * @描述: SSL工具类
 * @作者: 张尧
 * @创建时间: 2018-08-16 16:59:06
 */
public class SSLUtil {
    public static SSLContext getSSLContext(String keyPath, String keyPassword, String trustPath, String trustPassword) throws Exception {
        InputStream inputStream = null;
        InputStream inputStream2 = null;

        try {
            inputStream = new FileInputStream(keyPath);
            inputStream2 = new FileInputStream(trustPath);
        } catch (FileNotFoundException e) {
            inputStream = SSLUtil.class.getResourceAsStream("/" + keyPath);
            inputStream2 = SSLUtil.class.getResourceAsStream("/"+trustPath);
        }

        char[] keyPassphrase = keyPassword.toCharArray();
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(inputStream, keyPassphrase);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, keyPassphrase);

        char[] trustPassphrase = trustPassword.toCharArray();
        KeyStore tks = KeyStore.getInstance("JKS");
        tks.load(inputStream2, trustPassphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(tks);
        SSLContext context = SSLContext.getInstance("SSLv3");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context;
    }
}
