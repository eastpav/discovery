package org.eastpav;

import lombok.NonNull;
import lombok.Setter;
import org.eastpav.discovery.annotation.GlobalItem;

import java.util.List;
import java.util.Map;

/**
 * 配置对象示例.
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/25.
 */
public class ConfigBean {

    @GlobalItem
    private String param1;
    private int param2;
    private Double param3;
    private Integer param4;
    private List<Integer> param5;
    private Map<String, Integer> param6;
    private DeviceClass param7;
    private int[] param8;

//    private SomeConfig param7;
//
//    class SomeConfig {
//        private String conf1;
//        private int conf2;
//        private List<String> conf3;
//    }

    @Override
    public String toString() {
        return param1 + " " + param2 + " " + param3 + " " + param4;
    }

    public void print() {
        System.out.println(this.toString());
        param5.forEach(System.out::println);
        //param6.forEach((k, v) -> System.out.println("k:" + k + " v:" + v));
        if(param8 != null) {
            System.out.println("array:");
            for(int p : param8) {
                System.out.println(p);
            }
        }
        System.out.println("\n\n");
    }
}
