import infrastructure.cache.Cache;
import infrastructure.cache.JedisCache;

public class JedisPoolTest {

    public static void main(String[] args) {

        Cache cache = new JedisCache();
        cache.set("test","pool ok");
        System.out.println(cache.get("test"));
    }
}
