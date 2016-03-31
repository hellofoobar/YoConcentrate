/**
 * Created by Jun Yuan on 2016-03-25.
 */
import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 * Creates a Layout class that has several functions to implement the game.
 */
public class Layout extends JPanel {
   private static final long serialVersionUID = 1L;
   ArrayList<Card> cardList;  // array list of Cards.

   JPanel gridPanel = new JPanel(new GridLayout(GameConstants.ROW, GameConstants.COL)); // gridPanel with GridLayout size rows*columns

   /**
    * Sets up the game board.
    */
   public Layout(Client tempClient){
      cardList = tempClient.getCardsFromServer();
      gridPanel.setPreferredSize(new Dimension(800, 800));
      this.add(gridPanel);

      for(int i = 0; i < cardList.size(); i++){
         gridPanel.add(cardList.get(i));
      }
   }

   /**
    * Get cards
    * @return a cardList with all the created cards
    */
   public ArrayList<Card> getCards() { return cardList; }

   public void flipCard(int parseInt) {
      cardList.get(parseInt).flipCardUp();

   }
}
