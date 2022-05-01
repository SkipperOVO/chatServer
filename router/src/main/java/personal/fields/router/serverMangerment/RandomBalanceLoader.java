package personal.fields.router.serverMangerment;

import exception.NoAvaliableServerException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class RandomBalanceLoader<T> implements BalanceLoader<T> {

    private Random random = new Random();

    public T getOne(List<T> list) throws NoAvaliableServerException {
        // 这个其实应该放到抽象类中
        if (list.size() == 0)
            throw new NoAvaliableServerException();

        int idx = random.nextInt(list.size());
        if (list.contains(list.get(idx)) == false)
            return null;
        return list.get(idx);
    }
}
