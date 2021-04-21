import java.util.ArrayList;
import java.util.List;

public class Shop implements Runnable {
    private List<Product> products;

    public Shop() {
        this.products = new ArrayList<>();
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void addNewProduct(){
        int id = this.products.size();
        this.products.add(new Product(id, "The Product " + String.valueOf(id)));
    }

    @Override
    public void run() {

    }
}
