package protobuf;

public class Test {
    public static void main(String[] args) {
        boolean res = dfs(3, true);
    }

    public static boolean dfs(int n, boolean isAlice) {
        // n <= 1 时，当前手的人一定会输
        if (n <= 1) {
            if (isAlice) return false;
            else return true;
        }

        boolean res;

        for (int i = n-1; i >= 1; --i) {
            if (n % i != 0) continue;
            if (isAlice)
                if (dfs(n-i, !isAlice)) {
                    res = true;
                    return res;
                }
                else
                if (dfs(n-i, !isAlice) == false) {
                    res = false;
                    return false;
                }
        }
        res = !isAlice;

        return !isAlice;
    }
}
