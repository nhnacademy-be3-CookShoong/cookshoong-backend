package store.cookshoong.www.cookshoongbackend.store.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import store.cookshoong.www.cookshoongbackend.store.entity.QHoliday;
import store.cookshoong.www.cookshoongbackend.store.entity.QStore;
import store.cookshoong.www.cookshoongbackend.store.model.response.HolidayListResponseDto;

/**
 * 휴업일 커스텀 레포지토리 구현
 *
 * @author papel
 * @since 2023.07.07
 */
@RequiredArgsConstructor
public class HolidayRepositoryImpl implements HolidayRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;


    /**
     * {@inheritDoc}
     */
    @Override
    public Page<HolidayListResponseDto> lookupHolidayPage(Long storeId, Pageable pageable) {
        return null;
    }

    /**
     * 매장의 휴업일 리스트.
     *
     * @param storeId 매장 아이디
     * @param pageable  페이지 정보
     * @return 각 페이지에 해당하는 휴업일 리스트
     */
    private List<HolidayListResponseDto> getHolidays(Long storeId, Pageable pageable) {
        QHoliday holiday = QHoliday.holiday;
        QStore store = QStore.store;

        return jpaQueryFactory
            .select(Projections.constructor(HolidayListResponseDto.class,
                holiday.id, store.id, holiday.holidayDate))
            .from(holiday)
            .innerJoin(holiday.store, store)
            .where(holiday.store.id.eq(storeId))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }
}
