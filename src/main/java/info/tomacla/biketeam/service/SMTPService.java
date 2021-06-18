package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.Attachment;
import info.tomacla.biketeam.common.ImageDescriptor;
import info.tomacla.biketeam.domain.team.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
public class SMTPService {

    @Autowired
    private TeamService teamService;

    @Autowired
    private Environment env;

    protected static final Logger log = LoggerFactory.getLogger(SMTPService.class);

    public void send(Team team, String to, String name, String subject, String message, String cc, ImageDescriptor embedImage, List<Attachment> files) {

        Properties props = getSmtpProperties();
        if (props == null) {
            return;
        }

        try {

            log.info("Sending mail to {} - {} : [{}]", to, name, subject);

            Authenticator auth = new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(env.getProperty("smtp.username"), env.getProperty("smtp.password"));
                }
            };

            Session session = Session.getInstance(props, auth);

            MimeMessage msg = new MimeMessage(session);
            msg.setSubject(subject + " - " + team.getName(), "UTF-8");

            msg.addHeader("Content-Transfer-Encoding", "8bit");
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.setSentDate(new Date());
            msg.setFrom(new InternetAddress(env.getProperty("smtp.from"), team.getName()));
            msg.setReplyTo(InternetAddress.parse(env.getProperty("smtp.from"), false));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            if (cc != null) {
                msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
            }

            Multipart multipart = new MimeMultipart("related");

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(message, "text/html; charset=utf-8");
            multipart.addBodyPart(messageBodyPart);

            if (embedImage != null) {
                MimeBodyPart embedImagePart = new PreencodedMimeBodyPart("base64");
                embedImagePart.setContent(
                        Base64.getEncoder().encodeToString(Files.readAllBytes(embedImage.getPath())),
                        embedImage.getExtension().getMediaType()
                );
                embedImagePart.setFileName("image" + embedImage.getExtension().getExtension());
                embedImagePart.setDisposition(MimeBodyPart.INLINE);
                embedImagePart.setContentID("<Image>");
                multipart.addBodyPart(embedImagePart);
            }

            if (files != null) {
                for (Attachment file : files) {
                    MimeBodyPart filePart = new PreencodedMimeBodyPart("base64");
                    filePart.setContent(file.content, file.mimeType);
                    filePart.setDisposition(MimeBodyPart.ATTACHMENT);
                    filePart.setFileName(file.name);
                    multipart.addBodyPart(filePart);
                }
            }

            msg.setContent(multipart);

            Transport.send(msg);


        } catch (IOException | MessagingException e) {
            log.error("Error while sending email : " + e.getMessage(), e);
            throw new RuntimeException("Unable to send email", e);
        }

    }

    public void send(Team team, String to, String name, String subject, String message) {
        this.send(team, to, name, subject, message, null, null, null);
    }

    public void send(Team team, String to, String name, String subject, String message, ImageDescriptor embedImage) {
        this.send(team, to, name, subject, message, null, embedImage, null);
    }

    public void send(Team team, String to, String name, String subject, String message, String cc) {
        this.send(team, to, name, subject, message, cc, null, null);
    }

    private Properties getSmtpProperties() {

        if (env.containsProperty("smtp.host") && env.containsProperty("smtp.port")
                && env.containsProperty("smtp.username") && env.containsProperty("smtp.password")
                && env.containsProperty("smtp.from")) {
            Properties props = new Properties();
            props.put("mail.smtp.host", env.getProperty("smtp.host"));
            props.put("mail.smtp.port", env.getProperty("smtp.port"));
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.username", env.getProperty("smtp.username"));
            props.put("mail.password", env.getProperty("smtp.password"));
            return props;
        }

        return null;


    }

}