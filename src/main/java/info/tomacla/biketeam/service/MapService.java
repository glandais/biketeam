package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.domain.map.*;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.team.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class MapService {

    private static final Logger log = LoggerFactory.getLogger(MapService.class);

    @Autowired
    private GpxService gpxService;

    @Autowired
    private FileService fileService;

    @Autowired
    private MapRepository mapRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private RideRepository rideRepository;

    public void delete(String teamId, String mapId) {
        log.info("Request map deletion {}", mapId);
        final Optional<Map> optionalMap = get(teamId, mapId);
        if (optionalMap.isPresent()) {
            final Map map = optionalMap.get();
            rideRepository.removeMapIdInGroups(map.getId());
            fileService.delete(FileRepositories.GPX_FILES, map.getTeamId(), map.getId() + ".gpx");
            fileService.delete(FileRepositories.FIT_FILES, map.getTeamId(), map.getId() + ".fit");
            fileService.delete(FileRepositories.MAP_IMAGES, map.getTeamId(), map.getId() + ".png");
            mapRepository.delete(map);
        }
    }

    public void save(Map map) {
        mapRepository.save(map);
    }

    public Map save(Team team, InputStream is, String defaultName, String forceId) {

        log.info("Saving new map with default name {}", defaultName);

        Path gpx = fileService.getTempFileFromInputStream(is);

        final Map newMap = gpxService.parseAndStore(team, gpx, defaultName, forceId);

        return mapRepository.save(newMap);

    }

    public Optional<Map> get(String teamId, String mapId) {
        final Optional<Map> optionalMap = mapRepository.findById(mapId);
        if (optionalMap.isPresent() && optionalMap.get().getTeamId().equals(teamId)) {
            return optionalMap;
        }
        return Optional.empty();
    }

    public List<String> listTags(String teamId) {
        return this.listTags(teamId, null);
    }

    public List<String> listTags(String teamId, String q) {
        if (q == null || q.isBlank()) {
            return mapRepository.findAllDistinctTags(teamId);
        }
        return mapRepository.findDistinctTagsContainer(teamId, q.toLowerCase());
    }

    public List<MapIdNamePostedAtVisibleProjection> searchMaps(String teamId, String q) {
        return (q == null || q.isBlank()) ? mapRepository.findAllByTeamIdOrderByPostedAtDesc(teamId)
                : mapRepository.findAllByTeamIdAndNameContainingIgnoreCaseOrderByPostedAtDesc(teamId, q);
    }

    public List<MapIdNamePostedAtVisibleProjection> listMaps(String teamId) {
        return mapRepository.findAllByTeamIdOrderByPostedAtDesc(teamId);
    }

    public Page<Map> listMaps(String teamId, int pageSize) {
        return this.listMaps(teamId, 0, pageSize);
    }

    public Page<Map> listMaps(String teamId, int page, int pageSize) {
        return this.listMaps(teamId, page, pageSize, null);
    }

    public Page<Map> listMaps(String teamId, int page, int pageSize, MapSorterOption sortOption) {
        return mapRepository.findByTeamId(teamId, PageRequest.of(page, pageSize, getPageSort(sortOption)));
    }

    public Page<Map> searchMaps(String teamId, int page, int pageSize, MapSorterOption sortOption,
                                double lowerDistance, double upperDistance, MapType type,
                                double lowerPositiveElevation, double upperPositiveElevation,
                                List<String> tags, WindDirection windDirection) {
        Sort sort = getPageSort(sortOption);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        SearchMapSpecification spec = new SearchMapSpecification(
                teamId,
                lowerDistance,
                upperDistance,
                type,
                lowerPositiveElevation,
                upperPositiveElevation,
                tags,
                windDirection
        );

        return mapRepository.findAll(spec, pageable);

    }

    public Optional<Path> getFitFile(String teamId, String mapId) {
        String fitName = mapId + ".fit";
        if (fileService.exists(FileRepositories.FIT_FILES, teamId, fitName)) {
            return Optional.of(fileService.get(FileRepositories.FIT_FILES, teamId, fitName));
        }
        return Optional.empty();
    }

    public Optional<Path> getGpxFile(String teamId, String mapId) {
        String gpxName = mapId + ".gpx";
        if (fileService.exists(FileRepositories.GPX_FILES, teamId, gpxName)) {
            return Optional.of(fileService.get(FileRepositories.GPX_FILES, teamId, gpxName));
        }
        return Optional.empty();
    }

    public Optional<Path> getImageFile(String teamId, String mapId) {
        String mapImage = mapId + ".png";
        if (fileService.exists(FileRepositories.MAP_IMAGES, teamId, mapImage)) {
            return Optional.of(fileService.get(FileRepositories.MAP_IMAGES, teamId, mapImage));
        }
        return Optional.empty();
    }

    public void generateImage(String teamId, String mapId) {
        teamService.get(teamId).ifPresent(team ->
                this.getGpxFile(teamId, mapId).ifPresent(gpxFile -> gpxService.generateImage(team, mapId, gpxFile))
        );
    }

    private Sort getPageSort(MapSorterOption sortOption) {
        Sort sort = Sort.by("postedAt").descending();
        if (sortOption != null) {
            if (sortOption.equals(MapSorterOption.SHORT)) {
                sort = Sort.by("length").ascending();
            } else if (sortOption.equals(MapSorterOption.LONG)) {
                sort = Sort.by("length").descending();
            } else if (sortOption.equals(MapSorterOption.HILLY)) {
                sort = Sort.by("positiveElevation").descending();
            } else if (sortOption.equals(MapSorterOption.FLAT)) {
                sort = Sort.by("positiveElevation").ascending();
            }
        }
        return sort;
    }


}
