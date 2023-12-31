package store.cookshoong.www.cookshoongbackend.point.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import com.querydsl.core.NonUniqueResultException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import store.cookshoong.www.cookshoongbackend.account.entity.Account;
import store.cookshoong.www.cookshoongbackend.common.config.QueryDslConfig;
import store.cookshoong.www.cookshoongbackend.order.entity.Order;
import store.cookshoong.www.cookshoongbackend.point.entity.PointLog;
import store.cookshoong.www.cookshoongbackend.point.entity.PointReasonOrder;
import store.cookshoong.www.cookshoongbackend.point.entity.PointReasonReview;
import store.cookshoong.www.cookshoongbackend.point.entity.PointReasonSignup;
import store.cookshoong.www.cookshoongbackend.point.model.response.PointLogResponseDto;
import store.cookshoong.www.cookshoongbackend.point.model.response.PointResponseDto;
import store.cookshoong.www.cookshoongbackend.review.entity.Review;
import store.cookshoong.www.cookshoongbackend.util.TestEntity;
import store.cookshoong.www.cookshoongbackend.util.TestPersistEntity;

@DataJpaTest
@Import({QueryDslConfig.class, TestPersistEntity.class})
class PointLogRepositoryImplTest {
    @Autowired
    PointLogRepository pointLogRepository;
    @Autowired
    TestEntityManager em;
    @Autowired
    TestEntity te;
    @Autowired
    TestPersistEntity tpe;

    Account account;

    Account nonLogAccount;

    List<PointLog> pointLogs;

    Order order;

    @BeforeEach
    void beforeEach() {
        nonLogAccount = tpe.getLevelOneActiveCustomer();

        account = tpe.getLevelOneActiveCustomer();
        order = te.getOrder(account, tpe.getOpenStore(), te.getOrderStatus("CREATE", "생성"));

        PointReasonSignup pointReasonSignup = em.persist(new PointReasonSignup(account));
        PointReasonOrder pointReasonOrderMinus = em.persist(new PointReasonOrder(order, "주문 시 포인트 사용"));
        PointReasonOrder pointReasonOrderPlus = em.persist(new PointReasonOrder(order, "결제 완료로 인한 포인트 적립"));

        pointLogs = new ArrayList<>(List.of(
            new PointLog(account, pointReasonSignup, 4_000),
            new PointLog(account, pointReasonOrderMinus, -4_000),
            new PointLog(account, pointReasonOrderPlus, 200)
        ));

        pointLogRepository.saveAll(pointLogs);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("현재 포인트 확인")
    void lookupMyPointTest() throws Exception {
        int expectPoint = 0;
        for (PointLog pointLog : pointLogs) {
            expectPoint += pointLog.getPointMovement();
        }

        PointResponseDto pointResponseDto = pointLogRepository.lookupMyPoint(account);

        assertThat(pointResponseDto.getPoint()).isEqualTo(expectPoint);
    }

    @Test
    @DisplayName("포인트 로그 확인")
    void lookupMyPointLog() throws Exception {
        Page<PointLogResponseDto> pointLogResponses =
            pointLogRepository.lookupMyPointLog(account, Pageable.ofSize(20));

        assertThat(pointLogResponses).hasSize(pointLogs.size());

        Iterator<PointLogResponseDto> responseIterator = pointLogResponses.iterator();

        Collections.reverse(pointLogs);

        for (PointLog pointLog :  pointLogs) {
            PointLogResponseDto logResponse = responseIterator.next();

            assertThat(logResponse.getReason()).isEqualTo(pointLog.getPointReason().getExplain());
            assertThat(logResponse.getPointMovement()).isEqualTo(pointLog.getPointMovement());
            assertThat(logResponse.getPointAt()).isEqualTo(pointLog.getPointAt());
        }
    }

    @Test
    @DisplayName("로그가 없을 경우 포인트 0 반환")
    void lookupMyPointZeroIfLogNullTest() throws Exception {
        PointResponseDto pointResponseDto = pointLogRepository.lookupMyPoint(nonLogAccount);
        assertThat(pointResponseDto.getPoint()).isZero();
    }

    @Test
    @DisplayName("사용 포인트 확인")
    void lookupUsePointTest() throws Exception {
        PointLog pointLog = pointLogRepository.lookupUsePoint(order);

        assertThat(pointLog.getPointMovement()).isEqualTo(-4_000);
    }

    @Test
    @DisplayName("사용 포인트 확인 - 해당 주문에 사용한 포인트 없음")
    void lookupUsePointNullTest() throws Exception {
        Order otherOrder = te.getOrder(account, tpe.getOpenStore(), te.getOrderStatus("TEST", "TEST용"));
        PointLog pointLog = pointLogRepository.lookupUsePoint(otherOrder);

        assertThat(pointLog).isNull();
    }

    @Test
    @DisplayName("사용 포인트 확인 - 중복 사용 포인트 기록이 있음")
    void lookupUsePointDuplicateTest() throws Exception {
        PointReasonOrder reasonOrder = em.persist(new PointReasonOrder(order, "주문 시 포인트 사용"));
        PointLog duplicateUsePointLog = new PointLog(account, reasonOrder, -4_000);
        pointLogRepository.save(duplicateUsePointLog);
        em.flush();
        em.clear();

        assertThrowsExactly(NonUniqueResultException.class, () ->
            pointLogRepository.lookupUsePoint(order));
    }
}
