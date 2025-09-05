package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.*;
import play.Logger;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.Common;
import utils.Constants;
import utils.EmailUtils;
import utils.FilePartChain;
import views.html.redirect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.*;

public class MailController extends Controller {

    // Injecting config.
    @Inject
    Config config;

    @Inject
    private UserController userController;

    public static final String MAIL_USERNAME = "nasa.opennex@gmail.com";
    public static final String MAIL_PASSWORD = "jia-04222020";
    public Result getReceivedMails() {
        User user = User.find.query().where().eq("id", request().getQueryString("userId")).findOne();
        //List<Mail> result = user.getReceivedMail();  // TODO
        List<Mail> result = null;
        return ok(Json.toJson(result));
    }

    public Result getMail() {
        Map<String, String> params = new HashMap<>();
        Mail mail = Mail.find.byId(Long.valueOf(request().getQueryString("mailId")));
        return ok(Json.toJson(mail));
    }

    public Result getSentMails() {
        Map<String, String> params = new HashMap<>();
        User user = User.find.query().where().eq("id", request().getQueryString("userId")).findOne();
        //List<Mail> result = user.getSentMail();   // TODO
        List<Mail> result = null;
        return ok(Json.toJson(result));
    }

    /**
     * This should return received mail given valid user id
     * @param userId user id
     * @return all received mails
     */
    public Result getReceivedMails(Long userId) {
        if(userId == null) {
            return Common.badRequestWrapper("User id is null or empty");
        }
        User user = User.find.query().where().eq("id", userId).findOne();
        List<Mail> receivedEmails = new ArrayList<>();
        try {
            receivedEmails = user.getReceivedMail();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok(Json.toJson(receivedEmails));
    }

    /**
     * This should return mail given valid mail id
     * @param mailId mail id
     * @return mail
     */
    public Result getMail(Long mailId) {
        if(mailId == null) {
            return Common.badRequestWrapper("mailId is null or empty");
        }
        Mail mail = Mail.find.byId(mailId);
        if (mail == null) {
            ObjectNode response = Json.newObject();
            response.put("message", "Invalid mail id");
            return badRequest(response);
        }
        return ok(Json.toJson(mail));
    }

    /**
     * This should return all sent mails given valid user id
     * @param userId user id
     * @return sent mails
     */
    public Result getSentMails(Long userId) {
        if(userId==null) {
            return Common.badRequestWrapper("userId is null or empty");
        }
        User user = User.find.query().where().eq("id", userId).findOne();
        List<Mail> sentEmails = new ArrayList<>();
        try{
            sentEmails = user.getSentMail();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return ok(Json.toJson(sentEmails));
    }
    public Result postMail() {
        JsonNode requestNode = request().body().asJson();
        User sender = User.find.query().where().eq("id", requestNode.get("userId").asText()).findOne();
        User receiver = User.find.query().where().eq("email", requestNode.get("receiver").asText()).findOne();
        if(sender == null || receiver == null)
            return Common.badRequestWrapper("Wrong sender or receiver.");
        Mail mail = new Mail();
        mail.setContent(requestNode.get("content").asText());
        mail.setTitle(requestNode.get("title").asText());
        mail.setSender(sender);
        mail.setReceiver(receiver);
//		mail.setAttachments(attachments);
        mail.setTimestamp(new Date());
        mail.save();

        String body = "Dear " + receiver.getFirstName() + ",\n\n" +
                "You got a new message from " + sender.getFirstName() +
                " (" + sender.getEmail() + "): \n\n" +
                requestNode.get("content").asText();

        if (sendRealEmail(receiver.getEmail(), mail.getTitle(), body))
            Logger.info("Send real email succeeded!");
        else
            Logger.info("Send real email failed!");
        return ok(Json.toJson(mail).toString());
    }

    /**
     * Create a mail in the database and send a real email through nasa.opennex gmail
     * @return ok if send successfully
     *
     */
    public Result createMail() {
        JsonNode requestNode = request().body().asJson();
        int senderId = Integer.valueOf(requestNode.findPath("sender").asText());
        int receiverId = Integer.valueOf(requestNode.findPath("receiver").asText());
        User sender = User.find.query().where().eq("id", senderId).findOne();
        User receiver = User.find.query().where().eq("id", receiverId).findOne();

        ObjectNode response = Json.newObject();
        if(sender == null || receiver == null) {
            response.put("message", "Invalid sender or receiver");
            return badRequest();
        }

        Mail mail = new Mail();
        mail.setContent(requestNode.get("content").asText());
        mail.setTitle(requestNode.get("title").asText());
        mail.setSender(sender);
        mail.setReceiver(receiver);
        mail.setTimestamp(new Date());
        mail.save();

        String body = "Dear " + receiver.getFirstName() + ",\n\n" +
                "You got a new message from " + sender.getFirstName() +
                " (" + sender.getEmail() + "): \n\n" +
                requestNode.get("content").asText();

        try {
            EmailUtils.sendIndividualEmail(config, receiver.getEmail(), mail.getTitle(), body);
        } catch(Exception e ) {
            e.printStackTrace();
            Logger.error("Failed to send an email.");
            response.put("message", "Fail to send an email");
            return internalServerError(response);
        }
        return created(Json.toJson(mail));
    }



    public boolean sendRealEmail(String recipient, String subject, String body) {
        String username = MAIL_USERNAME;
        String password = MAIL_PASSWORD;
        String[] to = { recipient };

        Logger.info("sendRealEmail: Starting email sending process. Recipient: " + recipient + ", Subject: " + subject);

        Properties props = System.getProperties();
        String host = "smtp.gmail.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", username);
        props.put("mail.smtp.password", password);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        Logger.info("SMTP properties configured: " + props);

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(username));
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for( int i = 0; i < to.length; i++ ) {
                toAddress[i] = new InternetAddress(to[i]);
            }

            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject(subject);
            message.setText(body);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, username, password);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            ae.printStackTrace();
            return false;
        }
        catch (MessagingException me) {
            me.printStackTrace();
            return false;
        }

        return true;
    }

    public MailController() {
        try {
            File dir = new File("files");
            dir.mkdir();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }





}
