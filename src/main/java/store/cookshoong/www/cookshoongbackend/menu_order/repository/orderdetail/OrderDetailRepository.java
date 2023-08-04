package store.cookshoong.www.cookshoongbackend.menu_order.repository.orderdetail;

import org.springframework.data.jpa.repository.JpaRepository;
import store.cookshoong.www.cookshoongbackend.menu_order.entity.orderdetail.OrderDetail;

/**
 * 주문 상세 레포지토리.
 *
 * @author papel (윤동현)
 * @since 2023.07.11
 */
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}