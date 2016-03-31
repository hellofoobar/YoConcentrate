/**
 * Created by Jun Yuan on 2016-03-25.
 */
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.*;
import javax.swing.*;

/**
 * Class for setting up game logic
 */
public class Game extends JFrame {
   private static final long serialVersionUID = 1L;

   private Player[] players = new Player[GameConstants.NUM_PLAYERS];
   private Card card1 = null; // first card selection
   private Card card2 = null; // second card selection
   private int matches = 0; // private int matches = 0;
   private boolean sleep; // thread sleep?
   private int clickCount = 0; // clickCount limits player to two clicks per turn
   private cardActionListener cardAction = null; // cardActionListener
   private Client client;
   private int counter = GameConstants.NUM_PLAYERS;
   private int whosTurn = counter % GameConstants.NUM_PLAYERS;
   Layout gameboard = null; // the game board
   String nameCode = "name";
   private String boardcode = "board";

   /**
    * Starts the game
    */
   public void init() {

      String[] options = {"OK"};
      JPanel panel = new JPanel();
      JLabel lbl = new JLabel("Enter Your name: ");
      JTextField txt = new JTextField(10);
      panel.add(lbl);
      panel.add(txt);

      for (int i = 1; i <= players.length; i++) {

         int selectedOption = JOptionPane.showOptionDialog(null, panel, "Player" + i, JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

         if (selectedOption == 0 && txt.getText().trim().length() > 0) {
            players[i-1] = new Player(txt.getText());
         }
         else {
            players[i-1] = new Player("Player" + i);
         }
         txt.setText("");

         client = new Client(this, players[i-1]);
         try {
            Thread.sleep(100);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }

         client.sendData(players[whosTurn].getName(), nameCode);
         client.sendAndGetData(nameCode + "-" + players[whosTurn].getName());
      }


      this.setSize(800, 830);
      this.setTitle(players[whosTurn].getName() + "'s turn\t\t" + "Score: " + players[whosTurn].getPairsFound());
      playGame();
      client.listenToServer();
   }

   /**
    * @param args
    */
   public static void main(String[] args) {
      Game game = new Game();
      game.init();
   }

   /**
    * Sets up game board and adds a listener to each card.
    */
   public void playGame() {

      this.setTitle(players[whosTurn].getName() + "'s turn\t\t" + "Score: " + players[whosTurn].getPairsFound());
      card1 = null;
      card2 = null;
      matches = 0;
      sleep = false;
      clickCount = 0;
      gameboard = null;
      cardAction = null;

      cardAction = new cardActionListener();
      gameboard = new Layout(client);

      addsListener(cardAction);

      this.add(gameboard);
      this.setVisible(true);
   }

   /**
    * Adds an actionListener to every card in the cardList.
    * @param cardAction  perform the actionListener on these cards
    */
   public void addsListener(cardActionListener cardAction){
      ArrayList<Card> tempList = gameboard.getCards();
      int i = 0;
      while(i < tempList.size()){
         tempList.get(i).addActionListener(cardAction);
         tempList.get(i).addMouseListener(new cardActionListener());
         i++;
      }
   }

   /**
    * Private class Flipper creates a thread that sleeps for 2 seconds when an attempt is incorrect.
    */
   private class Flipper extends SwingWorker<Void, Void>{
      private static final int DELAY = 2000;
      /**
       * Puts the thread to sleep for 2 seconds after the user clicks 2 cards.
       */
      public Void doInBackground(){
         try{
            sleep = true;
            Thread.sleep(DELAY);
         }
         catch(InterruptedException e){
            e.printStackTrace();
         }
         return null;
      }

      /**
       * Flips the cards back to their back images if no match was found after 2 seconds. Sets both cards back to null and
       * sets awake back to true so the game won't sleep.
       */
      public void done(){
         card1.flipCardDown();
         card2.flipCardDown();
         card1 = null;
         card2 = null;
         sleep = false;
      }
   }

   /**
    * Creates a new cardActionListener with an ActionListener and a MouseListener.
    */
    class cardActionListener implements ActionListener, MouseListener{
      /**
       * Game flow and logic implemented here.
       * First checks if clickCount == 2, or thread is sleeping.
       * If card1 is null, player clicks to flip the first card.
       * If card1 is not null, player clicks to flip card2.
       * Every card2 flip increments player's attempts by 1.
       * If card1 matches card2 by id, player found a matching pair.
       * If all 15 matches are found, the game will end and tally each players to determine the winner.
       * In the event of a top score tie, the last player to reach the top wins.
       * Sound FX for match hits, misses, and game winner.
       */
      @Override
      public void actionPerformed(ActionEvent e){
         if (clickCount == 2) { return; }

         if (sleep) { return; }

         if (card1 == null) {
            clickCount++;
            card1 = (Card) e.getSource();
            client.sendAndGetData( "flipCard" + "-" + card1.getCardsLocation());
            card1.flipCardUp();
         }
         else {
            clickCount++;
            card2 = (Card) e.getSource();
            client.sendAndGetData( "flipCard" + "-" + card2.getCardsLocation());
            card2.flipCardUp();
            players[whosTurn].setAttempts(players[whosTurn].getAttempts() + 1);

            if (card1.getId().equals(card2.getId())) {

               players[whosTurn].setPairsFound(players[whosTurn].getPairsFound() + 1);
               if (Player.leadingPlayer == null || players[whosTurn].getPairsFound() >= Player.leadingPlayer.getPairsFound()) {
                  Player.leadingPlayer = players[whosTurn];
               }
               matches++;

               try {
                  InputStream inputStream = getClass().getResourceAsStream("assets/lucky.wav");
                  AudioStream audioStream = new AudioStream(inputStream);
                  AudioPlayer.player.start(audioStream);
               }
               catch (Exception ex) {
                  System.out.println("Error playing sound.");
                  ex.printStackTrace();
               }

               card1.setDisabledIcon(new ImageIcon(GrayFilter.createDisabledImage(card1.getFaceIcon().getImage())));
               card2.setDisabledIcon(new ImageIcon(GrayFilter.createDisabledImage(card2.getFaceIcon().getImage())));
               card1 = null;
               card2 = null;
               clickCount = 0;
               counter++;
               whosTurn = counter % GameConstants.NUM_PLAYERS;
               Window window = SwingUtilities.getWindowAncestor(gameboard);
               JFrame frame = (JFrame) window;
               //frame.setTitle(players[whosTurn].getName() + "'s turn\t\t" + "Score: " + players[whosTurn].getPairsFound());
               frame.setTitle(players[whosTurn].getName() + "'s turn with " + players[whosTurn].getPairsFound() + " pairs.\t\t" + Player.leadingPlayer.getName() + " leads the game with " + Player.leadingPlayer.getPairsFound() + " pairs.");
            }
            else {

                  try {
                     InputStream inputStream = getClass().getResourceAsStream("assets/unlucky.wav");
                     AudioStream audioStream = new AudioStream(inputStream);
                     AudioPlayer.player.start(audioStream);
                  }
                  catch (Exception ex) {
                     System.out.println("Error playing sound.");
                     ex.printStackTrace();
                  }

                  Flipper flipCards = new Flipper();
                  flipCards.execute();
                  clickCount = 0;
                  counter++;
                  whosTurn = counter % GameConstants.NUM_PLAYERS;
                  Window window = SwingUtilities.getWindowAncestor(gameboard);
                  JFrame frame = (JFrame) window;
                  frame.setTitle(players[whosTurn].getName() + "'s turn with " + players[whosTurn].getPairsFound() + " pairs.\t\t" + Player.leadingPlayer.getName() + " leads the game with " + Player.leadingPlayer.getPairsFound() + " pairs.");
            }
         }

         if (matches == 15) {

            try {
               InputStream inputStream = getClass().getResourceAsStream("assets/win.wav");
               AudioStream audioStream = new AudioStream(inputStream);
               AudioPlayer.player.start(audioStream);
            }
            catch (Exception ex) {
               System.out.println("Error playing sound.");
               ex.printStackTrace();
            }
            finally {
               JOptionPane.showMessageDialog(gameboard, Player.leadingPlayer.getName() + " win with " + Player.leadingPlayer.getPairsFound() + " matching pairs." );
            }

            playAgain();
         }
      }

      @Override
      public void mouseClicked(MouseEvent e) {}

      @Override
      public void mouseEntered(MouseEvent e) {}

      @Override
      public void mouseExited(MouseEvent e) {}

      @Override
      public void mousePressed(MouseEvent e) {}

      /**
       * Creates a context menu that gets triggered when the player right clicks. The player can check their attempts, score, or resign.
       */
      @Override
      public void mouseReleased(MouseEvent e) {
         if(e.isMetaDown()){
            JPopupMenu menu = new JPopupMenu();
            JMenuItem score = new JMenuItem("Pairs");
            JMenuItem attempts = new JMenuItem("Attempts");
            JMenuItem resign = new JMenuItem("Resign");

            score.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                  String playerMatches = "Matches: " + players[whosTurn].getPairsFound();
                  JOptionPane.showMessageDialog(gameboard.gridPanel, playerMatches);
               }
            });

            attempts.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e){
                  String playerAttempts = "Attempts: " + players[whosTurn].getAttempts();
                  JOptionPane.showMessageDialog(gameboard.gridPanel, playerAttempts);
               }
            });

            resign.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                  if(JOptionPane.showConfirmDialog(resign, "Are you sure you want to resign match?", players[whosTurn].getName() + "'s turn", JOptionPane.YES_NO_OPTION) == 0) {
                     playAgain();
                  }
               }
            });

            menu.add(score);
            menu.add(attempts);
            menu.add(resign);
            menu.show(e.getComponent(), e.getX(), e.getY());
         }
      }
   }

   /**
    * Asks the player if they want to play again.
    * If player selects yes, creates new game board with reshuffled cards.
    * If player selects no or closes the window, the program exists.
    */
   public void playAgain() {
      if(JOptionPane.showConfirmDialog(this, "Do you want to play again?", "Play Again?", JOptionPane.YES_NO_OPTION) == 0) {
         remove(gameboard);
         init();
      }
      else {
         System.exit(0);
      }
   }

   /**
    * Signals to flip card
    * @param parseInt the location
    */
   public void flipCard(int parseInt) {
      gameboard.flipCard(parseInt);
   }
}
