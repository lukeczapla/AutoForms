import java.util.List;

public class Test {
 
  public static void main(String[] args) {
        FormPanel<Card> p = new FormPanel<>(Card.class);
    
        List<Card> cards = p.waitOnItems();
    
        System.out.println(cards);
        


  }
}