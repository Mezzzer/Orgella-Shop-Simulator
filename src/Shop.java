import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Shop implements Runnable {

    private static Shop instance;

    private List<Product> products;

    private Shop(){this.products = new ArrayList<>();}

    public static Shop getInstance(){
        if(instance == null){
            synchronized (Shop.class) {
                if(instance == null){
                    instance = new Shop();
                }
            }
        }
        return instance;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void addNewProduct(){
        int id = this.products.size();
        Product product = new Product(id, "The Product#" + id);
        product.setQuantity(10);
        this.products.add(product);
    }

//    docelowo nie przekazuje produktu tylko informację z kodem o powodzeniu/niepowodzeniu transakcji
    public int buyProduct(int id){

        if (this.products.get(id).getQuantity() != 0) {
            this.products.get(id).decreaseQuantity(1);
            System.out.println("Shop: The Product " + id + "has been bought. " + this.products.get(id).getQuantity() + " left.");
            return id;
        }
        else
            return -1;
    }

    public void deliverProducts(){
        for (Product p : products) {
            p.increaseQuantity(5);
        }
    }

    @Override
    public void run() {
        System.out.println("Shop is running");
        Random rand = new Random();
        while(true){
            int randomNumber = rand.nextInt(500);
            if (randomNumber == 13){
                this.deliverProducts();
                System.out.println("Delivery!");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
