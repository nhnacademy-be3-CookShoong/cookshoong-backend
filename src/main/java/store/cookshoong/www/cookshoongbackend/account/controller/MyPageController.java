package store.cookshoong.www.cookshoongbackend.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.cookshoong.www.cookshoongbackend.order.model.response.LookupAccountOrderInStatusResponseDto;
import store.cookshoong.www.cookshoongbackend.order.service.OrderService;

/**
 * 사용자 마이 페이지 엔드포인트.
 *
 * @author eora21 (김주호)
 * @since 2023.08.13
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my-page/{accountId}")
public class MyPageController {
    private final OrderService orderService;

    /**
     * 사용자 id 반환.
     *
     * @param accountId the account id
     * @return the account id
     */
    @ModelAttribute
    public Long getAccountId(@PathVariable Long accountId) {
        return accountId;
    }

    /**
     * 내 주문 확인.
     *
     * @param accountId the account id
     * @param pageable  the pageable
     * @return the orders
     */
    @GetMapping("/orders")
    public Page<LookupAccountOrderInStatusResponseDto> getOrders(@ModelAttribute Long accountId, Pageable pageable) {
        return orderService.lookupAccountOrders(accountId, pageable);
    }
}
