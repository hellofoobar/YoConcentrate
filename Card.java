/**
 * Created by Jun Yuan on 2016-03-25.
 */
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Card class defines all cards
 */
public class Card extends JButton implements ActionListener {
   private static final long serialVersionUID = 1L;

   private String id = ""; // card identifier
   private ImageIcon faceIcon = null;  // stores front image of card
   private ImageIcon backIcon = null; // stores back image of card
   private ImageIcon rolloverIcon = null; // store image that shows when mouse hovers over card (for testing)
   private int location; // location of card on grid

   /**
    * Constructs a new Card
    * @param id  the id used to set image for each card
    * @param location the location of card on grid
    */
   public Card(String id, int location) {

      this.id = id;
      this.location = location;
      this.setActionCommand("click");
      this.addActionListener(this);
      Image imgBack = null;
      Image imgRollover = null;
      Image imgFront = null;

      try {
         imgBack = ImageIO.read(getClass().getResource("assets/WeChat.png"));
         imgRollover = ImageIO.read(getClass().getResource("assets/" + id + ".png"));
         imgFront = ImageIO.read(getClass().getResource("assets/" + id + ".png"));
      }
      catch (IOException e) {
         e.printStackTrace();
      }

      faceIcon = new ImageIcon(imgFront);
      rolloverIcon = new ImageIcon(imgRollover);
      backIcon = new ImageIcon(imgBack);
      this.setIcon(backIcon);
      this.setRolloverEnabled(true);
      this.setRolloverIcon(rolloverIcon);
      this.setDisabledIcon(faceIcon);
   }

   /**
    * Gets card face icon.
    * @return the face icon
    */
   public ImageIcon getFaceIcon() { return faceIcon; }

   /**
    * Gets card location.
    * @return location of card on grid
    */
   public int getCardsLocation() { return this.location; }

   /**
    * Flips the card down to display the back image. Re-enables the JButton to allow the player to click it.
    */
   public void flipCardDown(){
      this.setIcon(backIcon);
      this.setEnabled(true);
   }

   /**
    * Flips the card face up to display the image. Disables the JButton so the player can't click the same card twice in one turn.
    */
   public void flipCardUp(){
      this.setIcon(faceIcon);
      this.setEnabled(false);
   }

   @Override
   public void actionPerformed(ActionEvent event) { repaint(); }

   /**
    * Gets card id.
    * @return a String value that stores the image in id
    */
   public String getId() { return id;}
}
