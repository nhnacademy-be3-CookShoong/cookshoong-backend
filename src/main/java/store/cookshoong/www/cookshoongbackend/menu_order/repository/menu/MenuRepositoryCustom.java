package store.cookshoong.www.cookshoongbackend.menu_order.repository.menu;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;
import store.cookshoong.www.cookshoongbackend.menu_order.model.response.SelectMenuResponseDto;
import store.cookshoong.www.cookshoongbackend.menu_order.model.response.SelectOptionGroupResponseDto;

/**
 * 메뉴 커스텀 레포지토리 인터페이스.
 *
 * @author papel (윤동현)
 * @since 2023.07.17
 */
@NoRepositoryBean
public interface MenuRepositoryCustom {

    /**
     * 매장 메뉴 조회.
     *
     * @param menuId 메뉴 아이디
     * @return 매장의 메뉴
     */
    Optional<SelectMenuResponseDto> lookupMenu(Long menuId);

    /**
     * 매장 메뉴 리스트 조회.
     *
     * @param storeId 매장 아이디
     * @return 매장의 메뉴 리스트
     */
    List<SelectMenuResponseDto> lookupMenus(Long storeId);
    List<SelectOptionGroupResponseDto> lookupOptionGroupByMenu(Long storeId, Long menuId);
}
