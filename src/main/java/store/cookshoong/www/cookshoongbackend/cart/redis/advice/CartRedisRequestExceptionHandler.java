package store.cookshoong.www.cookshoongbackend.cart.redis.advice;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import store.cookshoong.www.cookshoongbackend.cart.redis.exception.CreateCartRedisValidationException;
import store.cookshoong.www.cookshoongbackend.cart.redis.exception.InvalidStoreException;
import store.cookshoong.www.cookshoongbackend.cart.redis.exception.ModifyCartMenuValidationException;
import store.cookshoong.www.cookshoongbackend.cart.redis.exception.NotFoundCartRedisKey;
import store.cookshoong.www.cookshoongbackend.cart.redis.exception.NotFoundMenuRedisHashKey;
import store.cookshoong.www.cookshoongbackend.common.exception.ErrorMessage;
import store.cookshoong.www.cookshoongbackend.common.exception.NotFoundException;
import store.cookshoong.www.cookshoongbackend.common.exception.ValidationFailureException;

/**
 * Redis 장바구니에 대한 Exception Advice.
 *
 * @author jeongjewan
 * @since 2023.07.23
 */
@Slf4j
@RestControllerAdvice(basePackages = "store.cookshoong.www.cookshoongbackend.cart.redis")
public class CartRedisRequestExceptionHandler {


    /**
     * (400) 검증 실패에 대한 응답.
     *
     * @param e             검증 실패 예외
     * @return              검증 실패 필드와 에러 메시지 반환
     */
    @ExceptionHandler({CreateCartRedisValidationException.class, ModifyCartMenuValidationException.class})
    public ResponseEntity<Map<String, String>> handleValidationFailure(ValidationFailureException e) {

        return ResponseEntity.badRequest().body(e.getErrors());
    }

    /**
     * (404) 객체 조회 실패에 대한 예외.
     *
     * @param e             조회실패 예외
     * @return              조회 실패 필드와 에러 메시지 반환
     */
    @ExceptionHandler({NotFoundCartRedisKey.class, NotFoundMenuRedisHashKey.class})
    public ResponseEntity<ErrorMessage> accountAddressNotFoundException(NotFoundException e) {


        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(e));
    }

    /**
     * (409) 서버의 상태와 충돌시에 대한 응답(중복값 충돌, ...)..
     *
     * @param e             주소 10개 초과 예외.
     * @return              주소 10개 초과 에러 메시지 반환
     */
    @ExceptionHandler({InvalidStoreException.class})
    public ResponseEntity<ErrorMessage> maxAddressLimitException(RuntimeException e) {


        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorMessage(e));
    }
}
