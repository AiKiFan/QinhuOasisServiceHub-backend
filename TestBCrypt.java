import cn.hutool.crypto.digest.BCrypt;
public class TestBCrypt {
    public static void main(String[] args) {
        String password = "Admin@123456";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        System.out.println("New hash: " + hash);
        System.out.println("Verify: " + BCrypt.checkpw(password, hash));
    }
}
