package study.datajpastart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.datajpastart.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
