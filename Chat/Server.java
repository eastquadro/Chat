package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by eastquadro on 21.10.2017.
 */
public class Server {
   private static Map<String,Connection> connectionMap = new ConcurrentHashMap<String,Connection>();

   public static void sendBroadcastMessage(Message message) {
       for(Map.Entry<String,Connection> pair : connectionMap.entrySet())
       {
           try {
               pair.getValue().send(message);
           } catch (IOException e) {
               System.out.println("Can't send a message");
           }
       }
   }

    private static class Handler extends Thread{
        Socket socket;
        public Handler(Socket socket) {
            this.socket = socket;
        }

        private void sendListOfUsers(Connection connection,String userName) throws IOException {
            for(Map.Entry<String,Connection> pair : connectionMap.entrySet())
            {
               String name =  pair.getKey();
               if(!name.equals(userName))
               connection.send(new Message(MessageType.USER_ADDED,name));
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true)
            {
              Message message =  connection.receive();
              if(message.getType() == MessageType.TEXT)
              {
                  String s = userName + ": " + message.getData();
                  sendBroadcastMessage(new Message(MessageType.TEXT,s));
              }
              else {
                  System.out.println("Huston, we have a problems");
              }
            }
        }

       private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
           connection.send(new Message(MessageType.NAME_REQUEST));
           Message msg = connection.receive();
            while (!msg.getType().equals(MessageType.USER_NAME))
            {
                connection.send(new Message(MessageType.NAME_REQUEST));
                msg = connection.receive();
            }
            while (msg.getData().equals(""))
            {
                connection.send(new Message(MessageType.NAME_REQUEST));
                msg = connection.receive();
            }
            while (connectionMap.containsKey(msg.getData()))
            {
                connection.send(new Message(MessageType.NAME_REQUEST));
                msg = connection.receive();

            }

            connectionMap.put(msg.getData(),connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED));

            return msg.getData();
       }

        @Override
        public void run() {

            if (socket != null && socket.getRemoteSocketAddress() != null) {
                ConsoleHelper.writeMessage("Established a new connection to a remote socket address: " + socket.getRemoteSocketAddress());
            }
            String name = null;
           try( Connection connection = new Connection(socket)) {
                name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED,name));
               sendListOfUsers(connection,name);
               serverMainLoop(connection,name);



           } catch (IOException | ClassNotFoundException e) {
               System.out.println("Some shit happens");
           }finally {
               if(name != null)
               {
                   connectionMap.remove(name);
                   sendBroadcastMessage(new Message(MessageType.USER_REMOVED,name));
               }
           }
            System.out.println("Connection was closed");
        }
    }
    public static void main(String[] args) throws IOException {
        ConsoleHelper.writeMessage("Input server port: ");
        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage("Server started...");
            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage("Something wrong, Server socket closed.");
        }
    }
}
