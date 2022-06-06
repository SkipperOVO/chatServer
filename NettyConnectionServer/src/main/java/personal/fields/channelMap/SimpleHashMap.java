package personal.fields.channelMap;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimpleHashMap<K, V> implements ChannelMap<K, V> {

    private ConcurrentHashMap<K, V> concurrentHashMap;

    public SimpleHashMap() {

        this.concurrentHashMap = new ConcurrentHashMap<>();

    }


    @Override
    public void put(K key, V val) {
        this.concurrentHashMap.put(key, val);
    }


    @Override
    public V get(K key) {
        return this.concurrentHashMap.get(key);
    }


    @Override
    public void remove(K key) {
        concurrentHashMap.remove(key);
    }
}
