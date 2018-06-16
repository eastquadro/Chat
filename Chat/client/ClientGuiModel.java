package com.javarush.task.task30.task3008.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by eastquadro on 24.10.2017.
 */
public class ClientGuiModel {
    private final Set<String> allUserNames = new HashSet<String>();
    private String newMessage;

    public Set<String> getAllUserNames() {
        return Collections.unmodifiableSet(allUserNames);
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }

    public String getNewMessage() {

        return newMessage;
    }

    public void addUser(String userName)
    {
        allUserNames.add(userName);
    }

    public void deleteUser(String userName)
    {
        allUserNames.remove(userName);
    }
}
