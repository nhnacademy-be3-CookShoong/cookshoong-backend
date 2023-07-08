package store.cookshoong.www.cookshoongbackend.store.repository.store;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import store.cookshoong.www.cookshoongbackend.store.model.response.SelectAllStoresResponseDto;
import store.cookshoong.www.cookshoongbackend.store.model.response.SelectStoreResponseDto;
import store.cookshoong.www.cookshoongbackend.store.model.response.SelectStoreForUserResponseDto;

/**
 * 매장 Custom 레포지토리 인터페이스.
 *
 * @author seungyeon
 * @since 2023.07.05
 */
@NoRepositoryBean
public interface StoreRepositoryCustom {

    /**
     * 사업자 회원의 매장 조회시 pagination 구현.
     *
     * @param accountId 회원 아이디
     * @param pageable  페이지 정보.
     * @return 페이지 별 매장 리스트
     */
    Page<SelectAllStoresResponseDto> lookupStoresPage(Long accountId, Pageable pageable);

    /**
     * 사업자 회원의 storeId에 해당하는 매장 조회 구현.
     *
     * @param accountId 회원 아이디
     * @param storeId   매장 아이디
     * @return 매장에 대한 정보
     */
    Optional<SelectStoreResponseDto> lookupStore(Long accountId, Long storeId);

    /**
     * 일반 회원이 선택한 매장의 정보 조회.
     *
     * @param storeId 해당 매장 id
     * @return 해당 매장 정보
     */
    Optional<SelectStoreForUserResponseDto> lookupStoreForUser(Long storeId);
}