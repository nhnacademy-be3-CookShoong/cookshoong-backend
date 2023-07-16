package store.cookshoong.www.cookshoongbackend.menu.exception.option;

import org.springframework.validation.BindingResult;
import store.cookshoong.www.cookshoongbackend.common.exception.ValidationFailureException;

/**
 * 옵션 생성 요청 데이터의 위반사항 발견 발생되는 예외.
 *
 * @author papel
 * @since 2023.07.13
 */
public class OptionValidationException extends ValidationFailureException {
    public OptionValidationException(BindingResult bindingResult) {
        super(bindingResult);
    }
}
