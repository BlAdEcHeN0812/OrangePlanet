package com.orangeplanet.zjuhelper.service;

import com.orangeplanet.zjuhelper.model.Email;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class EmailService {

    public List<Email> fetchEmails(String username, String password) throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", "imap.zju.edu.cn");
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        
        // ZJU email username usually needs @zju.edu.cn, but sometimes just the ID. 
        // If the user inputs just ID, we might need to append domain, but let's assume user inputs full email or ID as required.
        // Usually for IMAP login, full email address is safer.
        // However, the existing login uses student ID. Let's try to append @zju.edu.cn if not present.
        String emailUser = username;
        if (!emailUser.contains("@")) {
            emailUser = emailUser + "@zju.edu.cn";
        }

        System.out.println("Connecting to IMAP server with user: " + emailUser);
        store.connect("imap.zju.edu.cn", emailUser, password);
        System.out.println("Connected to IMAP server.");

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        // Fetch last 20 messages
        int messageCount = inbox.getMessageCount();
        int start = Math.max(1, messageCount - 19);
        Message[] messages = inbox.getMessages(start, messageCount);

        List<Email> emailList = new ArrayList<>();
        // Iterate in reverse order to show newest first
        for (int i = messages.length - 1; i >= 0; i--) {
            Message message = messages[i];
            String from = "Unknown";
            Address[] fromAddresses = message.getFrom();
            if (fromAddresses != null && fromAddresses.length > 0) {
                from = MimeUtility.decodeText(fromAddresses[0].toString());
            }
            
            String subject = message.getSubject();
            if (subject != null) {
                subject = MimeUtility.decodeText(subject);
            } else {
                subject = "(No Subject)";
            }

            String date = message.getSentDate() != null ? message.getSentDate().toString() : "Unknown Date";
            
            // Simple size approximation
            int size = message.getSize();
            String sizeStr = (size / 1024) + " KB";

            String content = "";
            try {
                content = getTextFromMessage(message);
            } catch (Exception e) {
                content = "Error fetching content: " + e.getMessage();
            }

            emailList.add(new Email(from, subject, date, sizeStr, content));
        }

        inbox.close(false);
        store.close();

        return emailList;
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        } else if (message.isMimeType("text/html")) {
            String html = (String) message.getContent();
            result = html;
        }
        return result;
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart)  throws MessagingException, IOException{
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // prefer plain text? or maybe html? let's just get something.
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + html;
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        return result;
    }
}
