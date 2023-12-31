package store.cookshoong.www.cookshoongbackend.coupon.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import store.cookshoong.www.cookshoongbackend.coupon.entity.CouponTypeCash;
import store.cookshoong.www.cookshoongbackend.coupon.entity.CouponTypePercent;
import store.cookshoong.www.cookshoongbackend.coupon.exception.CouponUsageNotFoundException;
import store.cookshoong.www.cookshoongbackend.coupon.model.request.CreateCashCouponPolicyRequestDto;
import store.cookshoong.www.cookshoongbackend.coupon.model.request.CreatePercentCouponPolicyRequestDto;
import store.cookshoong.www.cookshoongbackend.coupon.model.response.SelectPolicyResponseDto;
import store.cookshoong.www.cookshoongbackend.coupon.model.response.SelectProvableCouponPolicyResponseDto;
import store.cookshoong.www.cookshoongbackend.coupon.service.CouponPolicyService;
import store.cookshoong.www.cookshoongbackend.util.TestEntity;
import store.cookshoong.www.cookshoongbackend.util.TestEntityAspect;

@AutoConfigureRestDocs
@Import(TestEntity.class)
@WebMvcTest(CouponPolicyController.class)
class CouponPolicyControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CouponPolicyService couponPolicyService;

    @MockBean
    TestEntityAspect testEntityAspect;

    TestEntity te = new TestEntity();

    CreateCashCouponPolicyRequestDto cashRequestDto;

    CreatePercentCouponPolicyRequestDto percentRequestDto;

    List<SelectPolicyResponseDto> policies;

    AtomicLong atomicLong = new AtomicLong();

    @BeforeEach
    void beforeEach() {
        CouponTypeCash couponTypeCash = te.getCouponTypeCash_1000_10000();
        CouponTypePercent couponTypePercent = te.getCouponTypePercent_3_1000_10000();
        SelectPolicyResponseDto cashPolicy = new SelectPolicyResponseDto(atomicLong.getAndIncrement(),
            couponTypeCash, "금액 쿠폰", "현금처럼 쓰입니다.",
            1, 1L, 10L);
        SelectPolicyResponseDto percentPolicy = new SelectPolicyResponseDto(atomicLong.getAndIncrement(),
            couponTypePercent, "퍼센트 쿠폰", "퍼센트만큼 차감합니다.",
            1, 1L, 10L);

        policies = List.of(cashPolicy, percentPolicy);

        cashRequestDto = te.createUsingDeclared(CreateCashCouponPolicyRequestDto.class);
        ReflectionTestUtils.setField(cashRequestDto, "name", "금액 쿠폰");
        ReflectionTestUtils.setField(cashRequestDto, "description", "설정된 금액만큼 가격을 차감합니다.");
        ReflectionTestUtils.setField(cashRequestDto, "usagePeriod", 30);
        ReflectionTestUtils.setField(cashRequestDto, "discountAmount", 5_000);
        ReflectionTestUtils.setField(cashRequestDto, "minimumOrderPrice", 10_000);

        percentRequestDto = te.createUsingDeclared(CreatePercentCouponPolicyRequestDto.class);
        ReflectionTestUtils.setField(percentRequestDto, "name", "퍼센트 쿠폰");
        ReflectionTestUtils.setField(percentRequestDto, "description", "설정된 퍼센트만큼 가격을 차감합니다.");
        ReflectionTestUtils.setField(percentRequestDto, "usagePeriod", 30);
        ReflectionTestUtils.setField(percentRequestDto, "rate", 10);
        ReflectionTestUtils.setField(percentRequestDto, "minimumOrderPrice", 10_000);
        ReflectionTestUtils.setField(percentRequestDto, "maximumDiscountAmount", 30_000);
    }

    @Test
    @DisplayName("매장 쿠폰 정책 조회")
    void getStorePolicyTest() throws Exception {
        when(couponPolicyService.selectStorePolicy(any(Long.class), any(Pageable.class)))
            .thenAnswer(invocation -> new PageImpl<>(policies, invocation.getArgument(1), policies.size()));

        RequestBuilder request = RestDocumentationRequestBuilders
            .get("/api/coupon/policies/stores/{storeId}", Long.MIN_VALUE)
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("금액 쿠폰"))
            .andExpect(jsonPath("$.content[0].description").value("현금처럼 쓰입니다."))
            .andExpect(jsonPath("$.content[0].usagePeriod").value(1))
            .andExpect(jsonPath("$.content[0].unclaimedCouponCount").value(1))
            .andExpect(jsonPath("$.content[0].issueCouponCount").value(10))
            .andExpect(jsonPath("$.content[0].couponTypeResponse.discountAmount").value(1_000))
            .andExpect(jsonPath("$.content[0].couponTypeResponse.minimumOrderPrice").value(10_000))
            .andExpect(jsonPath("$.content[1].name").value("퍼센트 쿠폰"))
            .andExpect(jsonPath("$.content[1].description").value("퍼센트만큼 차감합니다."))
            .andExpect(jsonPath("$.content[1].usagePeriod").value(1))
            .andExpect(jsonPath("$.content[1].unclaimedCouponCount").value(1))
            .andExpect(jsonPath("$.content[1].issueCouponCount").value(10))
            .andExpect(jsonPath("$.content[1].couponTypeResponse.rate").value(3))
            .andExpect(jsonPath("$.content[1].couponTypeResponse.maximumDiscountAmount").value(1_000))
            .andExpect(jsonPath("$.content[1].couponTypeResponse.minimumOrderPrice").value(10_000))
            .andDo(MockMvcRestDocumentationWrapper.document("getStorePolicy",
                ResourceSnippetParameters.builder()
                    .pathParameters(parameterWithName("storeId").description("매장 id"))
                    .requestParameters(
                        parameterWithName("page").optional().description("페이지 번호"),
                        parameterWithName("size").optional().description("한 페이지에 몇 개의 데이터를 불러올 것인지"),
                        parameterWithName("sort").optional().description("페이징 정렬조건"))
                    .responseSchema(Schema.schema("getStorePolicy.Response")),
                responseFields(
                    fieldWithPath("content[].id").description("정책 id"),
                    fieldWithPath("content[].couponTypeResponse.type").description("쿠폰 타입 설명"),
                    fieldWithPath("content[].couponTypeResponse.discountAmount").optional().description("할인금"),
                    fieldWithPath("content[].couponTypeResponse.rate").optional().description("할인율"),
                    fieldWithPath("content[].couponTypeResponse.minimumOrderPrice").optional().description("최소주문금액"),
                    fieldWithPath("content[].couponTypeResponse.maximumDiscountAmount").optional().description("최대할인금액"),
                    fieldWithPath("content[].name").description("쿠폰명"),
                    fieldWithPath("content[].description").description("쿠폰 설명"),
                    fieldWithPath("content[].usagePeriod").description("발급 후 사용기"),
                    fieldWithPath("content[].unclaimedCouponCount").description("남은 쿠폰 개수"),
                    fieldWithPath("content[].issueCouponCount").description("전체 발행 쿠폰 개수"),
                    fieldWithPath("pageable.sort.empty").description("정렬 데이터 공백 여부"),
                    fieldWithPath("pageable.sort.sorted").description("정렬 여부"),
                    fieldWithPath("pageable.sort.unsorted").description("비정렬 여부"),
                    fieldWithPath("pageable.offset").description("데이터 순번"),
                    fieldWithPath("pageable.pageNumber").description("페이지 번호"),
                    fieldWithPath("pageable.pageSize").description("한 페이지당 조회할 데이터 개수"),
                    fieldWithPath("pageable.paged").description("페이징 정보 포함 여부"),
                    fieldWithPath("pageable.unpaged").description("페이징 정보 미포함 여부"),
                    fieldWithPath("last").description("마지막 페이지 여부"),
                    fieldWithPath("totalPages").description("전체 페이지 개수"),
                    fieldWithPath("totalElements").description("총 데이터 개수"),
                    fieldWithPath("first").description("첫 페이지 여부"),
                    fieldWithPath("size").description("한 페이지당 조회할 데이터 개수"),
                    fieldWithPath("number").description("현재 페이지 번호"),
                    fieldWithPath("sort.empty").description("정렬 데이터 공백 여부"),
                    fieldWithPath("sort.sorted").description("정렬 여부"),
                    fieldWithPath("sort.unsorted").description("비정렬 여부"),
                    fieldWithPath("numberOfElements").description("요청 페이지에서 조회된 데이터 개수"),
                    fieldWithPath("empty").description("데이터 미존재 여부")
                )
            ));
    }

    @Test
    @DisplayName("가맹점 쿠폰 정책 조회")
    void getMerchantPolicyTest() throws Exception {
        when(couponPolicyService.selectMerchantPolicy(any(Long.class), any(Pageable.class)))
            .thenAnswer(invocation -> new PageImpl<>(policies, invocation.getArgument(1), policies.size()));

        RequestBuilder request = RestDocumentationRequestBuilders
            .get("/api/coupon/policies/merchants/{merchantId}", Long.MIN_VALUE)
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("금액 쿠폰"))
            .andExpect(jsonPath("$.content[0].description").value("현금처럼 쓰입니다."))
            .andExpect(jsonPath("$.content[0].usagePeriod").value(1))
            .andExpect(jsonPath("$.content[0].unclaimedCouponCount").value(1))
            .andExpect(jsonPath("$.content[0].issueCouponCount").value(10))
            .andExpect(jsonPath("$.content[0].couponTypeResponse.discountAmount").value(1_000))
            .andExpect(jsonPath("$.content[0].couponTypeResponse.minimumOrderPrice").value(10_000))
            .andExpect(jsonPath("$.content[1].name").value("퍼센트 쿠폰"))
            .andExpect(jsonPath("$.content[1].description").value("퍼센트만큼 차감합니다."))
            .andExpect(jsonPath("$.content[1].usagePeriod").value(1))
            .andExpect(jsonPath("$.content[1].unclaimedCouponCount").value(1))
            .andExpect(jsonPath("$.content[1].issueCouponCount").value(10))
            .andExpect(jsonPath("$.content[1].couponTypeResponse.rate").value(3))
            .andExpect(jsonPath("$.content[1].couponTypeResponse.maximumDiscountAmount").value(1_000))
            .andExpect(jsonPath("$.content[1].couponTypeResponse.minimumOrderPrice").value(10_000))
            .andDo(MockMvcRestDocumentationWrapper.document("getMerchantPolicy",
                ResourceSnippetParameters.builder()
                    .pathParameters(parameterWithName("merchantId").description("가맹점 id"))
                    .requestParameters(
                        parameterWithName("page").optional().description("페이지 번호"),
                        parameterWithName("size").optional().description("한 페이지에 몇 개의 데이터를 불러올 것인지"),
                        parameterWithName("sort").optional().description("페이징 정렬조건"))
                    .responseSchema(Schema.schema("getMerchantPolicy.Response")),
                responseFields(
                    fieldWithPath("content[].id").description("정책 id"),
                    fieldWithPath("content[].couponTypeResponse.type").description("쿠폰 타입 설명"),
                    fieldWithPath("content[].couponTypeResponse.discountAmount").optional().description("할인금"),
                    fieldWithPath("content[].couponTypeResponse.rate").optional().description("할인율"),
                    fieldWithPath("content[].couponTypeResponse.minimumOrderPrice").optional().description("최소주문금액"),
                    fieldWithPath("content[].couponTypeResponse.maximumDiscountAmount").optional().description("최대할인금액"),
                    fieldWithPath("content[].name").description("쿠폰명"),
                    fieldWithPath("content[].description").description("쿠폰 설명"),
                    fieldWithPath("content[].usagePeriod").description("발급 후 사용기"),
                    fieldWithPath("content[].unclaimedCouponCount").description("남은 쿠폰 개수"),
                    fieldWithPath("content[].issueCouponCount").description("전체 발행 쿠폰 개수"),
                    fieldWithPath("pageable.sort.empty").description("정렬 데이터 공백 여부"),
                    fieldWithPath("pageable.sort.sorted").description("정렬 여부"),
                    fieldWithPath("pageable.sort.unsorted").description("비정렬 여부"),
                    fieldWithPath("pageable.offset").description("데이터 순번"),
                    fieldWithPath("pageable.pageNumber").description("페이지 번호"),
                    fieldWithPath("pageable.pageSize").description("한 페이지당 조회할 데이터 개수"),
                    fieldWithPath("pageable.paged").description("페이징 정보 포함 여부"),
                    fieldWithPath("pageable.unpaged").description("페이징 정보 미포함 여부"),
                    fieldWithPath("last").description("마지막 페이지 여부"),
                    fieldWithPath("totalPages").description("전체 페이지 개수"),
                    fieldWithPath("totalElements").description("총 데이터 개수"),
                    fieldWithPath("first").description("첫 페이지 여부"),
                    fieldWithPath("size").description("한 페이지당 조회할 데이터 개수"),
                    fieldWithPath("number").description("현재 페이지 번호"),
                    fieldWithPath("sort.empty").description("정렬 데이터 공백 여부"),
                    fieldWithPath("sort.sorted").description("정렬 여부"),
                    fieldWithPath("sort.unsorted").description("비정렬 여부"),
                    fieldWithPath("numberOfElements").description("요청 페이지에서 조회된 데이터 개수"),
                    fieldWithPath("empty").description("데이터 미존재 여부")
                )
            ));
    }

    @Test
    @DisplayName("사용처 전체 쿠폰 정책 조회")
    void getUsageAllPolicyTest() throws Exception {
        when(couponPolicyService.selectUsageAllPolicy(any(Pageable.class)))
            .thenAnswer(invocation -> new PageImpl<>(policies, invocation.getArgument(0), policies.size()));

        RequestBuilder request = RestDocumentationRequestBuilders
            .get("/api/coupon/policies/all")
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("금액 쿠폰"))
            .andExpect(jsonPath("$.content[0].description").value("현금처럼 쓰입니다."))
            .andExpect(jsonPath("$.content[0].usagePeriod").value(1))
            .andExpect(jsonPath("$.content[0].unclaimedCouponCount").value(1))
            .andExpect(jsonPath("$.content[0].issueCouponCount").value(10))
            .andExpect(jsonPath("$.content[0].couponTypeResponse.discountAmount").value(1_000))
            .andExpect(jsonPath("$.content[0].couponTypeResponse.minimumOrderPrice").value(10_000))
            .andExpect(jsonPath("$.content[1].name").value("퍼센트 쿠폰"))
            .andExpect(jsonPath("$.content[1].description").value("퍼센트만큼 차감합니다."))
            .andExpect(jsonPath("$.content[1].usagePeriod").value(1))
            .andExpect(jsonPath("$.content[1].unclaimedCouponCount").value(1))
            .andExpect(jsonPath("$.content[1].issueCouponCount").value(10))
            .andExpect(jsonPath("$.content[1].couponTypeResponse.rate").value(3))
            .andExpect(jsonPath("$.content[1].couponTypeResponse.maximumDiscountAmount").value(1_000))
            .andExpect(jsonPath("$.content[1].couponTypeResponse.minimumOrderPrice").value(10_000))
            .andDo(MockMvcRestDocumentationWrapper.document("getUsageAllPolicy",
                ResourceSnippetParameters.builder()
                    .requestParameters(
                        parameterWithName("page").optional().description("페이지 번호"),
                        parameterWithName("size").optional().description("한 페이지에 몇 개의 데이터를 불러올 것인지"),
                        parameterWithName("sort").optional().description("페이징 정렬조건"))
                    .responseSchema(Schema.schema("getUsageAllPolicy.Response")),
                responseFields(
                    fieldWithPath("content[].id").description("정책 id"),
                    fieldWithPath("content[].couponTypeResponse.type").description("쿠폰 타입 설명"),
                    fieldWithPath("content[].couponTypeResponse.discountAmount").optional().description("할인금"),
                    fieldWithPath("content[].couponTypeResponse.rate").optional().description("할인율"),
                    fieldWithPath("content[].couponTypeResponse.minimumOrderPrice").optional().description("최소주문금액"),
                    fieldWithPath("content[].couponTypeResponse.maximumDiscountAmount").optional().description("최대할인금액"),
                    fieldWithPath("content[].name").description("쿠폰명"),
                    fieldWithPath("content[].description").description("쿠폰 설명"),
                    fieldWithPath("content[].usagePeriod").description("발급 후 사용기간"),
                    fieldWithPath("content[].unclaimedCouponCount").description("남은 쿠폰 개수"),
                    fieldWithPath("content[].issueCouponCount").description("전체 발행 쿠폰 개수"),
                    fieldWithPath("pageable.sort.empty").description("정렬 데이터 공백 여부"),
                    fieldWithPath("pageable.sort.sorted").description("정렬 여부"),
                    fieldWithPath("pageable.sort.unsorted").description("비정렬 여부"),
                    fieldWithPath("pageable.offset").description("데이터 순번"),
                    fieldWithPath("pageable.pageNumber").description("페이지 번호"),
                    fieldWithPath("pageable.pageSize").description("한 페이지당 조회할 데이터 개수"),
                    fieldWithPath("pageable.paged").description("페이징 정보 포함 여부"),
                    fieldWithPath("pageable.unpaged").description("페이징 정보 미포함 여부"),
                    fieldWithPath("last").description("마지막 페이지 여부"),
                    fieldWithPath("totalPages").description("전체 페이지 개수"),
                    fieldWithPath("totalElements").description("총 데이터 개수"),
                    fieldWithPath("first").description("첫 페이지 여부"),
                    fieldWithPath("size").description("한 페이지당 조회할 데이터 개수"),
                    fieldWithPath("number").description("현재 페이지 번호"),
                    fieldWithPath("sort.empty").description("정렬 데이터 공백 여부"),
                    fieldWithPath("sort.sorted").description("정렬 여부"),
                    fieldWithPath("sort.unsorted").description("비정렬 여부"),
                    fieldWithPath("numberOfElements").description("요청 페이지에서 조회된 데이터 개수"),
                    fieldWithPath("empty").description("데이터 미존재 여부")
                )
            ));
    }

    @Test
    @DisplayName("매장 금액 쿠폰 정책 생성 성공")
    void postStoreCashCouponPolicySuccessTest() throws Exception {
        RequestBuilder request = RestDocumentationRequestBuilders
            .post("/api/coupon/policies/stores/{storeId}/cash", Long.MIN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cashRequestDto));

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isCreated())
            .andDo(MockMvcRestDocumentationWrapper.document("createStoreCashCouponPolicy",
                ResourceSnippetParameters.builder()
                    .pathParameters(parameterWithName("storeId").description("매장 id"))
                    .requestSchema(Schema.schema("createStoreCashCouponPolicy.Request")),
                requestFields(
                    fieldWithPath("name").description("쿠폰명"),
                    fieldWithPath("description").description("쿠폰 설명"),
                    fieldWithPath("usagePeriod").description("만료일"),
                    fieldWithPath("discountAmount").description("할인금액"),
                    fieldWithPath("minimumOrderPrice").description("최소주문금액"))
            ));

        verify(couponPolicyService, Mockito.times(1))
            .createStoreCashCouponPolicy(any(Long.class), any(CreateCashCouponPolicyRequestDto.class));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvSource(value = {
        "쿠폰명:name", "설명:description", "만료일:usagePeriod", "할인금액:discountAmount", "최소주문금액:minimumOrderPrice"
    }, delimiter = ':')
    @DisplayName("매장 금액 쿠폰 정책 생성 실패 - 필수 필드 없음")
    void postStoreCashCouponPolicyNonFieldFailTest(String displayName, String fieldName) throws Exception {
        ReflectionTestUtils.setField(cashRequestDto, fieldName, null);
        RequestBuilder request = RestDocumentationRequestBuilders
            .post("/api/coupon/policies/stores/{storeId}/cash", Long.MIN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cashRequestDto));

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(couponPolicyService, Mockito.never())
            .createStoreCashCouponPolicy(any(Long.class), any(CreateCashCouponPolicyRequestDto.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1_000, 0, 100_000})
    @DisplayName("매장 금액 쿠폰 정책 생성 실패 - 할인금액 범위 초과")
    void postStoreCashCouponPolicyDiscountAmountOutOfRangeFailTest(Integer discountAmount) throws Exception {
        ReflectionTestUtils.setField(cashRequestDto, "discountAmount", discountAmount);

        RequestBuilder request = RestDocumentationRequestBuilders
            .post("/api/coupon/policies/stores/{storeId}/cash", Long.MIN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cashRequestDto));

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(couponPolicyService, Mockito.never())
            .createStoreCashCouponPolicy(any(Long.class), any(CreateCashCouponPolicyRequestDto.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1_000, 100_000})
    @DisplayName("매장 금액 쿠폰 정책 생성 실패 - 최소주문금액 범위 초과")
    void postStoreCashCouponPolicyminimumOrderPriceOutOfRangeFailTest(Integer minimumOrderPrice) throws Exception {
        ReflectionTestUtils.setField(cashRequestDto, "minimumOrderPrice", minimumOrderPrice);

        RequestBuilder request = RestDocumentationRequestBuilders
            .post("/api/coupon/policies/stores/{storeId}/cash", Long.MIN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cashRequestDto));

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(couponPolicyService, Mockito.never())
            .createStoreCashCouponPolicy(any(Long.class), any(CreateCashCouponPolicyRequestDto.class));
    }

    @Test
    @DisplayName("매장 퍼센트 쿠폰 정책 생성 성공")
    void postStorePercentCouponPolicySuccessTest() throws Exception {
        RequestBuilder request = RestDocumentationRequestBuilders
            .post("/api/coupon/policies/stores/{storeId}/percent", Long.MIN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(percentRequestDto));

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isCreated())
            .andDo(MockMvcRestDocumentationWrapper.document("createStorePercentCouponPolicy",
                ResourceSnippetParameters.builder()
                    .pathParameters(parameterWithName("storeId").description("매장 id"))
                    .requestSchema(Schema.schema("createStorePercentCouponPolicy.Request")),
                requestFields(
                    fieldWithPath("name").description("쿠폰명"),
                    fieldWithPath("description").description("쿠폰 설명"),
                    fieldWithPath("usagePeriod").description("만료일"),
                    fieldWithPath("rate").description("할인율"),
                    fieldWithPath("minimumOrderPrice").description("최소주문금액"),
                    fieldWithPath("maximumDiscountAmount").description("최대할인금액"))
            ));

        verify(couponPolicyService, Mockito.times(1))
            .createStorePercentCouponPolicy(any(Long.class), any(CreatePercentCouponPolicyRequestDto.class));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @CsvSource(value = {
        "쿠폰명:name", "설명:description", "만료일:usagePeriod", "할인율:rate", "최소주문금액:minimumOrderPrice",
        "최대할인금액:maximumDiscountAmount"}, delimiter = ':')
    @DisplayName("매장 퍼센트 쿠폰 정책 생성 실패 - 필수 필드 없음")
    void postStorePercentCouponPolicyNonFieldFailTest(String displayName, String fieldName) throws Exception {
        ReflectionTestUtils.setField(percentRequestDto, fieldName, null);
        RequestBuilder request = RestDocumentationRequestBuilders
            .post("/api/coupon/policies/stores/{storeId}/percent", Long.MIN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(percentRequestDto));

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(couponPolicyService, Mockito.never())
            .createStorePercentCouponPolicy(any(Long.class), any(CreatePercentCouponPolicyRequestDto.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 100})
    @DisplayName("매장 퍼센트 쿠폰 정책 생성 실패 - 할인율 범위 초과")
    void postStorePercentCouponPolicyRateOutOfRangeFailTest(int rate) throws Exception {
        ReflectionTestUtils.setField(percentRequestDto, "rate", rate);
        RequestBuilder request = RestDocumentationRequestBuilders
            .post("/api/coupon/policies/stores/{storeId}/percent", Long.MIN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(percentRequestDto));

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(couponPolicyService, Mockito.never())
            .createStorePercentCouponPolicy(any(Long.class), any(CreatePercentCouponPolicyRequestDto.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {-10_000, 30_000})
    @DisplayName("매장 퍼센트 쿠폰 정책 생성 실패 - 최소주문금액 범위 초과")
    void postStorePercentCouponPolicyMinimumOrderPriceOutOfRangeFailTest(int minimumOrderPrice) throws Exception {
        ReflectionTestUtils.setField(percentRequestDto, "minimumOrderPrice", minimumOrderPrice);
        RequestBuilder request = RestDocumentationRequestBuilders
            .post("/api/coupon/policies/stores/{storeId}/percent", Long.MIN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(percentRequestDto));

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(couponPolicyService, Mockito.never())
            .createStorePercentCouponPolicy(any(Long.class), any(CreatePercentCouponPolicyRequestDto.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 60_000})
    @DisplayName("매장 퍼센트 쿠폰 정책 생성 실패 - 최대할인금액 범위 초과")
    void postStorePercentCouponPolicyMaximumDiscountAmountOutOfRangeFailTest(int maximumDiscountAmount) throws Exception {
        ReflectionTestUtils.setField(percentRequestDto, "maximumDiscountAmount", maximumDiscountAmount);
        RequestBuilder request = RestDocumentationRequestBuilders
            .post("/api/coupon/policies/stores/{storeId}/percent", Long.MIN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(percentRequestDto));

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(couponPolicyService, Mockito.never())
            .createStorePercentCouponPolicy(any(Long.class), any(CreatePercentCouponPolicyRequestDto.class));
    }

    @Test
    @DisplayName("가맹점 금액 쿠폰 정책 생성 성공")
    void postMerchantCashCouponPolicySuccessTest() throws Exception {
        RequestBuilder request = RestDocumentationRequestBuilders
            .post("/api/coupon/policies/merchants/{merchantId}/cash", Long.MIN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cashRequestDto));

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isCreated())
            .andDo(MockMvcRestDocumentationWrapper.document("createMerchantCashCouponPolicy",
                ResourceSnippetParameters.builder()
                    .pathParameters(parameterWithName("merchantId").description("가맹점 id"))
                    .requestSchema(Schema.schema("createMerchantCashCouponPolicy.Request")),
                requestFields(
                    fieldWithPath("name").description("쿠폰명"),
                    fieldWithPath("description").description("쿠폰 설명"),
                    fieldWithPath("usagePeriod").description("만료일"),
                    fieldWithPath("discountAmount").description("할인금액"),
                    fieldWithPath("minimumOrderPrice").description("최소주문금액"))
            ));

        verify(couponPolicyService, Mockito.times(1))
            .createMerchantCashCouponPolicy(any(Long.class), any(CreateCashCouponPolicyRequestDto.class));
    }

    @Test
    @DisplayName("가맹점 퍼센트 쿠폰 정책 생성 성공")
    void postMerchantPercentCouponPolicySuccessTest() throws Exception {
        RequestBuilder request = RestDocumentationRequestBuilders
            .post("/api/coupon/policies/merchants/{merchantId}/percent", Long.MIN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(percentRequestDto));

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isCreated())
            .andDo(MockMvcRestDocumentationWrapper.document("createMerchantPercentCouponPolicy",
                ResourceSnippetParameters.builder()
                    .pathParameters(parameterWithName("merchantId").description("가맹점 id"))
                    .requestSchema(Schema.schema("createMerchantPercentCouponPolicy.Request")),
                requestFields(
                    fieldWithPath("name").description("쿠폰명"),
                    fieldWithPath("description").description("쿠폰 설명"),
                    fieldWithPath("usagePeriod").description("만료일"),
                    fieldWithPath("rate").description("할인율"),
                    fieldWithPath("minimumOrderPrice").description("최소주문금액"),
                    fieldWithPath("maximumDiscountAmount").description("최대할인금액"))
            ));

        verify(couponPolicyService, Mockito.times(1))
            .createMerchantPercentCouponPolicy(any(Long.class), any(CreatePercentCouponPolicyRequestDto.class));
    }

    @Test
    @DisplayName("전체 금액 쿠폰 정책 생성 성공")
    void postAllCashCouponPolicySuccessTest() throws Exception {
        RequestBuilder request = RestDocumentationRequestBuilders
            .post("/api/coupon/policies/all/cash")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cashRequestDto));

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isCreated())
            .andDo(MockMvcRestDocumentationWrapper.document("createAllCashCouponPolicy",
                ResourceSnippetParameters.builder()
                    .requestSchema(Schema.schema("createAllCashCouponPolicy.Request")),
                requestFields(
                    fieldWithPath("name").description("쿠폰명"),
                    fieldWithPath("description").description("쿠폰 설명"),
                    fieldWithPath("usagePeriod").description("만료일"),
                    fieldWithPath("discountAmount").description("할인금액"),
                    fieldWithPath("minimumOrderPrice").description("최소주문금액"))
            ));

        verify(couponPolicyService, Mockito.times(1))
            .createAllCashCouponPolicy(any(CreateCashCouponPolicyRequestDto.class));
    }

    @Test
    @DisplayName("전체 금액 쿠폰 정책 생성 실패 - 전체 사용 범위 엔티티 부재")
    void postAllCashCouponPolicyNonUsageAllFailTest() throws Exception {
        RequestBuilder request = RestDocumentationRequestBuilders
            .post("/api/coupon/policies/all/cash")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cashRequestDto));

        doThrow(CouponUsageNotFoundException.class)
            .when(couponPolicyService).createAllCashCouponPolicy(any(CreateCashCouponPolicyRequestDto.class));

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isNotFound());

        verify(couponPolicyService, Mockito.times(1))
            .createAllCashCouponPolicy(any(CreateCashCouponPolicyRequestDto.class));
    }

    @Test
    @DisplayName("전체 퍼센트 쿠폰 정책 생성 성공")
    void postAllPercentCouponPolicySuccessTest() throws Exception {
        RequestBuilder request = RestDocumentationRequestBuilders
            .post("/api/coupon/policies/all/percent")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(percentRequestDto));

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isCreated())
            .andDo(MockMvcRestDocumentationWrapper.document("createAllPercentCouponPolicy",
                ResourceSnippetParameters.builder()
                    .requestSchema(Schema.schema("createAllPercentCouponPolicy.Request")),
                requestFields(
                    fieldWithPath("name").description("쿠폰명"),
                    fieldWithPath("description").description("쿠폰 설명"),
                    fieldWithPath("usagePeriod").description("만료일"),
                    fieldWithPath("rate").description("할인율"),
                    fieldWithPath("minimumOrderPrice").description("최소주문금액"),
                    fieldWithPath("maximumDiscountAmount").description("최대할인금액"))
            ));

        verify(couponPolicyService, Mockito.times(1))
            .createAllPercentCouponPolicy(any(CreatePercentCouponPolicyRequestDto.class));
    }

    @Test
    @DisplayName("쿠폰 정책 삭제 성공")
    void deletePolicyTest() throws Exception {
        RequestBuilder request = RestDocumentationRequestBuilders
            .delete("/api/coupon/policies/{policyId}", Long.MIN_VALUE);

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isNoContent())
            .andDo(MockMvcRestDocumentationWrapper.document("deletePolicy",
                ResourceSnippetParameters.builder()
                    .pathParameters(parameterWithName("policyId").description("쿠폰 정책 id"))
            ));

        verify(couponPolicyService, Mockito.times(1))
            .deletePolicy(any(Long.class));
    }

    @Test
    @DisplayName("쿠폰 정책 숨김 성공")
    void patchHidePolicyTest() throws Exception {
        RequestBuilder request = RestDocumentationRequestBuilders
            .patch("/api/coupon/policies/{policyId}", Long.MIN_VALUE);

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isNoContent())
            .andDo(MockMvcRestDocumentationWrapper.document("patchHidePolicy",
                ResourceSnippetParameters.builder()
                    .pathParameters(parameterWithName("policyId").description("쿠폰 정책 id"))
            ));

        verify(couponPolicyService, Mockito.times(1))
            .patchHidePolicy(anyLong());
    }

    @Test
    @DisplayName("모든 사용처 이벤트 쿠폰 획득 테스트")
    void getProvableUsageAllCouponPolicyTest() throws Exception {
        List<SelectProvableCouponPolicyResponseDto> responses = List.of(
            new SelectProvableCouponPolicyResponseDto(1L,
                new CouponTypeCash(1_000, 10_000), 30),
            new SelectProvableCouponPolicyResponseDto(2L,
                new CouponTypePercent(20, 20_000, 10_000), 14));

        when(couponPolicyService.getProvableUsageAllCouponPolicies())
            .thenReturn(responses);

        RequestBuilder request = RestDocumentationRequestBuilders
            .get("/api/coupon/policies/event");

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(MockMvcRestDocumentationWrapper.document("getProvableUsageAllCouponPolicy",
                ResourceSnippetParameters.builder()
                    .responseSchema(Schema.schema("getProvableUsageAllCouponPolicy.Response")),
                responseFields(
                    fieldWithPath("[].couponPolicyId").description("정책 id"),
                    fieldWithPath("[].couponTypeResponse.type").description("쿠폰 타입 설명"),
                    fieldWithPath("[].couponTypeResponse.discountAmount").optional().description("할인금"),
                    fieldWithPath("[].couponTypeResponse.rate").optional().description("할인율"),
                    fieldWithPath("[].couponTypeResponse.minimumOrderPrice").optional().description("최소주문금액"),
                    fieldWithPath("[].couponTypeResponse.maximumDiscountAmount").optional().description("최대할인금액"),
                    fieldWithPath("[].usagePeriod").description("사용 기간")
                )
            ));

        verify(couponPolicyService, Mockito.times(1))
            .getProvableUsageAllCouponPolicies();
    }

    @Test
    @DisplayName("가맹점 이벤트 쿠폰 획득 테스트")
    void getProvableMerchantCouponPolicyTest() throws Exception {
        List<SelectProvableCouponPolicyResponseDto> responses = List.of(
            new SelectProvableCouponPolicyResponseDto(1L,
                new CouponTypeCash(1_000, 10_000), 30),
            new SelectProvableCouponPolicyResponseDto(2L,
                new CouponTypePercent(20, 20_000, 10_000), 14));

        when(couponPolicyService.getProvableMerchantCouponPolicies(anyLong()))
            .thenReturn(responses);

        RequestBuilder request = RestDocumentationRequestBuilders
            .get("/api/coupon/policies/event/merchants/{merchantId}", Long.MAX_VALUE);

        mockMvc.perform(request)
            .andDo(print())
            .andExpect(status().isOk())
            .andDo(MockMvcRestDocumentationWrapper.document("getProvableMerchantCouponPolicy",
                ResourceSnippetParameters.builder()
                    .responseSchema(Schema.schema("getProvableMerchantCouponPolicy.Response")),
                responseFields(
                    fieldWithPath("[].couponPolicyId").description("정책 id"),
                    fieldWithPath("[].couponTypeResponse.type").description("쿠폰 타입 설명"),
                    fieldWithPath("[].couponTypeResponse.discountAmount").optional().description("할인금"),
                    fieldWithPath("[].couponTypeResponse.rate").optional().description("할인율"),
                    fieldWithPath("[].couponTypeResponse.minimumOrderPrice").optional().description("최소주문금액"),
                    fieldWithPath("[].couponTypeResponse.maximumDiscountAmount").optional().description("최대할인금액"),
                    fieldWithPath("[].usagePeriod").description("사용 기간")
                )
            ));

        verify(couponPolicyService, Mockito.times(1))
            .getProvableMerchantCouponPolicies(anyLong());
    }
}
