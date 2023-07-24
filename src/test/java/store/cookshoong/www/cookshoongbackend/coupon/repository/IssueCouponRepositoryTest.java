package store.cookshoong.www.cookshoongbackend.coupon.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import store.cookshoong.www.cookshoongbackend.account.entity.Account;
import store.cookshoong.www.cookshoongbackend.config.QueryDslConfig;
import store.cookshoong.www.cookshoongbackend.coupon.entity.CouponPolicy;
import store.cookshoong.www.cookshoongbackend.coupon.entity.IssueCoupon;
import store.cookshoong.www.cookshoongbackend.util.TestEntity;
import store.cookshoong.www.cookshoongbackend.util.TestPersistEntity;

@DataJpaTest
@Import({TestPersistEntity.class, QueryDslConfig.class})
class IssueCouponRepositoryTest {
    @Autowired
    IssueCouponRepository issueCouponRepository;

    @Autowired
    TestEntityManager em;

    @Autowired
    TestEntity te;

    @Autowired
    TestPersistEntity tpe;

    CouponPolicy couponPolicy;

    @BeforeEach
    void beforeEach() {
        couponPolicy = te.getCouponPolicy(te.getCouponTypeCash_1000_10000(), te.getCouponUsageAll());
        Account customer = tpe.getLevelOneActiveCustomer();

        for (int i = 0; i < 2; i++) {
            te.getIssueCoupon(couponPolicy).provideToAccount(customer);
        }

        for (int i = 0; i < 8; i++) {
            te.getIssueCoupon(couponPolicy);
        }

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("미발급 쿠폰 획득 테스트 - 사용자에게 발급된 쿠폰 가져오지 않는지")
    void findAccountIsNullTest() throws Exception {
        List<IssueCoupon> issueCoupons = issueCouponRepository.findTop10ByCouponPolicyAndAccountIsNull(couponPolicy);
        assertThat(issueCoupons).hasSize(8);

        issueCoupons.forEach(issueCoupon -> assertThat(issueCoupon.getAccount()).isNull());
    }

    @Test
    @DisplayName("미발급 쿠폰 획득 테스트 - 최대 10개만 가져오는지")
    void findTop10Test() throws Exception {
        for (int i = 0; i < 3; i++) {
            te.getIssueCoupon(couponPolicy);
        }

        em.flush();
        em.clear();

        List<IssueCoupon> issueCoupons = issueCouponRepository.findTop10ByCouponPolicyAndAccountIsNull(couponPolicy);
        assertThat(issueCoupons).hasSize(10);

        issueCoupons.forEach(issueCoupon -> assertThat(issueCoupon.getAccount()).isNull());
    }
}
