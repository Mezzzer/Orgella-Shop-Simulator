import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

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

    public static void main(String[] args) throws InterruptedException {

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

        Scanner scan = new Scanner(System.in);

        System.out.println("Set number of products: ");
        int numberOfProducts = Integer.parseInt(scan.nextLine());
        System.out.println("Number of products set to " + numberOfProducts);

        System.out.println("Set number of clients: ");
        int numberOfClients = Integer.parseInt(scan.nextLine());
        System.out.println("Number of clients set to " + numberOfClients);


        Shop shop = new Shop();
        List<Client> clients = new ArrayList<>();
        for (int i=0; i<numberOfProducts; i++){
            shop.addNewProduct();
        }

        for(int i=0; i<numberOfClients;i++){
            clients.add(new Client(i));
        }

    }
}
