package store.cookshoong.www.cookshoongbackend.menu.repository.option;

import org.springframework.data.jpa.repository.JpaRepository;
import store.cookshoong.www.cookshoongbackend.menu.entity.option.Option;

/**
 * 옵션 레포지토리.
 *
 * @author papel
 * @since 2023.07.11
 */
public interface OptionRepository extends JpaRepository<Option, Long> {
}
