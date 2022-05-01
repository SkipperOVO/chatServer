package personal.fields.router.serverMangerment;

import exception.NoAvaliableServerException;

import java.util.List;

public interface BalanceLoader<T> {

    public T getOne(List<T> list) throws NoAvaliableServerException;

}
