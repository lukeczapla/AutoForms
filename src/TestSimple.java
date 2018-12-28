import java.util.List;

public class TestSimple {
 
  public static void main(String[] args) {
        FormPanel<SimplePoint> p = new FormPanel<SimplePoint>(SimplePoint.class);
    
        List<SimplePoint> cards = p.waitOnItems();
    
        System.out.println(cards);
        
  }
}