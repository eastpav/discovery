package org.eastpav;

import org.apache.zookeeper.data.Stat;
import org.eastpav.discovery.Discoverer;
import org.eastpav.discovery.config.Config;
import org.eastpav.discovery.config.ConfigNotSupportedException;
import org.eastpav.discovery.util.PathUtil;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 虚拟管理器.
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/19.
 */
public class DummyManager {

    public void start() {
        Discoverer discoverer = new Discoverer();
        discoverer.init("116.62.67.216:31004");
        String conf = "param1=this is param36\n" +
                "param2=1\n" +
                "param4=2\n" +
                "param3=3.6\n" +
                "param5=56;102;5000\n" +
                "param6=concentrator-10;deviceHolder-20";

        String conf2 = "param1=hahahahahaha\n" +
                "param2=90\n" +
                "param4=80\n" +
                "param3=70.6\n" +
                "param5=1;2;3000\n" +
                "param6=forYou-50;forMe-50";

        String configPath = PathUtil.makeConfigPath("test", "deviceHolder");
        String globalConfigPath = PathUtil.makeConfigPath("test");

        Config config = discoverer.createServerConfig(configPath, conf, false);

        //Config config2 = discoverer.createServerConfig(globalConfigPath, conf, false);


        System.out.println("ServerConfig create\n\n");
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            System.out.println("ServerConfig[node] update\n\n");
            config.update(conf2);
        }catch (ConfigNotSupportedException e) {
            e.printStackTrace();
        }

        try {
            TimeUnit.SECONDS.sleep(20);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            System.out.println("ServerConfig[global] update\n\n");
//            config2.update(conf2);
//        }catch (ConfigNotSupportedException e) {
//            e.printStackTrace();
//        }

        String parentPath = PathUtil.makeNodeListenPath("test", "deviceSave");
        List<String> nodeList = discoverer.getChildren(parentPath);
        System.out.println("->->->->->->->->->->->->->->->");
        nodeList.forEach(node -> {
            System.out.println("node:" + node);
            String fullName = PathUtil.makeNodeFullPath("test", "deviceSave", node);
            Stat stat = discoverer.getPathStat(fullName);
            System.out.println("stat:" + new Date(stat.getCtime()).toString() + " " + new Date(stat.getMtime()).toString());
        });
        System.out.println("->->->->->->->->->->->->->->->");


        Stat stat2 = discoverer.getPathStat(PathUtil.makeConfigPath("test", "deviceSave"));
        System.out.println("stat:" + new Date(stat2.getCtime()).toString() + " " + new Date(stat2.getMtime()).toString());

        try {
            TimeUnit.SECONDS.sleep(20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
