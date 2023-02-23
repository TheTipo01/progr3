package com.progr3.server;

import com.progr3.entities.Account;
import com.progr3.entities.Email;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Manager {
    private final Account account;
    private final ReadWriteLock lock;

    public Manager(Account account) {
        this.account = account;
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Returns the emails in the inbox of the account
     *
     * @return List of emails
     * @throws IOException If the inbox folder doesn't exist
     */
    public List<Email> getInbox() throws IOException {
        ArrayList<Email> emails = new ArrayList<>();
        Lock read = lock.readLock();
        read.lock();

        // Reads every file in the inbox folder for our account and adds it to the list of emails
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("./posta/" + account.getAddress()))) {
            for (Path p : stream) {
                // Ignore the account file
                if (!p.toString().contains("account")) {
                    try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(p.toFile()))) {
                        emails.add((Email) input.readObject());
                    } catch (ClassNotFoundException | ClassCastException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        read.unlock();

        return emails;
    }

    /**
     * Saves an email to the inbox of the account
     *
     * @param email Email to save
     * @return True if the email was saved successfully
     */
    public boolean writeEmail(Email email) {
        Lock write = lock.writeLock();
        if (email != null) {
            write.lock();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("./posta/" + account.getAddress() + "/" + email.getId().toString()));
                oos.writeObject(email);
                oos.close();
            } catch (IOException e) {
                return false;
            } finally {
                write.unlock();
            }
        }

        return true;
    }

    /**
     * Deletes an email from the inbox of the account
     *
     * @param email Email to delete
     * @return True if the email was deleted successfully
     */
    public boolean deleteEmail(Email email) {
        boolean deleted = false;

        Lock write = lock.writeLock();
        if (email != null) {
            write.lock();
            File file = new File("./posta/" + account.getAddress() + "/" + email.getId().toString());
            deleted = file.delete();
            write.unlock();
        }

        return deleted;
    }
}
