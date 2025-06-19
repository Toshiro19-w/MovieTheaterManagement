package com.cinema.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

public class GmailSender {
    private static final Logger LOGGER = Logger.getLogger(GmailSender.class.getName());
    private static final String APPLICATION_NAME = "CinemaHub";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_SEND);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    
    private static Gmail service = null;
    
    static {
        try {
            // Khởi tạo Gmail service
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            LOGGER.info("Gmail API service initialized successfully");
        } catch (GeneralSecurityException | IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Gmail API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Tạo đối tượng Credential để xác thực với Gmail API
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Tải file credentials.json từ resources
        InputStream in = GmailSender.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new IOException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Xây dựng flow và trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(Paths.get(TOKENS_DIRECTORY_PATH).toFile()))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    
    /**
     * Gửi email sử dụng Gmail API
     */
    public static boolean sendEmail(String to, String subject, String bodyText) {
        if (service == null) {
            LOGGER.severe("Gmail API service is not initialized");
            return false;
        }
        
        try {
            // Tạo email
            MimeMessage mimeMessage = createEmail(to, "me", subject, bodyText);
            Message message = createMessageWithEmail(mimeMessage);
            
            // Gửi email
            message = service.users().messages().send("me", message).execute();
            LOGGER.log(Level.INFO, "Email sent successfully with message ID: {0}", message.getId());
            return true;
        } catch (MessagingException | IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to send email: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Tạo MimeMessage từ các thông tin email
     */
    private static MimeMessage createEmail(String to, String from, String subject, String bodyText) 
            throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }
    
    /**
     * Chuyển đổi MimeMessage thành đối tượng Message của Gmail API
     */
    private static Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
    
    /**
     * Gửi mã xác nhận 6 số đến email người dùng
     */
    public static boolean sendVerificationCode(String email, String username, String verificationCode) {
        String subject = "Mã xác nhận đặt lại mật khẩu";
        String body = "Xin chào " + username + ",\n\n" +
                    "Mã xác nhận để đặt lại mật khẩu của bạn là: " + verificationCode + "\n\n" +
                    "Mã này có hiệu lực trong 10 phút.\n\n" +
                    "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                    "Trân trọng,\nQuản trị viên hệ thống";
        
        return sendEmail(email, subject, body);
    }
}