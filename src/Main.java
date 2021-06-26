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

//        System.out.println("Year 2049. Earth is ruled by monopolistic Orgella Corporation.");
//        Thread.sleep(2000);
//        System.out.println("It is specializing in creating items called The Products.");
//        Thread.sleep(2000);
//        System.out.println("Humanity is on a verge of destruction.");
//        Thread.sleep(2000);
//        System.out.println("Those who survived have only purpose - to buy The Products.");
//        Thread.sleep(2000);
//        System.out.println("Will you survive in this dark, hostile environment?");
//        Thread.sleep(2000);
//        System.out.println("We will find out...");
//        Thread.sleep(2000);
//        clearConsole();

//        Scanner scan = new Scanner(System.in);
//
//        System.out.println("Set number of products: ");
//        int numberOfProducts = Integer.parseInt(scan.nextLine());
//        System.out.println("Number of products set to " + numberOfProducts);
//
//        System.out.println("Set number of clients: ");
//        int numberOfClients = Integer.parseInt(scan.nextLine());
//        System.out.println("Number of clients set to " + numberOfClients);
//
//
//        for (int i=0; i<numberOfProducts; i++){
//            Shop.getInstance().addNewProduct();
//        }
//
//        List<Thread> clientsThreads = new ArrayList<Thread>();
//        for(int i=0; i<numberOfClients;i++){
//            Client client = new Client(i);
//            Thread clientThread = new Thread(client);
//            clientsThreads.add(clientThread);
//        }
//
//        Thread shopThread = new Thread(Shop.getInstance());
//        shopThread.start();
//
//        for(int i=0; i<numberOfClients; i++){
//            clientsThreads.get(i).start();
//        }


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

        BackendSession session = new BackendSession(contactPoint, keyspace);

        session.upsertProduct(0, 20);
        session.upsertProduct(1, 30);
        session.upsertProduct(2, 40);

        String output = session.selectAll();
		System.out.println("Products: \n" + output);

		session.deleteAll();

        System.exit(0);

    }
}
