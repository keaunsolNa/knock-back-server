package org.knock.knock_back.service.layerInterface;

import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.knock.knock_back.dto.document.movie.KOFIC_INDEX;
import org.knock.knock_back.dto.document.movie.MOVIE_INDEX;
import org.knock.knock_back.repository.movie.MovieRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MovieInterface {

    class MovieMaker {

        private final MovieRepository movieRepository;
        private final ElasticsearchOperations elasticsearchOperations;
        // Constructor
        public MovieMaker(MovieRepository movieRepository, ElasticsearchOperations elasticsearchOperations) {
            this.movieRepository = movieRepository;
            this.elasticsearchOperations = elasticsearchOperations;
        }

        public void recreateMovies(Set<MOVIE_INDEX> movies) {
            movieRepository.deleteAll();
            final int batchSize = 100;
            List<MOVIE_INDEX> buffer = new ArrayList<>(batchSize);

            for (MOVIE_INDEX movie : movies) {
                buffer.add(movie);
                if (buffer.size() >= batchSize) {
                    movieRepository.saveAll(buffer);
                    buffer.clear();
                }
            }

            if (!buffer.isEmpty()) {
                movieRepository.saveAll(buffer);
            }
        }

        public List<MOVIE_INDEX> readAllMovie() {
            List<MOVIE_INDEX> result = new ArrayList<>();
            movieRepository.findAll().forEach(result::add);
            return result;
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
    }
}
