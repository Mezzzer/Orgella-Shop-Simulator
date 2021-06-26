import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Client implements Runnable {
    private int id;

    public Client(int id) {
        this.id = id;
    }

    public void buyProduct(){
        Random rand = new Random();
        int productNumber = rand.nextInt(Shop.getInstance().getProducts().size());
        int boughtProductId = Shop.getInstance().buyProduct(productNumber);
        if (boughtProductId != -1)
            System.out.println("Me, client " + this.id + " bought The Product#" + boughtProductId);
        else
            System.out.println("Me, client " + this.id + " couldn't buy The Product#" + productNumber);
    }

    @Override
    public void run() {
        System.out.println("Client " + this.id + " running");
        Random rand = new Random();
        while(true){
            int randomNumber = rand.nextInt(100);
            if (randomNumber == 13){
                this.buyProduct();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
