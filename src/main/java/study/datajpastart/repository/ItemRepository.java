package study.datajpastart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.datajpastart.entity.Item;

public interface ItemRepository extends JpaRepository<Item, String> {
}
