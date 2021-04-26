package orgellashop;
import orgellashop.backend.BackendException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class Main {

    private static final String PROPERTIES_FILENAME = "config.properties";

    public final static void clearConsole()
    {
        try
        {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows"))
            {
                Runtime.getRuntime().exec("cls");
            }
            else
            {
                Runtime.getRuntime().exec("clear");
            }
        }
        catch (final Exception e){}
    }

    public static void main(String[] args) throws InterruptedException, BackendException {

        Scanner scan = new Scanner(System.in);

        System.out.println("Set number of products: ");
        int numberOfProducts = Integer.parseInt(scan.nextLine());
        System.out.println("Number of products set to " + numberOfProducts);

        System.out.println("Set number of clients: ");
        int numberOfClients = Integer.parseInt(scan.nextLine());
        System.out.println("Number of clients set to " + numberOfClients);


        for (int i=0; i<numberOfProducts; i++){
            Shop.getInstance().addNewProduct(i);
        }

        List<Thread> clientsThreads = new ArrayList<Thread>();
        for(int i=0; i<numberOfClients;i++){
            Client client = new Client(i);
            Thread clientThread = new Thread(client);
            clientsThreads.add(clientThread);
        }

        Thread shopThread = new Thread(Shop.getInstance());
        shopThread.start();

        for(int i=0; i<numberOfClients; i++){
            clientsThreads.get(i).start();
        }
    }
}
