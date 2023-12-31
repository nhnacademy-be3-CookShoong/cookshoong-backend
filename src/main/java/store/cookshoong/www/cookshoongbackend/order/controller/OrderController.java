package store.cookshoong.www.cookshoongbackend.order.controller;

import static store.cookshoong.www.cookshoongbackend.cart.utils.CartConstant.CART;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.cookshoong.www.cookshoongbackend.address.model.response.AddressResponseDto;
import store.cookshoong.www.cookshoongbackend.address.service.AddressService;
import store.cookshoong.www.cookshoongbackend.cart.redis.model.vo.CartRedisDto;
import store.cookshoong.www.cookshoongbackend.cart.redis.service.CartRedisService;
import store.cookshoong.www.cookshoongbackend.coupon.service.ProvideCouponService;
import store.cookshoong.www.cookshoongbackend.order.entity.OrderStatus;
import store.cookshoong.www.cookshoongbackend.order.exception.OrderRequestValidationException;
import store.cookshoong.www.cookshoongbackend.order.exception.OutOfDistanceException;
import store.cookshoong.www.cookshoongbackend.order.model.request.CreateOrderRequestDto;
import store.cookshoong.www.cookshoongbackend.order.model.request.PatchOrderRequestDto;
import store.cookshoong.www.cookshoongbackend.order.model.response.CreateOrderResponseDto;
import store.cookshoong.www.cookshoongbackend.order.model.response.LookupOrderInStatusResponseDto;
import store.cookshoong.www.cookshoongbackend.order.model.response.SelectOrderPossibleResponseDto;
import store.cookshoong.www.cookshoongbackend.order.service.OrderService;
import store.cookshoong.www.cookshoongbackend.point.service.PointService;
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
    private final PointService pointService;

    /**
     * 주문 생성 엔드포인트.
     *
     * @param createOrderRequestDto the create order request dto
     * @param bindingResult         the binding result
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
            CART + createOrderRequestDto.getAccountId());

        int totalPrice = getTotalPrice(cartItems);

        int deliveryCost = storeService.selectStoreDeliveryCost(createOrderRequestDto.getStoreId());
        createOrderRequestDto.setDeliveryCost(deliveryCost);

        int discountPrice = getDiscountPrice(createOrderRequestDto, totalPrice, deliveryCost);

        orderService.createOrder(createOrderRequestDto, cartItems);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CreateOrderResponseDto(discountPrice));
    }

    private void validOrderDistance(CreateOrderRequestDto createOrderRequestDto) {
        boolean inStandardDistance =
            isInStandardDistance(createOrderRequestDto.getAccountId(), createOrderRequestDto.getStoreId());

        if (!inStandardDistance) {
            throw new OutOfDistanceException();
        }
    }

    private boolean isInStandardDistance(Long accountId, Long storeId) {
        AddressResponseDto addressResponseDto =
            addressService.selectAccountAddressRenewalAt(accountId);

        return storeService.isInStandardDistance(addressResponseDto, storeId);
    }

    private int getTotalPrice(Long accountId) {
        List<CartRedisDto> cartItems = cartRedisService.selectCartMenuAll(
            CART + accountId);

        return cartRedisService.getTotalPrice(cartItems);
    }

    private int getTotalPrice(List<CartRedisDto> cartItems) {
        return cartRedisService.getTotalPrice(cartItems);
    }

    private int getDiscountPrice(CreateOrderRequestDto createOrderRequestDto, int totalPrice, int deliveryCost) {
        int couponDiscountPrice = getCouponDiscountPrice(createOrderRequestDto, totalPrice);
        int beforePointDiscountPrice = couponDiscountPrice + deliveryCost;
        int pointDiscount = getPointDiscount(createOrderRequestDto, beforePointDiscountPrice);
        return couponDiscountPrice + deliveryCost - pointDiscount;
    }

    private int getCouponDiscountPrice(CreateOrderRequestDto createOrderRequestDto, int totalPrice) {
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

    private int getPointDiscount(CreateOrderRequestDto createOrderRequestDto, int beforePointDiscountPrice) {
        int pointAmount = createOrderRequestDto.getPointAmount();
        int validPoint =
            pointService.getValidPoint(createOrderRequestDto.getAccountId(), pointAmount, beforePointDiscountPrice);

        createOrderRequestDto.setPointAmount(validPoint);

        return validPoint;
    }

    /**
     * 상태 변경 엔드포인트.
     *
     * @param patchOrderRequestDto the cancel order request dto
     * @param bindingResult        the binding result
     * @return the response entity
     */
    @PatchMapping("/status")
    public ResponseEntity<Void> patchOrderStatus(@RequestBody @Valid PatchOrderRequestDto patchOrderRequestDto,
                                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new OrderRequestValidationException(bindingResult);
        }

        OrderStatus.StatusCode statusCode = patchOrderRequestDto.getStatusCode();
        orderService.changeStatus(patchOrderRequestDto.getOrderCode(), statusCode);

        return ResponseEntity.noContent()
            .build();
    }

    /**
     * 진행중인 주문을 확인하는 엔드포인트.
     *
     * @param storeId the store id
     * @return the order in progress
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<List<LookupOrderInStatusResponseDto>> getOrderInProgress(@PathVariable Long storeId) {
        return ResponseEntity.ok(orderService.lookupOrderInProgress(storeId));
    }

    /**
     * 완료된 주문 페이징 목록을 확인하는 엔드포인트.
     *
     * @param storeId  the store id
     * @param pageable the pageable
     * @return the order in complete
     */
    @GetMapping("/{storeId}/complete")
    public ResponseEntity<Page<LookupOrderInStatusResponseDto>> getOrderInComplete(@PathVariable Long storeId,
                                                                                   Pageable pageable) {
        return ResponseEntity.ok(orderService.lookupOrderInComplete(storeId, pageable));
    }

    /**
     * 주문 가능 여부를 반환하는 엔드포인트.
     *
     * @param storeId   the store id
     * @param accountId the account id
     * @return the order possible
     */
    @GetMapping("/{storeId}/possible/{accountId}")
    public ResponseEntity<SelectOrderPossibleResponseDto> getOrderPossible(@PathVariable Long storeId,
                                                                           @PathVariable Long accountId) {
        boolean inStandardDistance = isInStandardDistance(accountId, storeId);
        int totalPrice = getTotalPrice(accountId);
        return ResponseEntity.ok(orderService.selectOrderPossible(inStandardDistance, storeId, totalPrice));
    }
}
