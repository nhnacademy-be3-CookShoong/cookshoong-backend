package store.cookshoong.www.cookshoongbackend.coupon.model.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import store.cookshoong.www.cookshoongbackend.coupon.entity.CouponTypeCash;

/**
 * 가게에서 금액 쿠폰 정책을 생성할 때 사용되는 dto.
 *
 * @author eora21
 * @since 2023.07.04
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateCashCouponPolicyRequestDto extends AbstractCouponPolicyRequest {
    @NotNull
    @Min(value = 1_000)
    @Max(value = 50_000)
    private Integer discountAmount;

    @NotNull
    @Min(value = 0)
    @Max(value = 20_000)
    private Integer minimumOrderPrice;

    /**
     * dto 값을 이용하여 CouponTypeCash 엔티티를 생성하는 static method.
     *
     * @param dto the dto
     * @return the coupon type cash entity
     */
    public static CouponTypeCash toEntity(CreateCashCouponPolicyRequestDto dto) {
        return new CouponTypeCash(dto.getDiscountAmount(), dto.getMinimumOrderPrice());
    }
}

