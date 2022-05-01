package personal.fields.router.serverMangerment;

import VO.ServerInfo;
import exception.NoAvaliableServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocalBalanceLoader<T> implements BalanceLoader<T> {

    private static final Logger logger = LoggerFactory.getLogger(LocalBalanceLoader.class);

    @Value("${server.local.address}")
    private String localAddress;

    @Value("${server.local.port}")
    private int port;

    public T getOne(List<T> list) throws NoAvaliableServerException {
        if (list.size() == 0)
            throw new NoAvaliableServerException();

        ServerInfo instance = null;
        try {
            instance = (ServerInfo) list.get(0).getClass().newInstance();
            instance.setIp(localAddress);
            instance.setPort(port);
            if (list.contains(instance) == false) return null;
        } catch (Exception e) {
            logger.error("实例化 server Info 失败！");
            e.printStackTrace();
        }
        return (T) instance;
    }
}
