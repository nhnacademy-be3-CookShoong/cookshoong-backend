package store.cookshoong.www.cookshoongbackend.account.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.cookshoong.www.cookshoongbackend.shop.model.response.SelectAllBanksForUserResponseDto;
import store.cookshoong.www.cookshoongbackend.shop.model.response.SelectAllCategoriesForUserResponseDto;
import store.cookshoong.www.cookshoongbackend.shop.model.response.SelectAllMerchantsForUserResponseDto;
import store.cookshoong.www.cookshoongbackend.shop.service.BankTypeService;
import store.cookshoong.www.cookshoongbackend.shop.service.MerchantService;
import store.cookshoong.www.cookshoongbackend.shop.service.StoreCategoryService;

/**
 * 사업자 : 등록, 수정시 필요한 selectBox, checkBox 용 api.
 *
 * @author seungyeon
 * @since 2023.07.12
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts/")
public class BusinessSelectController {

    private final BankTypeService bankTypeService;
    private final MerchantService merchantService;
    private final StoreCategoryService storeCategoryService;

    /**
     * 사업자등록 : 은행 리스트 조회.
     *
     * @return 은행 리스트
     */
    @GetMapping("/banks")
    public ResponseEntity<List<SelectAllBanksForUserResponseDto>> getBanksForUser() {
        return ResponseEntity
            .ok(bankTypeService.selectBanksForUser());
    }

    /**
     * 사업자 등록 : 가맹점 리스트 조회.
     *
     * @return 가맹점 리스트
     */
    @GetMapping("/merchants")
    public ResponseEntity<List<SelectAllMerchantsForUserResponseDto>> getMerchantsForUser() {
        return ResponseEntity
            .ok(merchantService.selectAllMerchantsForUser());
    }

    /**
     * 사업자 등록 : 카테고리 리스트 조회.
     *
     * @return 매장 키테고리 리스트
     */
    @GetMapping("/categories")
    public ResponseEntity<List<SelectAllCategoriesForUserResponseDto>> getCategoriesForUser() {
        return ResponseEntity
            .ok(storeCategoryService.selectAllCategoriesForUser());
    }
}