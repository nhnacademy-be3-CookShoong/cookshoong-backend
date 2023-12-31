package store.cookshoong.www.cookshoongbackend.shop.repository.businesshour;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import store.cookshoong.www.cookshoongbackend.shop.entity.BusinessHour;

/**
 * 영업시간 레포지토리.
 *
 * @author seungyeon
 * @since 2023.07.04
 */
public interface BusinessHourRepository extends JpaRepository<BusinessHour, Long>, BusinessHourRepositoryCustom {

    List<BusinessHour> findByStoreId(Long storeId);
}
