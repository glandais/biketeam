package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.Dates;
import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.common.ImageDescriptor;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class MailService implements ExternalPublicationService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private TeamService teamService;

    @Autowired
    private RideService rideService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private UserService userService;

    @Autowired
    private SMTPService smtpService;

    public void publish(Team team, Ride ride) {

        userService.listUsersWithMailActivated().forEach(user -> {

            log.info("Publish ride {} to {}", ride.getId(), user.getEmail());

            StringBuilder sb = new StringBuilder();
            sb.append("<html>").append("<head></head>").append("<body>");
            sb.append("<p>").append(ride.getTitle()).append(" - ").append(Dates.frenchDateFormat(ride.getDate())).append("</p>");
            sb.append("<p>").append(getHtmlLink(urlService.getRideUrl(team.getId(), ride.getId()))).append("</p>");
            sb.append("<p>").append(ride.getDescription()).append("</p>");
            sb.append("<br/>");
            ride.getGroups().forEach(group -> {
                sb.append("<p>").append(group.getName()).append(" - ");
                sb.append(group.getLowerSpeed()).append("/").append(group.getUpperSpeed()).append(" km/h").append("<br/>");
                sb.append("Départ ").append(Dates.formatTime(group.getMeetingTime())).append(" - ");
                sb.append(group.getMeetingLocation()).append("<br/>");
                if (group.getMapId() != null) {
                    sb.append("Map : ").append(getHtmlLink(urlService.getMapUrl(team.getId(), group.getMapId()))).append("<br/>");
                }
                sb.append("</p>");
            });
            if (ride.isImaged()) {
                sb.append("<p><img src=\"cid:Image\" /></p>");
            }
            sb.append("<br/>").append("<p>").append(team.getName()).append("</p>");
            sb.append("</body>").append("</html>");

            final String content = sb.toString();
            if (ride.isImaged()) {
                rideService.getImage(team.getId(), ride.getId()).ifPresent(rideImage -> this.publish(team, user, ride.getTitle(), content, rideImage.getPath()));
            } else {
                this.publish(team, user, ride.getTitle(), content, null);
            }

        });

    }

    public void publish(Team team, Publication publication) {

        userService.listUsersWithMailActivated().forEach(user -> {

            log.info("Publish publication {} to {}", publication.getId(), user.getEmail());

            StringBuilder sb = new StringBuilder();
            sb.append("<html>").append("<head></head>").append("<body>");
            sb.append("<p>").append(publication.getTitle()).append("</p>");
            sb.append("<p>").append(getHtmlLink(urlService.getUrl())).append("</p>");
            sb.append("<p>").append(publication.getContent()).append("</p>");
            if (publication.isImaged()) {
                sb.append("<p><img src=\"cid:Image\" /></p>");
            }
            sb.append("<br/>").append("<p>").append(team.getName()).append("</p>");
            sb.append("</body>").append("</html>");

            final String content = sb.toString();

            if (publication.isImaged()) {
                publicationService.getImage(team.getId(), publication.getId()).ifPresent(pubImage -> this.publish(team, user, publication.getTitle(), content, pubImage.getPath()));
            } else {
                this.publish(team, user, publication.getTitle(), content, null);
            }

        });

    }

    private String getHtmlLink(String href) {
        return "<a href=\"" + href + "\">" + href + "</a>";
    }

    private void publish(Team team, User user, String subject, String content, Path image) {

        try {

            ImageDescriptor embedImage = null;
            if (image != null) {
                final FileExtension fileExtension = FileExtension.findByFileName(image.getFileName().toString()).get();
                embedImage = ImageDescriptor.of(fileExtension, image);
            }
            smtpService.send(team, user.getEmail(), user.getFirstName() + " " + user.getLastName(), subject, content, embedImage);

        } catch (Exception e) {
            log.error("Error while publishing by email : " + content, e);
        }

    }

}