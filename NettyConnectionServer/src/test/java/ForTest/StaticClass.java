package ForTest;

public class StaticClass {

    public static int staticFiled;



    public static void f() {}


    public StaticClass() {
        staticFiled = 1001;
        System.out.println("construct");
    }


    static {
        System.out.println("static");
        staticFiled = 121;
    }
}
