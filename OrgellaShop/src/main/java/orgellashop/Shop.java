package orgellashop;
import orgellashop.backend.BackendSession;
import orgellashop.backend.BackendException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class Shop implements Runnable {

    private static Shop instance;

    private List<Product> products;

    private BackendSession session;

    private int productsNumber;

    private Shop() throws BackendException {
        this.products = new ArrayList<>();
        this.productsNumber = 0;

        String PROPERTIES_FILENAME = "config.properties";
        String contactPoint = null;
        String keyspace = null;

        Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME));

            contactPoint = properties.getProperty("contact_point");
            keyspace = properties.getProperty("keyspace");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        session = new BackendSession(contactPoint, keyspace);
    }

    public static Shop getInstance() throws BackendException {
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

    public int getProductsNumber() {
        return productsNumber;
    }

    public void setProductsNumber(int productsNumber) {
        this.productsNumber = productsNumber;
    }


    public void addNewProduct(int id) throws BackendException {
//        int id = this.products.size();
//        Product product = new Product(id, "The Product#" + id);
//        product.setQuantity(10);
//        this.products.add(product);
        session.upsertProduct(id, 10);
        this.productsNumber += 1;
    }

    public int buyProduct(int id) throws BackendException {
//        if (this.products.get(id).getQuantity() != 0) {
//            this.products.get(id).decreaseQuantity(1);
//            System.out.println("Shop: The Product " + id + "has been bought. " + this.products.get(id).getQuantity() + " left.");
//            return id;
//        }
//        else
//            return -1;
        String product = session.getUser(id);
        String[] productList = product.split("\t");
        if (Integer.parseInt(productList[0]) != 0) {
            session.updateProduct(id, 1);
            System.out.println("Shop: The Product " + id + "has been bought. " + this.products.get(id).getQuantity() + " left.");
            return id;
        }
        else
            return -1;
    }

    public void deliverProducts() throws BackendException {
//        for (Product p : products) {
//            p.increaseQuantity(5);
//        }
        session.deliverProducts(10);
    }

    @Override
    public void run() {
        System.out.println("Shop is running");
        Random rand = new Random();
        while(true){
            int randomNumber = rand.nextInt(500);
            if (randomNumber == 13){
                try {
                    this.deliverProducts();
                } catch (BackendException e) {
                    e.printStackTrace();
                }
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
