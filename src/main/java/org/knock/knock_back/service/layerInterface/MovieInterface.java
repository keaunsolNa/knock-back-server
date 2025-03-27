package org.knock.knock_back.service.layerInterface;

import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.knock.knock_back.dto.document.movie.KOFIC_INDEX;
import org.knock.knock_back.dto.document.movie.MOVIE_INDEX;
import org.knock.knock_back.repository.movie.MovieRepository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface MovieInterface {

    class MovieMaker {

        private final MovieRepository movieRepository;
        private final ElasticsearchOperations elasticsearchOperations;
        // Constructor
        public MovieMaker(MovieRepository movieRepository, ElasticsearchOperations elasticsearchOperations) {
            this.movieRepository = movieRepository;
            this.elasticsearchOperations = elasticsearchOperations;
        }

        public void CreateMovie(Set<MOVIE_INDEX> movieINDEX) {
            movieRepository.saveAll(movieINDEX);
        }

        public Iterable<MOVIE_INDEX> readAllMovie() {

            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.matchAll(m -> m
                    ))
                    .withSort(Sort.by(Sort.Order.asc("openingTime")))
                    .withMaxResults(100)
                    .build();

            SearchHits<MOVIE_INDEX> searchHits = elasticsearchOperations.search(query, MOVIE_INDEX.class);

            return searchHits.getSearchHits()
                    .stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
        }

        public Optional<MOVIE_INDEX> readMovieByNm(String nm) { return movieRepository.findByMovieNm(nm); }

        public MOVIE_INDEX readMovieById(String id) { return movieRepository.findById(id).orElseThrow(); }

        public SearchHits<KOFIC_INDEX> searchKOFICByMovieNm(String movieNm)
        {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.match(m -> m
                            .field("movieNm")
                            .query(movieNm)
                            .fuzziness("AUTO")
                            .analyzer("nori")
                    ))
                    .withSort(Sort.by(Sort.Order.desc("_score")))
                    .withMaxResults(5)
                    .build();

            return elasticsearchOperations.search(query, KOFIC_INDEX.class);
        }

        public SearchHits<KOFIC_INDEX> searchKOFICByMovieNmAndDirectorNm(String movieNm, String directorNm)
        {
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> b
                            .should(s -> s.match(m -> m
                                    .field("movieNm")
                                    .query(movieNm)
                                    .fuzziness("AUTO")
                                    .analyzer("nori")
                            ))
                            .should(s -> s.match(m -> m
                                    .field("directors")
                                    .query(directorNm)
                                    .analyzer("nori")
                            ))
                    ))
                    .withSort(Sort.by(Sort.Order.desc("_score")))
                    .withMaxResults(5)
                    .build()
                    ;

            return elasticsearchOperations.search(query, KOFIC_INDEX.class);
        }

        public void updateMovie(MOVIE_INDEX movieINDEX) {

            MOVIE_INDEX movieIndex = movieRepository.findById(movieINDEX.getMovieId()).orElseThrow();
            movieIndex.setFavorites(movieINDEX.getFavorites());
            movieIndex.setReservationLink(movieINDEX.getReservationLink());

            movieRepository.save(movieIndex);

        }

        public void deleteMovie() {
            movieRepository.deleteMOVIE_INDEXByOpeningTimeBefore(System.currentTimeMillis());
        }

        public void deleteById(String id) {
            movieRepository.deleteById(id);
        }
    }
}
