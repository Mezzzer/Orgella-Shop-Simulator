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
    private BackendSession session;
    private int productsNumber;

    private Shop() throws BackendException {
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

    public int getProductsNumber() {
        return productsNumber;
    }

    public void setProductsNumber(int productsNumber) {
        this.productsNumber = productsNumber;
    }


    public void addNewProduct(int id) throws BackendException {
        session.upsertProduct(id, 10);
        this.productsNumber += 1;
    }

    public int buyProduct(int id) throws BackendException {
        String product = session.getUser(id);
        String[] productList = product.split("\\s+");
        if (Integer.parseInt(productList[1]) != 0) {
            session.updateProduct(id, 1);
            System.out.println("Shop: The Product#" + id + " has been bought. " + productList[1] + " left.");
            return id;
        }
        else
            return -1;
    }

    public void deliverProducts() throws BackendException {
        for (int i=0; i<this.productsNumber; i++)
            session.deliverProducts(i);
    }

    @Override
    public void run() {
        System.out.println("Shop is running");
        Random rand = new Random();
        while(true){
            int randomNumber = rand.nextInt(300);
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
