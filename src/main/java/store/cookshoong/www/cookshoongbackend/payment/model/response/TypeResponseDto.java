package store.cookshoong.www.cookshoongbackend.payment.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 결제 타입과 환불 타입에 name 에 대한 Response Dto.
 *
 * @author jeongjewan
 * @since 2023.07.06
 */
@Getter
@AllArgsConstructor
public class TypeResponseDto {

    private String name;
}