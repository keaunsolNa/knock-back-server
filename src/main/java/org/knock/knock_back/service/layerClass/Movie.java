package org.knock.knock_back.service.layerClass;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.knock.knock_back.component.util.converter.ConvertDTOAndIndex;
import org.knock.knock_back.dto.document.category.CATEGORY_LEVEL_TWO_INDEX;
import org.knock.knock_back.dto.document.movie.KOFIC_INDEX;
import org.knock.knock_back.dto.document.movie.MOVIE_INDEX;
import org.knock.knock_back.dto.document.user.SSO_USER_INDEX;
import org.knock.knock_back.dto.dto.movie.MOVIE_DTO;
import org.knock.knock_back.repository.movie.MovieRepository;
import org.knock.knock_back.repository.user.SSOUserRepository;
import org.knock.knock_back.service.layerInterface.MovieInterface;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author nks
 * @apiNote Movie 요청을 수행하는 Service
 */
@Service
public class Movie implements MovieInterface {

    private final MovieMaker movieMaker;
    private final ConvertDTOAndIndex translation;
    private final SSOUserRepository ssoUserRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final MovieRepository movieRepository;

    public Movie(ConvertDTOAndIndex translation,
                 SSOUserRepository ssoUserRepository,
                 ElasticsearchOperations elasticsearchOperations, MovieRepository movieRepository) {
        this.ssoUserRepository = ssoUserRepository;
        this.movieMaker = new MovieMaker(movieRepository, elasticsearchOperations);
        this.translation = translation;
        this.elasticsearchOperations = elasticsearchOperations;
        this.movieRepository = movieRepository;
    }

    public void createMovie(Set<MOVIE_DTO> movies) {

        for (SSO_USER_INDEX ssoUserIndex : ssoUserRepository.findAll()) {

            Set<String> movieList = ssoUserIndex.getSubscribeList().get(CategoryLevelOne.MOVIE);

            if (movieList != null) {
                Set<String> movieIdsToKeep = movies.stream()
                        .map(MOVIE_DTO::getMovieId)
                        .collect(Collectors.toSet());

                movieList.removeIf(movieId -> !movieIdsToKeep.contains(movieId));
            }
        }
        // DELETE ALL DATA BEFORE CREATE
        movieMaker.deleteMovie();

        movieMaker.CreateMovie(translation.MovieDtoToIndex(movies));

    }

    public Iterable<MOVIE_DTO> readMovies() {

        Iterable<MOVIE_DTO> movieDtos = translation.MovieIndexToDTO(movieMaker.readAllMovie());

        List<MOVIE_DTO> list = new ArrayList<>();
        List<MOVIE_DTO> nullList = new ArrayList<>();

        for (MOVIE_DTO movie : movieDtos) {

            if (movie.getOpeningTime().equals("개봉 예정")) nullList.add(movie);
            else list.add(movie);

        }

        list.addAll(nullList);

        return list;
    }

    public Optional<MOVIE_INDEX> checkMovie(String movieNm) { return movieMaker.readMovieByNm(movieNm); }

    public MOVIE_DTO readMoviesDetail(String id) {

        MOVIE_INDEX movies = movieMaker.readMovieById(id);
        return translation.MovieIndexToDTO(movies);
    }

    public KOFIC_INDEX similaritySearch (String movieNm) throws Exception
    {
        SearchHits<KOFIC_INDEX> searchHits = movieMaker.searchKOFICByMovieNm(movieNm);

        if (searchHits.getTotalHits() - 1 > 1 &&
                searchHits.getSearchHit(0).getScore() == searchHits.getSearchHit(1).getScore()) {
            throw new Exception("_score 동점");
        }
        try
        {
            return Objects.requireNonNull(searchHits.stream().findFirst().orElse(null)).getContent();
        }
        catch (NullPointerException e)
        {
            return null;
        }
    }

    public KOFIC_INDEX similaritySearch (String movieNm, String directorNm)
    {
        SearchHits<KOFIC_INDEX> searchHits = movieMaker.searchKOFICByMovieNmAndDirectorNm(movieNm, directorNm);

        try
        {
            return Objects.requireNonNull(searchHits.stream().findFirst().orElse(null)).getContent();
        }
        catch (NullPointerException e)
        {
            return null;
        }
    }

    public Map<String, Object> getCategory()
    {
        Map<String, Map<String, Object>> categoryMap = new HashMap<>();

        Iterable<MOVIE_INDEX> iter = movieMaker.readAllMovie();

        for (MOVIE_INDEX movie : iter)
        {
            for (CATEGORY_LEVEL_TWO_INDEX category : movie.getCategoryLevelTwo())
            {

                if (categoryMap.containsKey(category.getNm()))
                {
                    Map<String, Object> innerMap = categoryMap.get(category.getNm());

                    @SuppressWarnings("unchecked")
                    List<String> movies = (List<String>) innerMap.get("movies");
                    movies.add(movie.getMovieId());

                }
                else
                {
                    List<String> movies = new ArrayList<>();
                    movies.add(movie.getMovieId());

                    Map<String, Object> innerMap = new HashMap<>();
                    innerMap.put("categoryId", category.getId());
                    innerMap.put("categoryNm", category.getNm());
                    innerMap.put("movies", movies);

                    categoryMap.put(category.getNm(), innerMap);
                }
            }
        }

        List<Map<String, Object>> dataList = new ArrayList<>(categoryMap.values());

        Map<String, Object> result = new HashMap<>();
        result.put("data", dataList);

        return result;
    }

    public List<MOVIE_DTO> getRecommend (String movieId)
    {

        /*
         *  movieId를 좋아하는 user 집합
         *  해당 user 들이 좋아하는, parameter movieId를 제외한 영화들
         *  Map<String[MovieId], Integer> 형태로 점수 메기기
         *  Integer 순으로 정렬하여 상위 리스트 반환
         */
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .must(Query.of(t -> t.match(m -> m
                                .field("subscribeList.MOVIE") // ✅ text 타입이므로 match 사용
                                .query(movieId)
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
                .map(hit -> hit.getContent().getSubscribeList().get(CategoryLevelOne.MOVIE)) // 유저의 영화 리스트 가져오기
                .filter(Objects::nonNull) // null 값 제거
                .flatMap(Set::stream) // 리스트를 단일 스트림으로 변환
                .filter(id -> !id.equals(movieId)) // parameter 받은 movieId 제외
                .collect(Collectors.toMap(Function.identity(), s -> 1, Integer::sum)) // 영화 ID를 키로, 등장 횟수를 값으로 저장
                .entrySet().stream() // Map 스트림으로 변환
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) // 값 기준 내림차순 정렬
                .limit(6) // 상위 6개만 선택
                .map(entry -> movieRepository.findById(entry.getKey()).orElse(null)) // movieId로 MOVIE_INDEX 조회
                .filter(Objects::nonNull) // null 값 제거
                .map(translation::MovieIndexToDTO)  // MOVIE_INDEX -> MOVIE_DTO 변환
                .collect(Collectors.toList())   // 리스트로 변환
        ;

    }

}
