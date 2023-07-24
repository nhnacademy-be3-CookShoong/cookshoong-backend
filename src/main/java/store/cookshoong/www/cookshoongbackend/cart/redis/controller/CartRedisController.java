package store.cookshoong.www.cookshoongbackend.cart.redis.controller;

import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.cookshoong.www.cookshoongbackend.cart.redis.exception.CreateCartRedisValidationException;
import store.cookshoong.www.cookshoongbackend.cart.redis.exception.ModifyCartMenuValidationException;
import store.cookshoong.www.cookshoongbackend.cart.redis.model.vo.CartMenuCountDto;
import store.cookshoong.www.cookshoongbackend.cart.redis.model.vo.CartRedisDto;
import store.cookshoong.www.cookshoongbackend.cart.redis.service.CartRedisService;

/**
 * 장바구니를 통해 요청과 응답에 대해서 Redis 로 처리하는 RestController.
 *
 * @author jeongjewan
 * @since 2023.07.22
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartRedisController {

    private final CartRedisService cartRedisService;

    /**
     * Redis 장바구니에 생성할 메서드.
     *
     * @param cartKey       redis key
     * @param menuKey       redis hashKey
     * @param cart          redis 장바구니에 저장될 객체
     * @return              상태코드 201(CREATED)와 함께 응답을 반환
     */
    @PostMapping("/{cartKey}/add-menu/{menuKey}")
    public ResponseEntity<Void> postCreateCart(@PathVariable String cartKey,
                                               @PathVariable String menuKey,
                                               @RequestBody @Valid CartRedisDto cart,
                                               BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new CreateCartRedisValidationException(bindingResult);
        }

        cartRedisService.createCartMenu(cartKey, menuKey, cart);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Redis 장바구니에서 메뉴를 수정할 메서드.
     *
     * @param cartKey       redis key
     * @param menuKey       redis hashKey
     * @param cart          redis 장바구니에 저장될 객체
     * @return              상태코드 200(Ok)와 함께 응답을 반환
     */
    @PutMapping("/{cartKey}/modify-menu/{menuKey}")
    public ResponseEntity<Void> putModifyCartMenu(@PathVariable String cartKey,
                                                  @PathVariable String menuKey,
                                                  @RequestBody @Valid CartRedisDto cart,
                                                  BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ModifyCartMenuValidationException(bindingResult);
        }

        cartRedisService.modifyCartMenuRedis(cartKey, menuKey, cart);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Redis 장바구니에 들어 있는 모든 메뉴를 조회하는 메서드.
     *
     * @param cartKey       redis Key
     * @return              상태코드 200(Ok)와 함께 응답을 반환 & 장바구니에서 보여줄 모든 메뉴에 대한 정보 반환
     */
    @GetMapping("/{cartKey}")
    public ResponseEntity<List<CartRedisDto>> getSelectCartMenuAll(@PathVariable String cartKey) {

        List<CartRedisDto> carts = cartRedisService.selectCartMenuAll(cartKey);

        return ResponseEntity.status(HttpStatus.OK).body(carts);
    }

    /**
     * Redis 장바구니 key 에 해당 메뉴를 조회하는 메서드.
     *
     * @param cartKey       redis Key
     * @param menuKey       redis hashKey
     * @return              상태코드 200(Ok)와 함께 응답을 반환 & 장바구니에서 해당 메뉴에 대한 정보를 반환
     */
    @GetMapping("/{cartKey}/menu/{menuKey}")
    public ResponseEntity<CartRedisDto> getSelectCartMenu(@PathVariable String cartKey,
                                                          @PathVariable String menuKey) {

        CartRedisDto cartMenu = cartRedisService.selectCartMenu(cartKey, menuKey);

        return ResponseEntity.status(HttpStatus.OK).body(cartMenu);
    }

    /**
     * Redis 장바구니에 저장되어 있는 메뉴들에 개수를 조회하는 메서드.
     *
     * @param cartKey       redis Key
     * @return              상태코드 200(Ok)와 함께 응답을 반환 & 장바구니에서 저장되어 있는 메뉴들에 대한 개수를 반환
     */
    @GetMapping("/{cartKey}/counts")
    public ResponseEntity<CartMenuCountDto> getSelectCartCount(@PathVariable String cartKey) {

        CartMenuCountDto cartCount = new CartMenuCountDto(cartRedisService.selectCartCount(cartKey));

        return ResponseEntity.status(HttpStatus.OK).body(cartCount);
    }

    /**
     * Redis 장바구니에 key 에 해당 메뉴를 삭제하는 메서드.
     *
     * @param cartKey       redis Key
     * @param menuKey       redis hashKey
     * @return              상태코드 204(NO_CONTENT)와 함께 응답을 반환
     */
    @DeleteMapping("/{cartKey}/menu-delete/{menuKey}")
    public ResponseEntity<Void> deleteCartMenu(@PathVariable String cartKey,
                                               @PathVariable String menuKey) {

        cartRedisService.removeCartMenu(cartKey, menuKey);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Redis 장바구니에 key 에 모든 메뉴를 삭제하는 메서드.
     *
     * @param cartKey       redis Key
     * @return              상태코드 204(NO_CONTENT)와 함께 응답을 반환
     */
    @DeleteMapping("/{cartKey}/delete-all")
    public ResponseEntity<Void> deleteCartMenuAll(@PathVariable String cartKey) {

        cartRedisService.removeCartMenuAll(cartKey);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}