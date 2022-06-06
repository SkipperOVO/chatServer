package infrastructure.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;


public class JedisCache implements Cache {

    private Jedis jedis;


    public JedisCache() {

        this.jedis = JedisPoolUtil.getJedisPoolInstance().getResource();

    }




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


    @Override
    public void incr(String key) {
        jedis.incr(key);
    }


    @Override
    public void decr(String key) {
        jedis.decr(key);
    }


    @Override
    public void release() {
        this.jedis.close();
    }



}
