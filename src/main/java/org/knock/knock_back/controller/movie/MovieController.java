package org.knock.knock_back.controller.movie;

import java.util.Map;

import org.knock.knock_back.dto.dto.movie.MOVIE_DTO;
import org.knock.knock_back.service.layerClass.Movie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author nks
 * @apiNote Movie 요청을 받는 Controller
 */
@RestController
@RequestMapping("/api/movie")
public class MovieController {

	private final Movie movieService;

	public MovieController(Movie movieService) {
		this.movieService = movieService;
	}

	/**
	 * 요청 시 MOVIE 인덱스에 저장된 모든 객체 정보를 가져와 반환한다.
	 * 반환 시 openingTime, poster 정보는 변환하여 반환
	 *
	 * @return ResponseEntity<Iterable < MOVIE_DTO>> : Movie 저장된 모든 영화 정보
	 */
	@GetMapping()
	public ResponseEntity<Iterable<MOVIE_DTO>> getMovies() {
		return ResponseEntity.ok(movieService.readMovies());
	}

	/**
	 * 요청 시 MOVIE 인덱스에 대상 객체 정보를 가져와 반환한다.
	 * 반환 시 openingTime, poster 정보는 변환하여 반환
	 *
	 * @param movieId : 대상 영화의 ID
	 * @return ResponseEntity<Iterable < MOVIE_DTO>> : Movie 저장된 모든 영화 정보
	 */
	@GetMapping("/getDetail")
	public ResponseEntity<MOVIE_DTO> getDetail(@RequestParam String movieId) {
		return ResponseEntity.ok(movieService.readMoviesDetail(movieId));
	}

	/**
	 * 노션 용 테스트
	 * @param payload
	 * @return
	 */
	@PostMapping("/test")
	public ResponseEntity<String> receiveFromNotion(@RequestBody Map<String, Object> payload) {
		System.out.println("🚀 받은 데이터: " + payload);
		return ResponseEntity.ok("✅ 데이터 수신 완료");
	}
	
	/**
	 * 요청 시 현재 상영 예정작 영화에 있는 모든 LEVEL_TWO CATEGORY 정보를 가져와 반환
	 *
	 * @return ResponseEntity<Iterable < CATEGORY_LEVEL_TWO_DTO>> : 현재 상영 예정 리스트에 있는 영화들
	 */
	@GetMapping("/getCategory")
	public ResponseEntity<Map<String, Object>> getCategory() {

		return ResponseEntity.ok(movieService.getCategory());
	}

	/**
	 * 요청 시 해당 영화를 구독한 다른 사람들이 공통으로 구독하고 있는 영화를 리스트로 만들어 반환한다.
	 *
	 * @param movieId : 확인하고 싶은 영화의 id
	 * @return ResponseEntity<Iterable < MOVIE_DTO>> : 대상 영화를 구독한 사람들이 공통적으로 구독한 영화들
	 */
	@GetMapping("/recommend")
	public ResponseEntity<Iterable<MOVIE_DTO>> getRecommend(@RequestParam String movieId) {

		return ResponseEntity.ok(movieService.getRecommend(movieId));
	}

}
