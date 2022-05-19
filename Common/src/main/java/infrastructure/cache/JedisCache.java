package infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@Component
public class JedisCache implements Cache {

    @Autowired
    private Jedis jedis;

    @Override
    public void set(String key, Object val) {
        jedis.set(key, val.toString());
    }

    @Override
    public Object get(String key) {
        return jedis.get(key);
    }

    @Override
    public void del(String key) {
        jedis.del(key);
    }

}
