/**
 * Created by Jun Yuan on 2016-03-25.
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {

   private ServerSocket serverSocket; // the server socket
   private ArrayList<TcpThread> tcpArray = new ArrayList<>(); // tcp thread array

   /**
    * Constructs a server
    * @param port the port to listen to
    */
   GameServer(int port){
      /* create socket server and wait for connection requests */
      try{
         serverSocket = new ServerSocket(port);
         System.out.println("GameServer waiting for client on port " + serverSocket.getLocalPort());
         int counter = 0;
         while(true){
            counter++;
            Socket socket = serverSocket.accept();
            System.out.println("New client asked for a connection");
            TcpThread t = new TcpThread(socket, counter);
            tcpArray.add(t);
            System.out.println("Starting a thread for a new client");
            t.start();
         }
      }
      catch (IOException e){
         System.out.println("Exception on new ServerSocket: " + e);
      }
   }

   /**
    *
    * @param arg
    */
   public static void main(String[] arg){
      new GameServer(GameConstants.PORT); // start server on port 8000
   }

   /**
    * One instance of this thread will run for each client
    * */
   class TcpThread extends Thread{

      Socket socket;
      int counter = 0;
      BufferedReader Sinput;
      PrintWriter Soutput;
      String playerName;

      /**
       * Constructs thread
       * @param socket the socket
       * @param number the number
       */
      TcpThread(Socket socket, int number) {
         this.socket = socket;
         this.counter = number;
      }

      /**
       * Process request
       * @param string player's name, current card list, or new card list
       * @return
       */
      public String process(String string) {
         if (string.contains("name")) {
            playerName = string.replace("name-", "");
            return playerName;
         }
         if (string.contains("flipCard")) {
            sendToEveryPlayer(string);
         }
         else if(string.contains("getCards")) {
            return createCardList();
         }
         return "";
      }

      /**
       * Thread runner
       */
      public void run() {
        	/* Create two way data stream */
         System.out.println("Thread trying to create Input/Output Streams");

         try {
            Soutput = new PrintWriter(socket.getOutputStream());
            Soutput.flush();
            Sinput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         }
         catch (IOException e){
            System.out.println("Exception creating new Input/Output Streams: " + e);
            return;
         }

         while(true) {
            System.out.println("Thread waiting for a String from the Client");
            try{
               String str = Sinput.readLine();
               Soutput.println(process(str));
               Soutput.flush();
            }
            catch (IOException e){
               System.out.println("Exception reading/writing streams: " + e);
               return;
            }

         }
      }

      /**
       * Helper method for socket out
       * @param string the string to send
       */
      public void send(String string) {
         Soutput.println(string);
         Soutput.flush();

      }
   }

   /**
    * Creates and shuffles a fresh set of card order
    * @return
    */
   public String createCardList(){
      StringBuilder tempString = new StringBuilder();
      ArrayList<String> cardList = new ArrayList<>();

      for(int i = 1; i <= (GameConstants.ROW * GameConstants.COL) / 2; i++){
         cardList.add(Integer.toString(i));
         cardList.add(Integer.toString(i));
      }

      Collections.shuffle(cardList);

      for (int cardNum = 0; cardNum < cardList.size(); cardNum++) {
         tempString.append("-");
         tempString.append(cardList.get(cardNum));
      }

      return tempString.toString();
   }

   /**
    * Sends current card state to every player
    * @param string
    */
   public void sendToEveryPlayer(String string) {
      for (TcpThread client : tcpArray) {
         client.send(string);
      }
   }
}

