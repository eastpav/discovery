package org.eastpav.discovery.config;

import org.json.JSONObject;

import java.util.Properties;
import java.util.Set;

/**
 * JSON配置字符串解析器.
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/22.
 */
public class JsonConfigResolver implements ConfigResolve {

    @Override
    public Properties resolve(String data) throws Exception {
        JSONObject obj = new JSONObject(data);
        Properties ps = new Properties();
        Set<String> set = obj.keySet();
        set.forEach(name -> ps.setProperty(name, obj.getString(name)));
        return ps;
    }
}
