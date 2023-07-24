package store.cookshoong.www.cookshoongbackend.shop.exception.holiday;

import org.springframework.validation.BindingResult;
import store.cookshoong.www.cookshoongbackend.common.exception.ValidationFailureException;

/**
 * 휴업일 생성 요청 데이터의 위반사항 발견 발생되는 예외.
 *
 * @author papel (윤동현)
 * @since 2023.07.07
 */
public class HolidayValidationException extends ValidationFailureException {
    public HolidayValidationException(BindingResult bindingResult) {
        super(bindingResult);
    }
}

