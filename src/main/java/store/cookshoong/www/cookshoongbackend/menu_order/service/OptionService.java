package store.cookshoong.www.cookshoongbackend.menu_order.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.cookshoong.www.cookshoongbackend.menu_order.entity.option.Option;
import store.cookshoong.www.cookshoongbackend.menu_order.entity.optiongroup.OptionGroup;
import store.cookshoong.www.cookshoongbackend.menu_order.exception.option.OptionGroupNotFoundException;
import store.cookshoong.www.cookshoongbackend.menu_order.model.request.CreateOptionRequestDto;
import store.cookshoong.www.cookshoongbackend.menu_order.model.response.SelectOptionResponseDto;
import store.cookshoong.www.cookshoongbackend.menu_order.repository.option.OptionGroupRepository;
import store.cookshoong.www.cookshoongbackend.menu_order.repository.option.OptionRepository;

/**
 * 옵션 관리 서비스.
 *
 * @author papel
 * @since 2023.07.11
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OptionService {
    private final OptionRepository optionRepository;
    private final OptionGroupRepository optionGroupRepository;

    /**
     * 옵션 등록 서비스.
     *
     * @param optionGroupId          옵션 그룹 아이디
     * @param createOptionRequestDto 옵션 등록 Dto
     */
    public void createOption(Long optionGroupId, CreateOptionRequestDto createOptionRequestDto) {
        OptionGroup optionGroup = optionGroupRepository.findById(optionGroupId)
            .orElseThrow(OptionGroupNotFoundException::new);
        Option option =
            new Option(
                optionGroup,
                createOptionRequestDto.getName(),
                createOptionRequestDto.getPrice(),
                0,
                false
                );
        optionRepository.save(option);
    }

    /**
     * 옵션 조회 서비스.
     *
     * @param storeId 매장 아이디
     * @return 매장의 옵션 리스트
     */
    public List<SelectOptionResponseDto> selectOptions(Long storeId) {
        return optionRepository.lookupOptions(storeId);
    }
}
