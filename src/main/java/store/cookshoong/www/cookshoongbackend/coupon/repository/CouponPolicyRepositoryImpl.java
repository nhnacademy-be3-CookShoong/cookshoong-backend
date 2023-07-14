package store.cookshoong.www.cookshoongbackend.coupon.repository;

import static store.cookshoong.www.cookshoongbackend.coupon.entity.QCouponPolicy.couponPolicy;
import static store.cookshoong.www.cookshoongbackend.coupon.entity.QCouponType.couponType;
import static store.cookshoong.www.cookshoongbackend.coupon.entity.QCouponUsage.couponUsage;
import static store.cookshoong.www.cookshoongbackend.coupon.entity.QCouponUsageAll.couponUsageAll;
import static store.cookshoong.www.cookshoongbackend.coupon.entity.QCouponUsageMerchant.couponUsageMerchant;
import static store.cookshoong.www.cookshoongbackend.coupon.entity.QCouponUsageStore.couponUsageStore;
import static store.cookshoong.www.cookshoongbackend.shop.entity.QMerchant.merchant;
import static store.cookshoong.www.cookshoongbackend.shop.entity.QStore.store;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import store.cookshoong.www.cookshoongbackend.coupon.model.response.QSelectPolicyResponseTempDto;
import store.cookshoong.www.cookshoongbackend.coupon.model.temp.SelectPolicyResponseTempDto;

/**
 * QueryDSL CouponPolicyRepository.
 * 쿠폰 정책을 조회할 수 있도록 한다.
 *
 * @author eora21(김주호)
 * @since 2023.07.14
 */
@RequiredArgsConstructor
public class CouponPolicyRepositoryImpl implements CouponPolicyRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<SelectPolicyResponseTempDto> lookupStorePolicy(Long storeId, Pageable pageable) {
        return getCouponPolicyPage(getCouponUsageStoreId(storeId), pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<SelectPolicyResponseTempDto> lookupMerchantPolicy(Long merchantId, Pageable pageable) {
        return getCouponPolicyPage(getCouponUsageMerchantId(merchantId), pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<SelectPolicyResponseTempDto> lookupAllPolicy(Pageable pageable) {
        return getCouponPolicyPage(getCouponUsageAllId(), pageable);
    }

    private Page<SelectPolicyResponseTempDto> getCouponPolicyPage(JPQLQuery<Long> couponUsageId, Pageable pageable) {
        List<SelectPolicyResponseTempDto> couponPolicy = getCouponPolicy(couponUsageId, pageable);
        Long total = getTotal(couponUsageId);

        return new PageImpl<>(couponPolicy, pageable, total);
    }

    private List<SelectPolicyResponseTempDto> getCouponPolicy(JPQLQuery<Long> couponUsageId, Pageable pageable) {
        return queryFactory
            .select(new QSelectPolicyResponseTempDto(
                couponPolicy.id,
                couponType,
                couponPolicy.name,
                couponPolicy.description,
                couponPolicy.expirationTime
            ))
            .from(couponPolicy)

            .innerJoin(couponPolicy.couponType, couponType)
            .innerJoin(couponPolicy.couponUsage, couponUsage)

            .where(couponUsage.id.eq(couponUsageId))

            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    private static JPQLQuery<Long> getCouponUsageStoreId(Long storeId) {
        return JPAExpressions.select(couponUsageStore.id)
            .from(couponUsageStore)
            .innerJoin(couponUsageStore.store, store)
            .where(store.id.eq(storeId));
    }

    private static JPQLQuery<Long> getCouponUsageMerchantId(Long merchantId) {
        return JPAExpressions.select(couponUsageStore.id)
            .from(couponUsageMerchant)
            .innerJoin(couponUsageMerchant.merchant, merchant)
            .where(merchant.id.eq(merchantId));
    }

    private static JPQLQuery<Long> getCouponUsageAllId() {
        return JPAExpressions.select(couponUsageStore.id)
            .from(couponUsageAll)
            .limit(1L);
    }

    private Long getTotal(JPQLQuery<Long> couponUsageId) {
        return queryFactory
            .select(couponPolicy.count())
            .from(couponPolicy)

            .innerJoin(couponPolicy.couponType, couponType)
            .innerJoin(couponPolicy.couponUsage, couponUsage)

            .where(couponUsage.id.eq(couponUsageId))
            .fetchOne();
    }
}