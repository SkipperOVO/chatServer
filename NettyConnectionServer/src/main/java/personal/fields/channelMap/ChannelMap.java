package personal.fields.channelMap;


/*

    存放 ChannelId 和 channel 映射的 map 接口
    之后可能会重新实现 ChannelMap，比如使用多个Map进行分区减少并发竞争
    目前使用单一的 HashMap

 */

public interface ChannelMap<K, V> {

    void put(K key, V val);

    V get(K key);

    void remove(K key);

}
