package personal.fields.infrastructure.ioc.cache;

public interface Cache {

    void set(String key, Object val);

    Object get(String key);

    void del(String key);

    void incr(String key);

    void decr(String key);

    void release();
}
