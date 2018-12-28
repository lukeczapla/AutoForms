
@Item
public class SimplePoint {
 
  @Property
  double x; 
  @Property
  double y;
  
  public SimplePoint() {
    x = 0.0; y = 0.0;
  }
  
  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  
  public void setX(double x) {
    this.x = x;
  }

  public void setY(double y) {
    this.y = y;
  }
  
  public String toString() {
    return "("+x+", "+y+")";
  }

  
}