package org.eastpav.discovery.node;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.eastpav.discovery.util.PathInfo;

import javax.swing.text.html.HTMLDocument;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 节点路由.
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/22.
 */
public class NodeRouter implements ServerNodeListener{
    private String nodeType;
    private RouteRule routeRule;
    private Integer virtualNodesSize;
    private HashFunction hashFunction;
    private Charset charset = Charset.forName("UTF-8");

    private final ConcurrentNavigableMap<Long, PathInfo> circle =
            new ConcurrentSkipListMap<>();

    public NodeRouter(String nodeType, RouteRule routeRule) {
        this(nodeType, routeRule, 16);
    }

    public NodeRouter(String nodeType, RouteRule routeRule, int nodeSize) {
        this.nodeType = nodeType;
        this.routeRule = routeRule;
        this.virtualNodesSize = nodeSize;
        this.hashFunction = hashFunctionForName("murmur3_128");
    }

    public void addNode(PathInfo pathInfo) {
        for (int i = 0; i < virtualNodesSize; i++) {
            circle.put(hash(pathInfo, i).asLong(), pathInfo);
        }
    }

    public void removeNode(PathInfo instance) {
        for (int i = 0; i < virtualNodesSize; i++) {
            circle.remove(hash(instance, i).asLong());
        }
    }

    public HashCode hash(PathInfo pathInfo, int i) {
        return hashFunction.newHasher().putString(pathInfo.getFullName(), charset)
                .putString(pathInfo.getEnvironment(), charset)
                .putInt(i).hash();
    }

    public Optional<PathInfo> getNode(Object o) {
        if(routeRule == RouteRule.BY_INTEGER) {
            return getNode((Integer)o);
        } else if(routeRule == RouteRule.BY_STRING) {
            return getNode((String)o);
        }

        return Optional.empty();
    }

    private Optional<PathInfo> getNode(Integer key) {
        if(key == null) {
            throw new IllegalArgumentException("getNode argument must not be null");
        }

        if(circle.isEmpty()) {
            return Optional.empty();
        }

        Long hash = hashFunction.newHasher().putInt(key).hash().asLong();

        return Optional.of(getPathInfo(hash));
    }

    private Optional<PathInfo> getNode(String key) {
        if(key == null) {
            throw new IllegalArgumentException("getNode argument must not be null");
        }

        if(circle.isEmpty()) {
            return Optional.empty();
        }

        Long hash = hashFunction.newHasher().putString(key, charset)
                .hash().asLong();

        return Optional.of(getPathInfo(hash));
    }

    private PathInfo getPathInfo(Long hash) {

        if(!circle.containsKey(hash)) {
            ConcurrentNavigableMap<Long, PathInfo> tailMap = circle.tailMap(hash);
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }

        return circle.get(hash);
    }


    private HashFunction hashFunctionForName(String name) {
        switch (name) {
            case "murmur3_32":
                return Hashing.murmur3_32();
            case "murmur3_128":
                return Hashing.murmur3_128();
            case "crc32":
                return Hashing.crc32();
            case "md5":
                return Hashing.md5();
            default:
                throw new IllegalArgumentException("Can't find hash function with name " + name);
        }
    }

    @Override
    public void onNodeAdded(PathInfo pathInfo) {
        if(pathInfo.getNodeType().equals(nodeType)) {
            addNode(pathInfo);
        }
    }

    @Override
    public void onNodeUpdated(PathInfo pathInfo) {

    }

    @Override
    public void onNodeRemoved(PathInfo pathInfo) {
        if(pathInfo.getNodeType().equals(nodeType)) {
            removeNode(pathInfo);
        }
    }

    public void print() {
        NavigableSet<Long> set = circle.keySet();
        Iterator<Long> it = set.iterator();
        while (it.hasNext()) {
            Long id = it.next();
            System.out.println(id + " -> " + getPathInfo(id).getFullName());
        }
    }
}
