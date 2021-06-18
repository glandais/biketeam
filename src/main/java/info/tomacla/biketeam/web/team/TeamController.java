package info.tomacla.biketeam.web.team;

import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRole;
import info.tomacla.biketeam.service.FacebookService;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/")
public class TeamController extends AbstractController {

    @Autowired
    protected TeamService teamService;

    @Autowired
    private FacebookService facebookService;

    @GetMapping
    public String getTeams(Principal principal, Model model) {
        addGlobalValues(principal, model, "Accueil", null);
        model.addAttribute("teams", teamService.list());
        return "root";
    }

    @GetMapping(value = "new")
    public String newTeam(Principal principal, Model model) {
        addGlobalValues(principal, model, "Créer une team", null);
        model.addAttribute("formdata", NewTeamForm.builder().get());
        model.addAttribute("timezones", getAllAvailableTimeZones());
        return "new";
    }

    @PostMapping(value = "new")
    public String submitNewTeam(NewTeamForm form, Principal principal, Model model) {

        try {

            final User targetAdmin = getUserFromPrincipal(principal).orElseThrow(() -> new IllegalStateException("User not authenticated"));

            final Team newTeam = new Team(Strings.permatitleFromString(form.getName()),
                    form.getName(),
                    form.getCity(),
                    form.getCountry(),
                    form.getDescription(),
                    null);

            newTeam.addRole(UserRole.admin(targetAdmin.getId(), newTeam.getId()));

            teamService.save(newTeam);
            teamService.initTeamImage(newTeam);

            return "redirect:/" + newTeam.getId();

        } catch (Exception e) {
            addGlobalValues(principal, model, "Créer une team", null);
            model.addAttribute("formdata", form);
            model.addAttribute("timezones", getAllAvailableTimeZones());
            return "new";
        }


    }

    @GetMapping(value = "/login-error")
    public String loginError(Principal principal, Model model) {
        addGlobalValues(principal, model, "Accueil", null);
        model.addAttribute("errors", List.of("Erreur de connexion"));
        model.addAttribute("teams", teamService.list());
        return "root";
    }

    @GetMapping(value = "/integration/facebook/login")
    public String getSiteIntegration(@RequestParam("state") String teamId,
                                     @RequestParam("code") String facebookCode,
                                     Principal principal,
                                     Model model) {

        checkAdmin(principal, teamId);
        final Team team = checkTeam(teamId);

        final String userAccessToken = facebookService.getUserAccessToken(facebookCode);
        team.getIntegration().setFacebookAccessToken(userAccessToken);

        teamService.save(team);

        return "redirect:/" + teamId + "/admin/integration";

    }

}