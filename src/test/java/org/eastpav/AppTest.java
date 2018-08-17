package org.eastpav;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eastpav.discovery.Discoverer;
import org.eastpav.discovery.annotation.GlobalItem;
import org.eastpav.discovery.config.*;
import org.eastpav.discovery.mq.EMqClient;
import org.eastpav.discovery.mq.MessageListener;
import org.eastpav.discovery.mq.MqClient;
import org.eastpav.discovery.node.Node;
import org.eastpav.discovery.node.NodeRouter;
import org.eastpav.discovery.node.RouteRule;
import org.eastpav.discovery.util.PathInfo;
import org.eastpav.discovery.util.PathUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }


    public void testNode() {
        Discoverer discoverer = new Discoverer();

        discoverer.init("121.196.201.204:32181");

        Node node = discoverer.newNode("test",
                "deviceManager", true, false, null);

        //ClientConfigImpl config = discoverer.createConfig("deviceManager", "tttttt", false);

        System.out.println(node);
        //System.out.println(config);
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        node.leave();

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        node.represent();

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //node.present();

    }


    public void testNodeCommunication() {
        DummyNode dummyNode1 = new DummyNode("typeOne", "blackMan");
        DummyNode dummyNode2 = new DummyNode("blackMan", "typeOne");

        dummyNode1.start();
        dummyNode2.start();

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        dummyNode1.sendMsg("blackMan", "", "this is test");


        try {
            TimeUnit.SECONDS.sleep(20);
        } catch (Exception e) {
            e.printStackTrace();
        }

        dummyNode2.stop();
        dummyNode1.stop();
    }

    public void testUtil() {
        String path = "/test/deviceManager/xxx";
        System.out.println(PathUtil.getServerType(path));

        String[] segments = path.split("/");
        for(String segment : segments) {
            System.out.println("segment: " + segment);
        }
    }

    public void testCluster() {
        LoggerContext loggerContext= (LoggerContext)org.slf4j.LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("root").setLevel(Level.INFO);

        DummyNode node = new DummyNode("deviceSave", null);
        node.start();

        System.out.println("\n\n");

        DummyNode node0 = new DummyNode("mailDing", null);
        node0.start();

        System.out.println("\n\n");
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }



        DummyNode node1 = new DummyNode("deviceManager", "deviceSave", "mailDing");
        node1.start();
        System.out.println("\n\n");
        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (Exception e) {
            e.printStackTrace();
        }

        node1.print();
        node1.printAll();


    }

    public void testConfig() {
        LoggerContext loggerContext= (LoggerContext)org.slf4j.LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("root").setLevel(Level.INFO);

        DummyNode node1 = new DummyNode("deviceSave", null);
        node1.start();

        DummyNode node2 = new DummyNode("deviceSave", null);
        node2.start();

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DummyManager manager = new DummyManager();
        manager.start();

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n\n");

    }

    public void testNodeRouter() {
        NodeRouter nodeRouter = new NodeRouter("manager", RouteRule.BY_INTEGER, 16);

        PathInfo pathInfo = PathUtil.parsePathInfo("/test/manager/nodes/manager-0001", null);
        PathInfo pathInfo2 = PathUtil.parsePathInfo("/test/manager/nodes/manager-0002", null);

        nodeRouter.addNode(pathInfo);

        nodeRouter.print();
        System.out.println("\n\n\n");
        nodeRouter.addNode(pathInfo);
        nodeRouter.addNode(pathInfo2);

        nodeRouter.print();

        Integer[] is = {254455, 0, 1, 66846515};

        for(Integer i : is) {
            Optional<PathInfo> info = nodeRouter.getNode(i);
            if (info.isPresent()) {
                System.out.println(info.get().getFullName());
            }
        }
    }

    public void testParam() {
        String prop = "param1=this is param3\n" +
                "param2=20000000\n" +
                "param4=25\n" +
                "param3=255555.6\n" +
                "param5=56;102;5000\n" +
                "param6=concentrator-250;deviceHolder-12\n" +
                "param7=DEVICE_CLASSB";
        InputStream is1 = new ByteArrayInputStream(prop.getBytes());
        Properties ps = new Properties();

        Object configOld = new ConfigBean();
        //Object configBean = new ConfigBean();

        try {
            ps.load(is1);
            Set<String> set = ps.stringPropertyNames();
            set.forEach(name -> System.out.println("name:" + name + " -> " + ps.getProperty(name)));


            ConfigUtils.parseConfig(configOld, ps, true);

            System.out.println(configOld);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testGeneric() {
        Object mm = new HashMap<>();

        Map map1 = (Map) mm;
        map1.put("test", "value");

        Map<String, Integer> map = (Map) mm;
        //map.put("test", "value");
        map.put("test2", 7);

        map.forEach((k,v) -> System.out.println(k + " -> " + v));
    }

    public void testProp() {
        String prop = "param1.name=this is param3\n" +
                "param1.country=China\n" +
                "param2=20000000\n" +
                "param4=25\n" +
                "param3=255555.6\n" +
                "param5=56;102;5000\n" +
                "param6=concentrator-250;deviceHolder-12";
        InputStream is1 = new ByteArrayInputStream(prop.getBytes());
        Properties ps = new Properties();
        try {
        ps.load(is1);

        Set<String> names = ps.stringPropertyNames();
        names.forEach(name -> System.out.println("name:" + name + " -> " + ps.getProperty(name)));
        is1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testEnum() {
        DeviceClass deviceClass = DeviceClass.DEVICE_CLASSA;

        Class clazz = deviceClass.getClass();
        boolean ee = clazz.isEnum();
        Object[] objs = clazz.getEnumConstants();
        System.out.println(ee);
        System.out.println(objs);
        Object haha = Enum.valueOf(clazz, "DEVICE_CLASSA");

        System.out.println(haha);
    }

    public void testAnnotaion() {
        ConfigBean configBean = new ConfigBean();
        Class clazz = configBean.getClass();

        Annotation[] annotations = clazz.getDeclaredAnnotations();
        for(Annotation a : annotations) {
            System.out.println(a.annotationType());
        }
        System.out.println("==============\n");
        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields) {
            Annotation[] as = field.getAnnotations();
            Annotation annotation = field.getAnnotation(GlobalItem.class);
            field.isAnnotationPresent(GlobalItem.class);
            for(Annotation a : as) {
                if(a.annotationType() == GlobalItem.class) {
                    System.out.println("field: " + field.getName() + " is @GlobalItem");
                }

                System.out.println(a.annotationType());
            }
        }
    }

    public void testArray() {
        String prop = "param1=this is param1\n" +
                "param2=20000000\n" +
                "param4=25\n" +
                "param3=255555.6\n" +
                "param5=56;102;5000\n" +
                "param8=25,56";
        InputStream is1 = new ByteArrayInputStream(prop.getBytes());
        Properties ps = new Properties();
        ConfigBean configOld = new ConfigBean();

        try {
            ps.load(is1);

            ConfigUtils.parseConfig(configOld, ps, false);
            System.out.println(configOld);
            configOld.print();

        } catch (Exception e) {
            e.printStackTrace();
        }

//        Object configOld = new ConfigBean();
//        Field[] fields = configOld.getClass().getDeclaredFields();
//        for(Field field : fields) {
//            System.out.println(field.getType().isArray());
//            if(field.getType().isArray()) {
//                System.out.println(field.getType().getComponentType());
//            }
//        }
    }

    public void testMqtt() {
        int port = 31301;
        String host = "116.62.67.216";
        String topic = "up/1122334455667788";
        String topic2 = "ok/112233445566778899";
        String username = "1122334455667788";
        String password = "99887766554433221100";

        try {
            MqClient client = new EMqClient(host, port);

            client.addTopic(topic);
            client.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(String topic, byte[] payload) {
                    System.out.println(topic +" message arrived: " + new String(payload));
                }
            });
            client.doConnect();

            TimeUnit.SECONDS.sleep(2000);

            client.close();
            System.out.println("exit");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testVarArgs() {

        String arg = null;

        dummyCall("myname");

    }

    private void dummyCall(String name, String... args) {
        System.out.println("name:" + name);

        System.out.println("args:"+ args.length);
        for(String arg: args) {

            System.out.println("arg:" + arg);
        }
    }
    public void testMutilArg() {
        MutilArgu argu = new MutilArguImpl();
        argu.present();
    }

    public void testHashSet() {
        Set<String> set = new HashSet<>();
        set.add("test1");
        set.add("test1");
        set.add("test2");
        set.add("test1");

        List<String> list = new ArrayList<>();
        list.add("test1");
        list.add("test1");
        list.add("test2");
        list.add("test1");

        System.out.println("set");
        for(String item : set) {
            System.out.println(item);
        }

        System.out.println("list");
        for(String item : list) {
            System.out.println(item);
        }
    }
}

