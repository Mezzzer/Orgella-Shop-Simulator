package orgellashop;
import orgellashop.backend.BackendSession;
import orgellashop.backend.BackendException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Shop implements Runnable {

//    private static Shop instance;
    private BackendSession session;
    private int productsNumber;
    private int deliverySize;
    private int deliveryWindow;

    public Shop() throws BackendException {
        this.productsNumber = 0;
        this.deliveryWindow = 0;
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

    public int getProductsNumber() {
        return productsNumber;
    }

    public void setProductsNumber(int productsNumber) {
        this.productsNumber = productsNumber;
    }

    public int getDeliverySize() {
        return deliverySize;
    }

    public void setDeliverySize(int deliverySize) {
        this.deliverySize = deliverySize;
    }

    public void addNewProduct(int id) throws BackendException {
        session.increaseQuantities(id, this.deliverySize, this.deliverySize);
        this.productsNumber += 1;
    }

    public void buyProduct(int productId, int clientId) throws BackendException {
        String product = session.getProductQuantity(productId);
        String[] productList = product.split("\\s+");
        if (Integer.parseInt(productList[1]) > 0) {
            session.updateProduct(productId);
            System.out.println("To client " + clientId + ": Your order for The Product#" + productId + " is being processed");
            session.addPendingOrder(productId, clientId, this.deliveryWindow);
        }
        else
            System.out.println("To client " + clientId + ": You can't buy this product");
    }

    public void sentProducts() throws BackendException {
        if (this.deliveryWindow > 2000000000)
            this.deliveryWindow = 0;
        else
            this.deliveryWindow += 1;

        String pendingOrders = session.selectPendingOrders(deliveryWindow - 1);
        if (!pendingOrders.equals("")) {
            String[] pendingOrdersList = pendingOrders.split("\n");

            for (int i = 0; i < pendingOrdersList.length; i++) {
                //0 - product_id, 1 - client_id
                String[] pendingOrder = pendingOrdersList[i].split("\\s+");
                int storageQuantity = Integer.parseInt(session.getProductStorageQuantity(Integer.parseInt(pendingOrder[0])));
                if (storageQuantity > 0) {
                    session.decreaseStorage(Integer.parseInt(pendingOrder[0]));
                    session.insertIntoHistory(Integer.parseInt(pendingOrder[0]), Integer.parseInt(pendingOrder[1]));
                    System.out.println("To client " + pendingOrder[1] + ": Your order of The Product#" + pendingOrder[0] + " has been sent");
                } else {
                    System.out.println("To client " + pendingOrder[1] + ": Your order of The Product#" + pendingOrder[0] + " has been put on the waiting list");
                    session.addWaitingOrder(Integer.parseInt(pendingOrder[0]), Integer.parseInt(pendingOrder[1]));
                }
            }

            session.deletePendingOrders(deliveryWindow - 1);
        }
    }

    public void deliverProducts() throws BackendException {
        List<Integer> deliverySizes = new ArrayList<Integer>(Collections.nCopies(this.productsNumber, this.deliverySize));
        List<Integer> storageSizes = new ArrayList<Integer>(Collections.nCopies(this.productsNumber, this.deliverySize));

        String waitingOrders = session.selectWaitingOrders();

        if (!waitingOrders.equals("")) {
            String[] waitingOrdersList = waitingOrders.split("\n");
            for (int i = 0; i < waitingOrdersList.length; i++) {
                //0 - product_id, 1 - client_id
                String[] waitingOrder = waitingOrdersList[i].split("\\s+");
                deliverySizes.set(Integer.parseInt(waitingOrder[0]), deliverySizes.get(Integer.parseInt(waitingOrder[0])) - 1);
                storageSizes.set(Integer.parseInt(waitingOrder[0]), storageSizes.get(Integer.parseInt(waitingOrder[0])) - 1);

                System.out.println("To client " + waitingOrder[1] + ": Your order of The Product#" + waitingOrder[0] + " has been sent");
            }

            session.deleteAllWaitings();
        }
        String negativeQuantities = session.getNegativeQuantities();
        if (!negativeQuantities.equals("")) {
            String[] negativeQuantitiesList = negativeQuantities.split("\n");

            for (int i = 0; i < negativeQuantitiesList.length; i++) {
                //0 - product_id, 1 - quantity
                String[] negativeProduct = negativeQuantitiesList[i].split("\\s+");
                deliverySizes.set(Integer.parseInt(negativeProduct[0]), deliverySizes.get(Integer.parseInt(negativeProduct[0])) + (-Integer.parseInt(negativeProduct[1])));
            }
        }
        for (int i=0; i<this.productsNumber; i++)
            session.increaseQuantities(i, deliverySizes.get(i), storageSizes.get(i));
    }

    @Override
    public void run() {
        System.out.println("Shop is running");
        Random rand = new Random();
        while(true){
            int randomNumber = rand.nextInt(200);
            if (randomNumber == 13){
                try {
                    sentProducts();
                } catch (BackendException e) {
                    e.printStackTrace();
                }

                try {
                    this.deliverProducts();
                    System.out.println("Delivered products");
                } catch (BackendException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
