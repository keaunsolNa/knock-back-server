package org.knock.knock_back.service.layerInterface;

import org.knock.knock_back.dto.document.movie.MOVIE_INDEX;
import org.knock.knock_back.repository.movie.MovieRepository;

/**
 * @author nks
 * @apiNote MOVIE 관련 인터페이스,
 * MOVIE-INDEX 관련 CRUD 작업 정의
 */
public interface MovieInterface {

	class MovieMaker {

		private final MovieRepository movieRepository;

		// Constructor
		public MovieMaker(MovieRepository movieRepository) {
			this.movieRepository = movieRepository;
		}

		public Iterable<MOVIE_INDEX> readAllMovie() {
			return movieRepository.findAll();
		}

		public MOVIE_INDEX readMovieById(String id) {
			return movieRepository.findById(id).orElseThrow();
		}

	}
}
