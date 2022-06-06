package personal.fields.router.result;

import lombok.Data;

@Data
public class ResultDTO<T> {

    public static final int SUCCESS = 200;

    public static final int ERROR = 500;

    private int statusCode;

    private String msg;

    private T obj;

    public ResultDTO(int statusCode, String msg, T obj) {
        this.statusCode = statusCode;
        this.msg = msg;
        this.obj = obj;
    }

    public static <T> ResultDTO<T> error(String msg) {
        return new ResultDTO<T>(ERROR, msg, null);
    }

    public static <T> ResultDTO<T> success(String msg, T obj) {
        return new ResultDTO<T>(SUCCESS, msg, obj);
    }

    public static <T> ResultDTO<T> success(T obj) {
        return new ResultDTO<T>(SUCCESS, "", obj);
    }

}
