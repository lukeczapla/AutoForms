import java.util.List;
import java.util.ArrayList;

@Item
public class Card {

    @Property(value = "Enter the rank")
    private String rank;
    @Property(value = "Enter the suit")
    private String suit;
    @Property
    private Piece piece;


    static final String[] ranks = {"Ace", "2", "3", "4", "5", "6", "7", "8",
            "9", "10", "Jack", "Queen", "King"};
    static final String[] suits = {"Clubs", "Diamonds", "Hearts", "Spades"};

    public Card() {
        rank = ranks[0];
        suit = suits[0];
    }

    public Card(int n) {
        if (n < 0 || n >= 52) n = 0;
        rank = ranks[n % 13];
        suit = suits[n / 13];
    }

    public Card(String rank, String suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public String getRank() {
        return rank;
    }

    public String getSuit() {
        return suit;
    }



    public void setRank(String rank) {
        this.rank = rank;
    }

    public void setSuit(String suit) {
        this.suit = suit;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public String toString() {
        return rank + " of " + suit + " with piece: " + getPiece().name();
    }

    public static void main(String[] args) {
        Card c = new Card();
        System.out.println(c.toString());
        Card c2 = new Card(3);
        System.out.println(c2);
        List<Card> cards = new ArrayList<Card>();
        System.out.println(cards.size());
        for (int i = 0; i < 52; i++) {
            Card nextCard = new Card(i);
            cards.add(nextCard);
        }
        System.out.println(cards);
        System.out.println(cards.size());
        Card randomCard = cards.remove((int)(cards.size()*Math.random()));
        System.out.println(randomCard);
        System.out.println(cards.size());
        cards.clear();
    }

}

