package store.cookshoong.www.cookshoongbackend.coupon.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import store.cookshoong.www.cookshoongbackend.coupon.entity.CouponLogType;

/**
 * 쿠폰 내역 타입 repository.
 *
 * @author eora21
 * @since 2023.07.04
 */
public interface CouponLogTypeRepository extends JpaRepository<CouponLogType, String> {
    default Optional<CouponLogType> findByCouponLogTypeCode(CouponLogType.Code couponLogTypeCode) {
        return findById(couponLogTypeCode.name());
    }
}
