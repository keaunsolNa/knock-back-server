package org.knock.knock_back.service.layerClass;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.knock.knock_back.component.util.converter.ConvertDTOAndIndex;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.Enum.PerformingArtsGenre;
import org.knock.knock_back.dto.Enum.PrfState;
import org.knock.knock_back.dto.document.category.CATEGORY_LEVEL_TWO_INDEX;
import org.knock.knock_back.dto.document.performingArts.KOPIS_INDEX;
import org.knock.knock_back.dto.document.user.SSO_USER_INDEX;
import org.knock.knock_back.dto.dto.performingArts.KOPIS_DTO;
import org.knock.knock_back.repository.performingArts.KOPISRepository;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author nks
 * @apiNote 공연예술 요청을 수행하는 Service
 */
@Service
public class PerformingArtsService {

    private final KOPISRepository kopisRepository;
    private final ConvertDTOAndIndex convertDTOAndIndex;
    private final ElasticsearchOperations elasticsearchOperations;

    public PerformingArtsService(KOPISRepository kopisRepository, ConvertDTOAndIndex convertDTOAndIndex, ElasticsearchOperations elasticsearchOperations) {
        this.kopisRepository = kopisRepository;
        this.convertDTOAndIndex = convertDTOAndIndex;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public Iterable<KOPIS_DTO> readPerformingArts() {

        long epochMillis = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())  // 시스템 기본 타임존 기준 변환
                .toInstant()
                .toEpochMilli();

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .filter(Query.of(f -> f.range(r -> r
                                .date(builder -> builder
                                        .field("from")
                                        .gte(String.valueOf(epochMillis)) // 현재 날짜보다 작거나 같은 from
                                ))))
                        .filter(Query.of(f -> f.range(r -> r
                                .date(builder -> builder
                                        .field("to")
                                        .lte(String.valueOf(epochMillis)) // 현재 날짜보다 크거나 같은 to
                                ))))
                ))
                .withSort(SortOptions.of(s -> s
                        .field(f -> f
                                .field("from")
                                .order(SortOrder.Asc)
                        )))
                .withMaxResults(100)
                .build()
                ;

        SearchHits<KOPIS_INDEX> kopis = elasticsearchOperations.search(query, KOPIS_INDEX.class);
        Iterable<KOPIS_INDEX> kopisIterable = kopis.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return convertDTOAndIndex.kopisIndexToKopisDTO(kopisIterable);

    }

    public KOPIS_DTO readPerformingArtsDetail(String id) {

        KOPIS_INDEX index = kopisRepository.findById(id).orElse(null);
        assert index != null;
        return convertDTOAndIndex.kopisIndexToKopisDTO(index);
    }

    public Map<String,Object> getCategory() {
        Map<String, Map<String, Object>> categoryMap = new HashMap<>();

        Iterable<KOPIS_INDEX> iter = kopisRepository.findAll();

        for (KOPIS_INDEX performingArts : iter)
        {
            CATEGORY_LEVEL_TWO_INDEX category = performingArts.getCategoryLevelTwo();

            if (categoryMap.containsKey(category.getNm()))
            {
                Map<String, Object> innerMap = categoryMap.get(category.getNm());

                @SuppressWarnings("unchecked")
                List<String> movies = (List<String>) innerMap.get("performingArts");
                movies.add(performingArts.getId());

            }
            else
            {
                List<String> performingArtsList = new ArrayList<>();
                performingArtsList.add(performingArts.getId());

                Map<String, Object> innerMap = new HashMap<>();
                innerMap.put("categoryId", category.getId());
                innerMap.put("categoryNm", category.getNm());
                innerMap.put("performingArts", performingArtsList);

                categoryMap.put(category.getNm(), innerMap);
            }
        }

        List<Map<String, Object>> dataList = new ArrayList<>(categoryMap.values());

        Map<String, Object> result = new HashMap<>();
        result.put("data", dataList);

        return result;
    }

    public Iterable<KOPIS_DTO> getRecommend(String performingArtsId) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .must(Query.of(t -> t.match(m -> m
                                .field("subscribeList.PERFORMING_ARTS") // ✅ text 타입이므로 match 사용
                                .query(performingArtsId)
                        )))
                ))
                .withSort(SortOptions.of(s -> s
                        .field(f -> f
                                .field("_score")
                                .order(SortOrder.Desc)
                        )))
                .withMaxResults(100)
                .build()
                ;

        SearchHits<SSO_USER_INDEX> users = elasticsearchOperations.search(query, SSO_USER_INDEX.class);

        return users.stream()
                .map(hit -> hit.getContent().getSubscribeList().get(CategoryLevelOne.PERFORMING_ARTS)) // 유저의 영화 리스트 가져오기
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .filter(id -> !id.equals(performingArtsId))
                .collect(Collectors.toMap(Function.identity(), s -> 1, Integer::sum))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(6)
                .map(entry -> kopisRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .map(convertDTOAndIndex::kopisIndexToKopisDTO)
                .collect(Collectors.toList())
                ;
    }

    public Iterable<KOPIS_DTO> readPerformingArtsByCategoryLevelTwo(String categoryNm) {

        long epochMillis = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())  // 시스템 기본 타임존 기준 변환
                .toInstant()
                .toEpochMilli();

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .must(Query.of(f -> f.term(t -> t
                                .field("categoryLevelTwo.nm")
                                .value(PerformingArtsGenre.fromEng(categoryNm))
                        )))
                        .must(Query.of(f -> f.range(r -> r
                                .date(builder -> builder
                                        .field("from")
                                        .lte(String.valueOf(epochMillis))
                                ))))
                        .must(Query.of(f -> f.range(r -> r
                                .date(builder -> builder
                                        .field("to")
                                        .gte(String.valueOf(epochMillis))
                                ))))
                ))
                .withSort(SortOptions.of(s -> s
                        .field(f -> f
                                .field("from")
                                .order(SortOrder.Asc)
                        )))
                .withMaxResults(18)
                .build()
                ;

        SearchHits<KOPIS_INDEX> kopis = elasticsearchOperations.search(query, KOPIS_INDEX.class);
        Iterable<KOPIS_INDEX> kopisIterable = kopis.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return convertDTOAndIndex.kopisIndexToKopisDTO(kopisIterable);
    }

    public Iterable<KOPIS_DTO> getUpcomingList() {

        Iterable<KOPIS_INDEX> index = kopisRepository.findByPrfState(PrfState.UPCOMING);
        return convertDTOAndIndex.kopisIndexToKopisDTO(index);
    }
}
