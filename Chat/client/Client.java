package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by eastquadro on 24.10.2017.
 */
public class Client extends Thread {
    protected Connection connection;
    private volatile boolean clientConnected = false;


    protected String getServerAddress() throws IOException {
      String s =   ConsoleHelper.readString();
        return s;
    }

    protected int getServerPort() throws IOException {
        int i = ConsoleHelper.readInt();
        return i;
    }

    protected String getUserName() throws IOException {
        String s = ConsoleHelper.readString();
        return s;
    }

    public void run()
    {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this){
                wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Oop's we have a problem.");
            System.exit(1);
        }
        if(clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
            while (clientConnected)
            {
                String s = ConsoleHelper.readString();
                if(s.equalsIgnoreCase("exit")){
                    break;
                }
                else if(shouldSendTextFromConsole()) {
                    sendTextMessage(s);
                }
            }

        }else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }

    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread() {
       return new SocketThread();
    }

    protected void sendTextMessage(String text){
        String s = text;
        try {
            connection.send(new Message(MessageType.TEXT,s));
        } catch (IOException e) {
            System.out.println("We can't connection");
            clientConnected = false;
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage("User: "+userName+" join this chat");
        }
        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage("User: "+userName+" left this chat");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this)
            {
                Client.this.notify();
            }
        }
       protected void clientHandshake() throws IOException,ClassNotFoundException{
           Message message;
            while (true) {
                message = connection.receive();

                if(message.getType() == MessageType.NAME_REQUEST) {
                    String s =  getUserName();
                    connection.send(new Message(MessageType.USER_NAME,s));
                }else if(message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    break;
                }else {
                    throw new IOException("Unexpected MessageType");
                }
            }
       }

       protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                } else if (message.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                } else if (message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
       }

        public void run()
        {
            try {
                Socket socket = new Socket(getServerAddress(),getServerPort());
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();

            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }


    }
}
