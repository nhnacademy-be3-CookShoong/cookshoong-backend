package store.cookshoong.www.cookshoongbackend.shop.repository.store;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import store.cookshoong.www.cookshoongbackend.address.entity.QAddress;
import store.cookshoong.www.cookshoongbackend.file.entity.QImage;
import store.cookshoong.www.cookshoongbackend.shop.entity.QBankType;
import store.cookshoong.www.cookshoongbackend.shop.entity.QStore;
import store.cookshoong.www.cookshoongbackend.shop.entity.QStoreCategory;
import store.cookshoong.www.cookshoongbackend.shop.entity.QStoreStatus;
import store.cookshoong.www.cookshoongbackend.shop.entity.QStoresHasCategory;
import store.cookshoong.www.cookshoongbackend.shop.model.response.QSelectAllStoresNotOutedResponseDto;
import store.cookshoong.www.cookshoongbackend.shop.model.response.QSelectAllStoresResponseDto;
import store.cookshoong.www.cookshoongbackend.shop.model.response.QSelectStoreForUserResponseDto;
import store.cookshoong.www.cookshoongbackend.shop.model.response.QSelectStoreResponseTemp;
import store.cookshoong.www.cookshoongbackend.shop.model.response.SelectAllStoresNotOutedResponseDto;
import store.cookshoong.www.cookshoongbackend.shop.model.response.SelectAllStoresResponseDto;
import store.cookshoong.www.cookshoongbackend.shop.model.response.SelectStoreForUserResponseDto;
import store.cookshoong.www.cookshoongbackend.shop.model.response.SelectStoreResponseTemp;

/**
 * 매장 커스텀 레포지토리 구현.
 *
 * @author seungyeon
 * @since 2023.07.05
 */
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<SelectAllStoresResponseDto> lookupStoresPage(Long accountId, Pageable pageable) {
        List<SelectAllStoresResponseDto> responseDtos = lookupStores(accountId, pageable);
        long total = lookupTotal(accountId);
        return new PageImpl<>(responseDtos, pageable, total);
    }

    /**
     * 사업자 회원의 가게 리스트.
     *
     * @param accountId 회원 인덱스 번호
     * @param pageable  페이지 정보
     * @return 각 페이지에 해당하는 매장 리스트
     */
    private List<SelectAllStoresResponseDto> lookupStores(Long accountId, Pageable pageable) {
        QStore store = QStore.store;
        QStoreStatus storeStatus = QStoreStatus.storeStatus;
        QAddress address = QAddress.address;

        return jpaQueryFactory
            .select(new QSelectAllStoresResponseDto(
                store.id, store.name,
                address.mainPlace, address.detailPlace, storeStatus.description))
            .from(store)
            .innerJoin(store.storeStatusCode, storeStatus)
            .innerJoin(store.address, address)
            .where(store.account.id.eq(accountId))
            .orderBy(store.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    /**
     * 회원이 가지고 있는 매장 리스트의 총 개수.
     *
     * @param accountId 회원 아이디
     * @return 회원이 가지고 있는 매장의 총 개수
     */
    private Long lookupTotal(Long accountId) {
        QStore store = QStore.store;
        QStoreStatus storeStatus = QStoreStatus.storeStatus;
        QAddress address = QAddress.address;

        return jpaQueryFactory
            .select(store.count())
            .from(store)
            .innerJoin(store.storeStatusCode, storeStatus)
            .innerJoin(store.address, address)
            .where(store.account.id.eq(accountId))
            .fetchOne();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<SelectStoreResponseTemp> lookupStore(Long accountId, Long storeId) {
        QStore store = QStore.store;
        QAddress address = QAddress.address;
        QBankType bankType = QBankType.bankType;
        QImage image = QImage.image;

        return Optional.ofNullable(jpaQueryFactory
            .select(new QSelectStoreResponseTemp(
                store.businessLicenseNumber,
                store.representativeName,
                store.openingDate,
                store.name, store.phoneNumber,
                address.mainPlace, address.detailPlace, address.latitude, address.longitude, store.defaultEarningRate,
                store.description, bankType.description, store.bankAccountNumber, store.storeImage.savedName))
            .from(store)
            .innerJoin(store.address, address)
            .innerJoin(store.bankTypeCode, bankType)
            .innerJoin(store.storeImage, image)
            .where(store.id.eq(storeId))
            .fetchOne());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<SelectStoreForUserResponseDto> lookupStoreForUser(Long storeId) {
        QStore store = QStore.store;
        QAddress address = QAddress.address;
        QImage image = QImage.image;

        return Optional.ofNullable(jpaQueryFactory
            .select(new QSelectStoreForUserResponseDto(
                store.businessLicenseNumber, store.representativeName, store.openingDate, store.name,
                store.phoneNumber, address.mainPlace, address.detailPlace, store.description,
                store.storeImage.savedName))
            .from(store)
            .innerJoin(store.address, address)
            .innerJoin(store.storeImage, image)
            .where(store.id.eq(storeId))
            .fetchOne());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<SelectAllStoresNotOutedResponseDto> lookupStoreLatLanPage(Pageable pageable) {
        List<SelectAllStoresNotOutedResponseDto> responseDtos = lookupNotOutedStore(pageable);
        Long total = lookupNotOutedStoreTotal();
        return new PageImpl<>(responseDtos, pageable, total);
    }

    private List<SelectAllStoresNotOutedResponseDto> lookupNotOutedStore(Pageable pageable) {
        QStore store = QStore.store;
        QStoreStatus storeStatus = QStoreStatus.storeStatus;
        QStoresHasCategory storesHasCategory = QStoresHasCategory.storesHasCategory;
        QStoreCategory storeCategory = QStoreCategory.storeCategory;
        QAddress address = QAddress.address;
        QImage image = QImage.image;

        return jpaQueryFactory
            .select(new QSelectAllStoresNotOutedResponseDto(
                store.id, store.name, storeStatus.description, address.mainPlace,
                address.detailPlace, address.latitude, address.longitude, storeCategory.categoryCode, store.storeImage.savedName))
            .from(store)
            .innerJoin(store.storeStatusCode, storeStatus)
            .innerJoin(store.address, address)
            .innerJoin(store.storesHasCategories, storesHasCategory)
            .innerJoin(storesHasCategory.categoryCode, storeCategory)
            .innerJoin(store.storeImage, image)
            .where(storeStatus.storeStatusCode.ne("OUTED"))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    private Long lookupNotOutedStoreTotal() {
        QStore store = QStore.store;
        QStoreStatus storeStatus = QStoreStatus.storeStatus;
        QAddress address = QAddress.address;
        QImage image = QImage.image;

        return jpaQueryFactory
            .select(store.count())
            .from(store)
            .innerJoin(store.storeStatusCode, storeStatus)
            .innerJoin(store.address, address)
            .innerJoin(store.storeImage, image)
            .where(storeStatus.storeStatusCode.ne("OUTED"))
            .fetchOne();
    }
}
