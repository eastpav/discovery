package org.eastpav.discovery.config;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * property字符串转换为Properties对象的解析器.
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/21.
 */
public class PropConfigResolver implements ConfigResolve {

    @Override
    public Properties resolve(String data) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(data.getBytes());
        Properties ps = new Properties();
        ps.load(inputStream);

        return ps;
    }
}
