@Item
public enum Piece {
 
  PAWN(1), ROOK(5), KNIGHT(3), BISHOP(3), QUEEN(9), KING(1000);
  
  int value = 0;
  
  private Piece(int points) {
    value = points;
  }
  
}