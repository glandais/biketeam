package info.tomacla.biketeam.web.admin.configuration;

import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.domain.global.SiteConfiguration;
import info.tomacla.biketeam.domain.global.SiteDescription;
import info.tomacla.biketeam.domain.global.SiteIntegration;
import info.tomacla.biketeam.domain.map.MapRepository;
import info.tomacla.biketeam.service.FacebookService;
import info.tomacla.biketeam.service.FileService;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/admin")
public class AdminConfigurationController extends AbstractController {

    @Autowired
    private FileService fileService;

    @Autowired
    private MapService mapService;

    @Autowired
    private FacebookService facebookService;

    @GetMapping
    public String getSiteDescription(Principal principal, Model model) {

        SiteDescription siteDescription = configurationService.getSiteDescription();

        EditSiteDescriptionForm form = EditSiteDescriptionForm.builder()
                .withSitename(siteDescription.getSitename())
                .withDescription(siteDescription.getDescription())
                .withFacebook(siteDescription.getFacebook())
                .withTwitter(siteDescription.getTwitter())
                .withEmail(siteDescription.getEmail())
                .withPhoneNumber(siteDescription.getPhoneNumber())
                .withAddressStreetLine(siteDescription.getAddressStreetLine())
                .withAddressPostalCode(siteDescription.getAddressPostalCode())
                .withAddressCity(siteDescription.getAddressCity())
                .withOther(siteDescription.getOther())
                .get();

        addGlobalValues(principal, model, "Administration - Description");
        model.addAttribute("formdata", form);
        return "admin_description";
    }

    @PostMapping
    public String updateSiteDescription(Principal principal, Model model,
                                        EditSiteDescriptionForm form) {

        try {

            SiteDescription siteDescription = configurationService.getSiteDescription();
            siteDescription.setSitename(form.getSitename());
            siteDescription.setDescription(form.getDescription());
            siteDescription.setFacebook(form.getFacebook());
            siteDescription.setTwitter(form.getTwitter());
            siteDescription.setEmail(form.getEmail());
            siteDescription.setPhoneNumber(form.getPhoneNumber());
            siteDescription.setAddressStreetLine(form.getAddressStreetLine());
            siteDescription.setAddressPostalCode(form.getAddressPostalCode());
            siteDescription.setAddressCity(form.getAddressCity());
            siteDescription.setOther(form.getOther());
            siteDescriptionRepository.save(siteDescription);


            addGlobalValues(principal, model, "Administration - Description");
            model.addAttribute("formdata", form);
            return "admin_description";

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Description");
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            return "admin_description";
        }

    }

    @GetMapping(value = "/configuration")
    public String getSiteConfiguration(Principal principal, Model model) {

        SiteConfiguration configuration = configurationService.getSiteConfiguration();
        EditSiteConfigurationForm form = EditSiteConfigurationForm.builder()
                .withTimezone(configuration.getTimezone())
                .withDefaultSearchTags(configuration.getDefaultSearchTags())
                .get();

        addGlobalValues(principal, model, "Administration - Configuration");
        model.addAttribute("formdata", form);
        model.addAttribute("timezones", getAllAvailableTimeZones());
        model.addAttribute("tags", mapService.listTags());
        return "admin_configuration";
    }

    @PostMapping(value = "/configuration")
    public String updateSiteConfiguration(Principal principal,
                                          Model model,
                                          EditSiteConfigurationForm form) {

        try {
            SiteConfiguration siteConfiguration = configurationService.getSiteConfiguration();
            EditSiteConfigurationForm.EditSiteConfigurationFormParser parser = form.parser();

            siteConfiguration.setTimezone(parser.getTimezone());
            siteConfiguration.setDefaultSearchTags(parser.getDefaultSearchTags());
            siteConfigurationRepository.save(siteConfiguration);

            addGlobalValues(principal, model, "Administration - Configuration");
            model.addAttribute("formdata", form);
            model.addAttribute("timezones", getAllAvailableTimeZones());
            model.addAttribute("tags", mapService.listTags());
            return "admin_configuration";

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Configuration");
            model.addAttribute("formdata", form);
            model.addAttribute("timezones", getAllAvailableTimeZones());
            model.addAttribute("tags", mapService.listTags());
            return "admin_configuration";

        }

    }

    @GetMapping(value = "/integration/facebook/login")
    public String getSiteIntegration(@RequestParam("code") String facebookCode,
                                     Principal principal,
                                     Model model) {

        try {
            SiteIntegration siteIntegration = configurationService.getSiteIntegration();
            final String userAccessToken = facebookService.getUserAccessToken(facebookCode);
            siteIntegration.setFacebookAccessToken(userAccessToken);

            siteIntegrationRepository.save(siteIntegration);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return "redirect:/admin/integration";
        }

    }

    @GetMapping(value = "/integration/facebook/backward")
    public String facebookConfBackward(Principal principal,
                                       Model model) {

        SiteIntegration siteIntegration = configurationService.getSiteIntegration();

        final int facebookConfigurationStep = siteIntegration.getFacebookConfigurationStep();
        if (facebookConfigurationStep == 4) {
            siteIntegration.setFacebookPageId(null);
        } else if (facebookConfigurationStep == 3) {
            siteIntegration.setFacebookAccessToken(null);
        } else if (facebookConfigurationStep == 2) {
            siteIntegration.setFacebookAppId(null);
            siteIntegration.setFacebookAppSecret(null);
        }

        siteIntegrationRepository.save(siteIntegration);

        return "redirect:/admin/integration";

    }

    @GetMapping(value = "/integration")
    public String getSiteIntegration(Principal principal, Model model) {

        SiteIntegration siteIntegration = configurationService.getSiteIntegration();
        EditSiteIntegrationForm form = EditSiteIntegrationForm.builder()
                .withMapBoxAPIKey(siteIntegration.getMapBoxAPIKey())
                .withFacebookAppId(siteIntegration.getFacebookAppId())
                .withFacebookAppSecret(siteIntegration.getFacebookAppSecret())
                .withFacebookPageId(siteIntegration.getFacebookPageId())
                .get();

        addGlobalValues(principal, model, "Administration - Intégrations");
        model.addAttribute("formdata", form);
        model.addAttribute("facebookConfigurationStep", siteIntegration.getFacebookConfigurationStep());
        if (siteIntegration.getFacebookConfigurationStep() == 2) {
            model.addAttribute("facebookUrl", facebookService.getLoginUrl());
        }
        return "admin_integration";
    }

    @PostMapping(value = "/integration")
    public String updateSiteIntegration(Principal principal, Model model,
                                        EditSiteIntegrationForm form) {

        SiteIntegration siteIntegration = configurationService.getSiteIntegration();

        try {

            siteIntegration.setMapBoxAPIKey(form.getMapBoxAPIKey());
            siteIntegration.setFacebookPageId(form.getFacebookPageId());
            siteIntegration.setFacebookAppId(form.getFacebookAppId());
            siteIntegration.setFacebookAppSecret(form.getFacebookAppSecret());
            siteIntegrationRepository.save(siteIntegration);

            addGlobalValues(principal, model, "Administration - Intégrations");
            model.addAttribute("formdata", form);
            model.addAttribute("facebookConfigurationStep", siteIntegration.getFacebookConfigurationStep());
            if (siteIntegration.getFacebookConfigurationStep() == 2) {
                model.addAttribute("facebookUrl", facebookService.getLoginUrl());
            }
            return "admin_integration";

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
            addGlobalValues(principal, model, "Administration - Intégrations");
            model.addAttribute("formdata", form);
            model.addAttribute("facebookConfigurationStep", siteIntegration.getFacebookConfigurationStep());
            if (siteIntegration.getFacebookConfigurationStep() == 2) {
                model.addAttribute("facebookUrl", facebookService.getLoginUrl());
            }
            return "admin_integration";
        }

    }

    @GetMapping(value = "/logo")
    public String getLogo(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Logo");
        return "admin_logo";
    }

    @PostMapping(value = "/logo")
    public String updateLogo(Principal principal, Model model,
                             @RequestParam("file") MultipartFile file) {
        try {
            Optional<FileExtension> optionalFileExtension = FileExtension.findByFileName(file.getOriginalFilename());
            if (optionalFileExtension.isPresent()) {
                Path newLogo = fileService.getTempFileFromInputStream(file.getInputStream());
                fileService.store(newLogo, "logo" + optionalFileExtension.get().getExtension());
            }
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        } finally {
            addGlobalValues(principal, model, "Administration - Général");
        }

        return "admin_logo";

    }


}
