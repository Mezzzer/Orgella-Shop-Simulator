//docelowo tej klasy w ogóle nie będzie, zostanie zastąpiona zapytaniami do bazy na temat danego produktu
public class Product {
    private int id;
    private String name;
    private int quantity;

    public Product(int id, String name) {
        this.id = id;
        this.name = name;
        this.quantity = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void decreaseQuantity(int number){
        this.quantity = quantity - number;
    }

    public void increaseQuantity(int number){
        this.quantity = quantity + number;
    }
}
