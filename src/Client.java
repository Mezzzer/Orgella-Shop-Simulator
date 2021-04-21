import java.util.ArrayList;
import java.util.List;

public class Client implements Runnable {
    private int id;
    private List<Product> ownedProducts;

    public Client(int id) {
        this.id = id;
        this.ownedProducts = new ArrayList<>();
    }


    @Override
    public void run() {

    }
}
