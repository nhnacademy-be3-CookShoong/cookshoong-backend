package store.cookshoong.www.cookshoongbackend.menu_order.repository.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import store.cookshoong.www.cookshoongbackend.menu_order.entity.menugroup.MenuHasMenuGroup;

/**
 * 메뉴 - 메뉴그룹 레포지토리.
 *
 * @author papel (윤동현)
 * @since 2023.07.11
 */
public interface MenuHasMenuGroupRepository extends JpaRepository<MenuHasMenuGroup, MenuHasMenuGroup.Pk> {
}
