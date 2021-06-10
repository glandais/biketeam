package info.tomacla.biketeam.domain.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

    List<User> findAll();

    Optional<User> findByStravaId(Long stravaId);

    List<User> findByEmailNotNull();

}
