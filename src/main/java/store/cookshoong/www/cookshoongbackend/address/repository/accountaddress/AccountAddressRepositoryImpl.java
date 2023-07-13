package store.cookshoong.www.cookshoongbackend.address.repository.accountaddress;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import store.cookshoong.www.cookshoongbackend.address.entity.QAccountAddress;
import store.cookshoong.www.cookshoongbackend.address.entity.QAddress;
import store.cookshoong.www.cookshoongbackend.address.model.response.AccountAddressResponseDto;

/**
 * 회원과 주소 repository.
 *
 * @author jeongjewan
 * @since 2023.07.04
 */
@RequiredArgsConstructor
public class AccountAddressRepositoryImpl implements AccountAddressRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AccountAddressResponseDto> lookupByAccountIdAddressAll(Long accountId) {
        QAccountAddress accountAddress = QAccountAddress.accountAddress;
        QAddress address = QAddress.address;

        return jpaQueryFactory
            .select(Projections.constructor(AccountAddressResponseDto.class,
                accountAddress.address.id, accountAddress.alias, address.mainPlace))
            .from(accountAddress)
            .innerJoin(accountAddress.address, address)
            .where(accountAddress.pk.accountId.eq(accountId))
            .fetch();
    }
}
