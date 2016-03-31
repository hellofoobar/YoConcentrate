/**
 * Created by Jun Yuan on 2016-03-25.
 */

/**
 * Class defines each player
 */
public class Player {

   static Player leadingPlayer = null; // tracks the player with most pairs found

   private String name;
   private int pairsFound = 0;
   private int attempts = 0;

   public String getName() { return name; }

   public int getAttempts() { return attempts; }

   public void setAttempts(int attempts) { this.attempts = attempts; }

   public int getPairsFound() { return pairsFound; }

   public void setPairsFound(int pairsFound) { this.pairsFound = pairsFound; }

   public Player(String name) { this.name = name; }
}
