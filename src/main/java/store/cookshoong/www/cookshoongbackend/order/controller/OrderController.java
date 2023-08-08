package store.cookshoong.www.cookshoongbackend.order.controller;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.cookshoong.www.cookshoongbackend.address.model.response.AddressResponseDto;
import store.cookshoong.www.cookshoongbackend.address.service.AddressService;
import store.cookshoong.www.cookshoongbackend.cart.redis.model.vo.CartRedisDto;
import store.cookshoong.www.cookshoongbackend.cart.redis.service.CartRedisService;
import store.cookshoong.www.cookshoongbackend.coupon.service.ProvideCouponService;
import store.cookshoong.www.cookshoongbackend.order.exception.OrderRequestValidationException;
import store.cookshoong.www.cookshoongbackend.order.exception.OutOfDistanceException;
import store.cookshoong.www.cookshoongbackend.order.model.request.CreateOrderRequestDto;
import store.cookshoong.www.cookshoongbackend.order.model.response.CreateOrderResponseDto;
import store.cookshoong.www.cookshoongbackend.order.service.OrderService;
import store.cookshoong.www.cookshoongbackend.shop.service.StoreService;

/**
 * 주문 관련 controller.
 *
 * @author eora21 (김주호)
 * @since 2023.08.02
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final CartRedisService cartRedisService;
    private final ProvideCouponService provideCouponService;
    private final AddressService addressService;
    private final StoreService storeService;

    /**
     * 주문 생성 엔드포인트.
     *
     * @param createOrderRequestDto the create order request dto
     * @return the response entity
     */
    @PostMapping
    public ResponseEntity<CreateOrderResponseDto> postOrder(@RequestBody @Valid
                                                                CreateOrderRequestDto createOrderRequestDto,
                                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new OrderRequestValidationException(bindingResult);
        }

        validOrderDistance(createOrderRequestDto);

        List<CartRedisDto> cartItems = cartRedisService.selectCartMenuAll(
                CartRedisService.CART + createOrderRequestDto.getAccountId());

        int totalPrice = cartRedisService.getTotalPrice(cartItems);

        int discountPrice = getDiscountPrice(createOrderRequestDto, totalPrice);

        orderService.createOrder(createOrderRequestDto, cartItems);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateOrderResponseDto(discountPrice));
    }

    private void validOrderDistance(CreateOrderRequestDto createOrderRequestDto) {
        AddressResponseDto addressResponseDto =
                addressService.selectAccountAddressRenewalAt(createOrderRequestDto.getAccountId());

        boolean inStandardDistance =
                storeService.isInStandardDistance(addressResponseDto, createOrderRequestDto.getStoreId());

        if (!inStandardDistance) {
            throw new OutOfDistanceException();
        }
    }

    private int getDiscountPrice(CreateOrderRequestDto createOrderRequestDto, int totalPrice) {
        UUID issueCouponCode = createOrderRequestDto.getIssueCouponCode();

        if (Objects.isNull(issueCouponCode)) {
            return totalPrice;
        }

        validCoupon(createOrderRequestDto, totalPrice, issueCouponCode);
        return provideCouponService.getDiscountPrice(issueCouponCode, totalPrice);
    }

    private void validCoupon(CreateOrderRequestDto createOrderRequestDto, int totalPrice, UUID issueCouponCode) {
        provideCouponService.validProvideCoupon(issueCouponCode, createOrderRequestDto.getAccountId());
        provideCouponService.validMinimumOrderPrice(issueCouponCode, totalPrice);
        provideCouponService.validExpirationDateTime(issueCouponCode);
    }
}
