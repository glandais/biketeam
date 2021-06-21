package info.tomacla.biketeam.web;


import info.tomacla.biketeam.common.Dates;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.LocalDefaultOAuth2User;
import info.tomacla.biketeam.service.ArchiveService;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.ui.Model;

import java.security.Principal;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractController {

    @Autowired
    protected TeamService teamService;

    @Autowired
    protected UserService userService;

    @Autowired
    private ArchiveService archiveService;

    // TODO do this automatically with annotation or other way
    protected void addGlobalValues(Principal principal, Model model, String pageTitle, Team team) {

        model.addAttribute("_pagetitle", pageTitle);
        model.addAttribute("_date_formatter", Dates.frenchFormatter);
        model.addAttribute("_authenticated", false);
        model.addAttribute("_admin", false);
        model.addAttribute("_team_admin", false);
        model.addAttribute("_team_member", false);

        getUserFromPrincipal(principal).ifPresent(user -> {
            model.addAttribute("_authenticated", true);
            model.addAttribute("_admin", user.isAdmin());
            model.addAttribute("_profile_image", user.getProfileImage());
            model.addAttribute("_user_id", user.getId());
            model.addAttribute("_identity", user.getIdentity());
            if (team != null) {
                model.addAttribute("_team_admin", user.isAdmin(team.getId()));
                model.addAttribute("_team_member", user.isMember(team.getId()));
            }
        });

        if (team != null) {
            model.addAttribute("team", team);
        }

    }

    protected void addOpenGraphValues(
            Team team,
            Model model,
            String title,
            String image,
            String url,
            String description) {

        Map<String, String> og = new HashMap<>();
        if (team.getDescription().getTwitter() != null) {
            og.put("twitter:image:src", image);
            og.put("twitter:site", "@" + team.getDescription().getTwitter());
            og.put("twitter:card", "summary_large_image");
            og.put("twitter:title", title);
            og.put("twitter:description", description);
        }

        og.put("og:image", image);
        og.put("og:image:alt", "Détails");
        og.put("og:image:width", "1200");
        og.put("og:image:height", "600");
        og.put("og:site_name", team.getName());
        og.put("og:type", "object");
        og.put("og:title", title);
        og.put("og:url", url);
        og.put("og:description", description);

        model.addAttribute("og", og);

    }

    protected Optional<User> getUserFromPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken wrapperPrincipal = (OAuth2AuthenticationToken) principal;
            LocalDefaultOAuth2User oauthprincipal = (LocalDefaultOAuth2User) wrapperPrincipal.getPrincipal();
            return userService.get(oauthprincipal.getLocalUserId());
        }
        return Optional.empty();
    }

    protected void checkAdmin(Principal principal, String teamId) {
        boolean admin = false;
        final Optional<User> optionalUser = getUserFromPrincipal(principal);
        if (optionalUser.isPresent()) {
            final User user = optionalUser.get();
            admin = user.isAdmin() || user.isAdmin(teamId);
        }
        if (!admin) {
            throw new IllegalStateException("User is not admin");
        }
    }

    protected List<String> getAllAvailableTimeZones() {
        return ZoneId.getAvailableZoneIds().stream().map(ZoneId::of).map(ZoneId::toString).sorted().collect(Collectors.toList());
    }

    protected Team checkTeam(String teamId) {
        return teamService.get(teamId).orElseThrow(() -> new IllegalArgumentException("Unknown team " + teamId));
    }

    protected String redirectToRides(String teamId) {
        return "redirect:/" + teamId + "/rides";
    }

    protected String redirectToRide(String teamId, String rideId) {
        return "redirect:/" + teamId + "/rides/" + rideId;
    }

    protected String redirectToMaps(String teamId) {
        return "redirect:/" + teamId + "/maps";
    }

    protected String redirectToMap(String teamId, String mapId) {
        return "redirect:/" + teamId + "/maps/" + mapId;
    }

    protected String redirectToFeed(String teamId) {
        return "redirect:/" + teamId;
    }

    protected String redirectToHome() {
        return "redirect:/";
    }

}