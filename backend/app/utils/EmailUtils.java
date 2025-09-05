package utils;

import com.typesafe.config.Config;
import play.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EmailUtils {

    /**
     * Is email enabled.
     *
     * @param config the configuration settings.
     * @return whether emails are enabled.
     */
    public static boolean isEmailEnabled(Config config) {
        boolean emailEnabled = config.getBoolean("system.mail.enabled");
        return emailEnabled;
    }

    /**
     * Are broadcast emails enabled.
     *
     * @param config the configuration settings.
     * @return whether broadcast emails are enabled.
     */
    public static boolean isEmailBroadcastEnabled(Config config) {
        boolean emailBroadcastEnabled = config.getBoolean("system.mail.broadcast.enabled");
        return emailBroadcastEnabled;
    }

    /**
     * Get the username.
     *
     * @param config the configuration settings.
     * @return the username.
     */
    public static String getUsername(Config config) {
        String username = config.getString("system.mail.user");
        return username;
    }

    /**
     * Get the password.
     *
     * @param config the configuration settings.
     * @return the password.
     */
    private static String getPassword(Config config) {
        String password = config.getString("system.mail.password");
        return password;
    }

    /**
     * Get the host.
     *
     * @param config the configuration settings.
     * @return the host.
     */
    public static String getHost(Config config) {
        String host = config.getString("system.mail.host");
        return host;
    }

    public static Integer getPort(Config config) {
        return Integer.parseInt(config.getString("system.mail.port"));
    }

    /**
     * Creates the email configurations for sending an email.
     *
     * @param config the configuration settings.
     * @return the map containing the email configurations.
     */
    public static Map<Object, Object> getEmailConfiguration(Config config) {
        Map<Object, Object> emailConfig = new HashMap<Object, Object>();
        // Get the data from the application configuration.
        String host = config.getString("system.mail.host");
        emailConfig.put("mail.smtp.host", host);
        String port = config.getString("system.mail.port");
        emailConfig.put("mail.smtp.port", port);
        String username = config.getString("system.mail.user");
        emailConfig.put("mail.smtp.user", username);
        String password = config.getString("system.mail.password");
        emailConfig.put("mail.smtp.password", password);
        String auth = config.getString("system.mail.auth");
        emailConfig.put("mail.smtp.auth", auth);
        String starttlsEnabled = config.getString("system.mail.starttls.enabled");
        emailConfig.put("mail.smtp.starttls.enable", starttlsEnabled);
        // Return the configurations.
        return emailConfig;
    }

    /**
     * Sends a mail given the recipient, subject and the body.
     *
     * @param config the configuration settings.
     * @param toRecipients the list of to recipients.
     * @param ccRecipients the list of cc recipients.
     * @param bccRecipients the list of bcc recipients.
     * @param subject the subject.
     * @param body the body.
     * @throws MessagingException if there was an exception while sending the message.
     */
    public static void sendMail(Config config, String[] toRecipients, String[] ccRecipients, String[] bccRecipients,
                                String subject, String body) throws MessagingException {
        // From config file, get the host, username and password and put it in variables for later use.
        String host = getHost(config);
        Integer port = getPort(config);
        String username = getUsername(config);
        String password = getPassword(config);
        Logger.info("sendMail: Retrieved configuration - Host: " + host + ", Port: " + port + ", Username: " + username);
        /*
        Properties props = System.getProperties();
        Logger.info("sendMail: Original system properties: " + props.toString());

        props.putAll(getEmailConfiguration(config));
        Logger.info("sendMail: Custom email configuration properties: " + props.toString());
        Logger.info("sendMail: Final SMTP properties: " + props.toString());
         */
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", "465");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                Logger.info("sendMail: Authenticating using username: " + username);
                return new PasswordAuthentication(username, password);
            }
        });
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));

        // Add the to recipients
        for (int i = 0; i < toRecipients.length; i++) {
            Logger.info("sendMail: Adding TO recipient: " + toRecipients[i]);
            InternetAddress recipient = new InternetAddress(toRecipients[i]);
            message.addRecipient(Message.RecipientType.TO, recipient);

        }

        // Add the cc recipients
        if (ccRecipients != null && ccRecipients.length > 0) {
            Logger.info("sendMail: Setting CC recipients: " + Arrays.toString(ccRecipients));
            String csv = String.join(",", ccRecipients);
            message.setRecipients(
                    Message.RecipientType.CC,
                    InternetAddress.parse(csv)
            );
        }

        // Add the bcc recipients
        for (int i = 0; i < bccRecipients.length; i++) {
            Logger.info("sendMail: Adding BCC recipient: " + bccRecipients[i]);
            InternetAddress recipient = new InternetAddress(bccRecipients[i]);
            message.addRecipient(Message.RecipientType.BCC, recipient);
        }

        message.setSubject(subject);
        message.setText(body);
        Logger.info("sendMail: Subject set to: " + subject);
        Logger.info("sendMail: Email body: " + body);

        Transport.send(message);
        /*
        Logger.info("sendMail: Getting SMTP transport...");
        Transport transport = session.getTransport("smtp");
        Logger.info("sendMail: Connecting to SMTP server at " + host + ":" + port + " using username: " + username);
        transport.connect(host, port, username, password);
        Logger.info("sendMail: Connected to SMTP server successfully.");
        transport.sendMessage(message, message.getAllRecipients());
        Logger.info("sendMail: Email sent successfully.");
        transport.close();
         */
        Logger.info("Send email succeeded!");
    }

    /**
     * Sends individual mail to one email.
     *
     * @param config the configuration settings.
     * @param recipient the recipient.
     * @param subject the subject.
     * @param body the body.
     * @throws MessagingException if there was an exception while sending the message.
     */
    public static void sendIndividualEmail(Config config, String recipient, String subject, String body) throws
            MessagingException {
        Logger.info("sendIndividualEmail: Starting email sending process.");
        Logger.info("sendIndividualEmail: Config = " + config);
        Logger.info("sendIndividualEmail: Recipient = " + recipient);
        Logger.info("sendIndividualEmail: Subject = " + subject);
        Logger.info("sendIndividualEmail: Body = " + body);
        // Generate the to, cc and bcc recipients.
        String[] toRecipients = {recipient};
        String[] ccRecipients = {};
        String[] bccRecipients = {};
        Logger.info("Generated recipients - To: " + Arrays.toString(toRecipients)
                + ", CC: " + Arrays.toString(ccRecipients)
                + ", BCC: " + Arrays.toString(bccRecipients));
        // Send mail.
        Logger.info("sendIndividualEmail: Calling sendMail method.");
        sendMail(config, toRecipients, ccRecipients, bccRecipients, subject, body);
        Logger.info("sendIndividualEmail: Email sent successfully.");
    }
}
