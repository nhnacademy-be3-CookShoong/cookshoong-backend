package store.cookshoong.www.cookshoongbackend.address.model.request;

import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import store.cookshoong.www.cookshoongbackend.common.util.RegularExpressions;
import store.cookshoong.www.cookshoongbackend.common.util.ValidationFailureMessages;

/**
 * 회원이 주소를 등록할 때.
 * 회원과 주소관계에서 별칭을 저장하고 주소로 가서 메인, 상세주소, 위도, 경도를 생성
 *
 * @author jeongjewan
 * @since 2023.07.04
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateAccountAddressRequestDto {

    @NotBlank
    private String alias;

    @NotBlank
    @Length(min = 1, max = 80)
    @Pattern(regexp = RegularExpressions.MAIN_DETAIL_ADDRESS, message = ValidationFailureMessages.MAIN_DETAIL_ADDRESS)
    private String mainPlace;

    @Length(min = 1, max = 80)
    @Pattern(regexp = RegularExpressions.MAIN_DETAIL_ADDRESS, message = ValidationFailureMessages.MAIN_DETAIL_ADDRESS)
    private String detailPlace;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;
}
