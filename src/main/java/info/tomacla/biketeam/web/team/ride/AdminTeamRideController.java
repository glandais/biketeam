package info.tomacla.biketeam.web.team.ride;

import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.template.RideTemplate;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.RideTemplateService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}/admin/rides")
public class AdminTeamRideController extends AbstractController {

    @Autowired
    private RideService rideService;

    @Autowired
    private MapService mapService;

    @Autowired
    private RideTemplateService rideTemplateService;


    @GetMapping
    public String getRides(@PathVariable("teamId") String teamId, Principal principal, Model model) {

        checkAdmin(principal, teamId);
        final Team team = checkTeam(teamId);

        addGlobalValues(principal, model, "Administration - Rides", team);
        model.addAttribute("rides", rideService.listRides(teamId));
        model.addAttribute("templates", rideTemplateService.listTemplates(teamId));
        return "team_admin_rides";
    }

    @GetMapping(value = "/new")
    public String newRide(@PathVariable("teamId") String teamId,
                          @RequestParam(value = "templateId", required = false, defaultValue = "empty-1") String templateId,
                          Principal principal,
                          Model model) {

        checkAdmin(principal, teamId);
        final Team team = checkTeam(teamId);

        NewRideForm form = null;

        if (templateId != null && !templateId.startsWith("empty-")) {
            Optional<RideTemplate> optionalTemplate = rideTemplateService.get(teamId, templateId);
            if (optionalTemplate.isPresent()) {
                form = NewRideForm.builder(optionalTemplate.get()).get();
            }
        }

        if (form == null && templateId.startsWith("empty-")) {
            int numberOfGroups = Integer.parseInt(templateId.replace("empty-", ""));
            form = NewRideForm.builder(numberOfGroups).get();
        }

        if (form == null) {
            form = NewRideForm.builder(1).get();
        }

        addGlobalValues(principal, model, "Administration - Nouveau ride", team);
        model.addAttribute("formdata", form);
        model.addAttribute("published", false);
        return "team_admin_rides_new";

    }

    @GetMapping(value = "/{rideId}")
    public String editRide(@PathVariable("teamId") String teamId,
                           @PathVariable("rideId") String rideId,
                           Principal principal,
                           Model model) {

        checkAdmin(principal, teamId);
        final Team team = checkTeam(teamId);

        Optional<Ride> optionalRide = rideService.get(teamId, rideId);
        if (optionalRide.isEmpty()) {
            return redirectToAdminRides(teamId);
        }

        Ride ride = optionalRide.get();

        NewRideForm form = NewRideForm.builder(ride.getGroups().size())
                .withId(ride.getId())
                .withDate(ride.getDate())
                .withDescription(ride.getDescription())
                .withType(ride.getType())
                .withPublishedAt(ride.getPublishedAt())
                .withTitle(ride.getTitle())
                .withGroups(ride.getGroups(), teamId, mapService)
                .get();

        addGlobalValues(principal, model, "Administration - Modifier le ride", team);
        model.addAttribute("formdata", form);
        model.addAttribute("published", ride.getPublishedStatus().equals(PublishedStatus.PUBLISHED));
        return "team_admin_rides_new";

    }

    @PostMapping(value = "/{rideId}")
    public String editRide(@PathVariable("teamId") String teamId,
                           @PathVariable("rideId") String rideId,
                           Principal principal,
                           Model model,
                           NewRideForm form) {

        checkAdmin(principal, teamId);
        final Team team = checkTeam(teamId);

        try {

            boolean isNew = rideId.equals("new");
            final ZoneId timezone = ZoneId.of(team.getConfiguration().getTimezone());

            NewRideForm.NewRideFormParser parser = form.parser();
            Ride target;
            if (!isNew) {
                Optional<Ride> optionalRide = rideService.get(teamId, rideId);
                if (optionalRide.isEmpty()) {
                    return redirectToAdminRides(teamId);
                }
                target = optionalRide.get();
                target.setDate(parser.getDate());
                target.setPublishedAt(parser.getPublishedAt(timezone));
                target.setTitle(parser.getTitle());
                target.setDescription(parser.getDescription());
                target.setType(parser.getType());
            } else {
                target = new Ride(teamId, parser.getType(), parser.getDate(), parser.getPublishedAt(timezone),
                        parser.getTitle(), parser.getDescription(), parser.getFile().isPresent(), null);
            }

            target.clearGroups();
            parser.getGroups(teamId, mapService).forEach(target::addGroup);

            if (parser.getFile().isPresent()) {
                target.setImaged(true);
                MultipartFile uploadedFile = parser.getFile().get();
                rideService.saveImage(teamId, target.getId(), form.getFile().getInputStream(), uploadedFile.getOriginalFilename());
            }

            rideService.save(target);

            addGlobalValues(principal, model, "Administration - Rides", team);
            model.addAttribute("rides", rideService.listRides(teamId));
            model.addAttribute("templates", rideTemplateService.listTemplates(teamId));
            return "team_admin_rides";


        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Modifier le ride", team);
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            return "team_admin_rides_new";
        }

    }


    @GetMapping(value = "/delete/{rideId}")
    public String deleteRide(@PathVariable("teamId") String teamId,
                             @PathVariable("rideId") String rideId,
                             Principal principal,
                             Model model) {

        checkAdmin(principal, teamId);

        try {
            rideService.delete(teamId, rideId);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return redirectToAdminRides(teamId);

    }

    private String redirectToAdminRides(String teamId) {
        return "redirect:/" + teamId + "/admin/rides";
    }

}