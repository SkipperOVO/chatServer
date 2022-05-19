package exception;

public class NoAvaliableServerException extends Exception {

    @Override
    public String toString() {
        return "没有可用的服务器！";
    }
}
