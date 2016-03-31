/**
 * Created by Jun Yuan on 2016-03-25.
 */
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client {

   BufferedReader Sinput; // socket reader
   PrintWriter Soutput; // socket writer
   Socket socket; // the socket
   private Game game; // the game
   private Player player;

   /**
    * Constructs a client
    * @param game the game
    */
   public Client(Game game, Player player) {
      this.game = game;
      this.player = player;

      try {
         socket = new Socket("localhost", GameConstants.PORT);
      }
      catch (Exception e) {
         System.out.println("Error connecting to server:" + e);
         return;
      }

      System.out.println(player.getName() + "'s connection accepted " +
              socket.getInetAddress() + ":" + socket.getPort());

      // Create two way data stream
      try {
         Sinput  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         Soutput = new PrintWriter(socket.getOutputStream(), true);
      }
      catch (IOException e) {
         System.out.println("Exception creating new input/output Streams: " + e);
         return;
      }
   }

   /**
    * Starts a new thread
    */
   public void listenToServer() {
      Thread listening = new Thread(){
         public void run(){
            while(true) {
               try {
                  String readingData = Sinput.readLine();
                  processServerData(readingData);
               } catch (IOException e) {
                  e.printStackTrace();
               }
            }
         }
      };
      listening.start();
   }

   public void sendData(String sendServerData, String code){
      Soutput.println(code + "-" + sendServerData);
   }

   /**
    * Respond to server for a card flip
    * @param string the incoming server request
    */
   public void processServerData(String string) {
      if (string.contains("flipCard")) {
         String cardToFlipLocation = string.replace("flipCard-", "");
         game.flipCard(Integer.parseInt(cardToFlipLocation));
      }
   }

   /**
    * Helper method for sending server requests
    * @param data the data to send
    * @return null upon exception
    */
   public String sendAndGetData(String data) {
      Soutput.println(data);
      try {
         return Sinput.readLine();
      }
      catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }

   /**
    * Sends server request for cards and parses server response
    * @return new card list
    */
   public ArrayList<Card> getCardsFromServer() {
      ArrayList<Card> cardList = new ArrayList<>();
      String cardString = sendAndGetData("getCards");
      String[] cardSymbols = cardString.split("-");

      for (int i = 1; i < (GameConstants.COL * GameConstants.ROW) + 1; i++) {
         cardList.add(new Card(cardSymbols[i], i - 1));
      }

      return cardList;
   }
}
