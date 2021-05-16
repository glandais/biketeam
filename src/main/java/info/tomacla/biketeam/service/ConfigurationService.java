package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.Timezone;
import info.tomacla.biketeam.domain.global.*;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConfigurationService {

    @Autowired
    private SiteDescriptionRepository siteDescriptionRepository;

    @Autowired
    private SiteConfigurationRepository siteConfigurationRepository;

    @Autowired
    private SiteIntegrationRepository siteIntegrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${default.sitename}")
    private String defaultSiteName;

    @Value("${default.description}")
    private String defaultDescription;

    @Value("${admin.strava-id}")
    private Long adminStravaId;

    @Value("${admin.first-name}")
    private String adminFirstName;

    @Value("${admin.last-name}")
    private String adminLastName;

    public List<String> getDefaultSearchTags() {
        List<String> defaultSearchTags = siteConfigurationRepository.findById(1L).get().getDefaultSearchTags();
        if (defaultSearchTags == null) {
            return new ArrayList<>();
        }
        return defaultSearchTags;
    }

    public ZoneId getTimezone() {
        return ZoneId.of(siteConfigurationRepository.findById(1L).get().getTimezone());
    }

    @PostConstruct
    public void init() {

        Optional<SiteDescription> siteDescription = siteDescriptionRepository.findById(1L);
        if (siteDescription.isEmpty()) {
            siteDescriptionRepository.save(new SiteDescription(
                    defaultSiteName,
                    defaultDescription));
        }

        Optional<SiteIntegration> siteIntegration = siteIntegrationRepository.findById(1L);
        if (siteIntegration.isEmpty()) {
            siteIntegrationRepository.save(new SiteIntegration(null));
        }

        Optional<SiteConfiguration> siteConfiguration = siteConfigurationRepository.findById(1L);
        if (siteConfiguration.isEmpty()) {
            siteConfigurationRepository.save(new SiteConfiguration(Timezone.DEFAULT_TIMEZONE, null));
        }

        if (userRepository.findByStravaId(adminStravaId).isEmpty()) {
            userRepository.save(new User(true,
                    adminFirstName,
                    adminLastName,
                    adminStravaId,
                    null,
                    null,
                    null,
                    null));
        }

    }


}