import redis.clients.jedis.Jedis;

public class JedisTest {

    public static void main(String[] args) {

        Jedis jedis = new Jedis("192.168.1.101", 7379);
        jedis.set("test", "ok");
        System.out.println(jedis.get("test"));
        String result = "ok";
        jedis.del("test");

    }
}
