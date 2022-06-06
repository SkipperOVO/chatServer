package infrastructure.ioc;

public interface IOC {

    //通过name获取 Bean.
    public Object getBean(String name);

    //通过class获取Bean.
    public <T> T getBean(Class<T> clazz);

    //通过name,以及Clazz返回指定的Bean
    public <T> T getBean(String name,Class<T> clazz);

}
