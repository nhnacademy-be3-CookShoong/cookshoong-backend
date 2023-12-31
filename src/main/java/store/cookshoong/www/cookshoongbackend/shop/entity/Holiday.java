package store.cookshoong.www.cookshoongbackend.shop.entity;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 휴무일 엔티티.
 *
 * @author seungyeon
 * @since 2023.07.04
 */
@Getter
@Entity
@Table(name = "holiday")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Holiday {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holiday_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "holiday_start_date", nullable = false)
    private LocalDate holidayStartDate;

    @Column(name = "holiday_end_date", nullable = false)
    private LocalDate holidayEndDate;

    /**
     * 휴업일 생성자.
     *
     * @param store 매장
     * @param holidayStartDate 휴업 시작일
     * @param holidayEndDate 휴업 종료일
     */
    public Holiday(Store store, LocalDate holidayStartDate, LocalDate holidayEndDate) {
        this.store = store;
        this.holidayStartDate = holidayStartDate;
        this.holidayEndDate = holidayEndDate;
    }
}
