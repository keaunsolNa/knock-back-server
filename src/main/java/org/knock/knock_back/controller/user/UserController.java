package org.knock.knock_back.controller.user;

import java.util.List;
import java.util.Map;

import org.knock.knock_back.dto.Enum.AlarmTiming;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.dto.user.SSO_USER_DTO;
import org.knock.knock_back.service.layerClass.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * @author nks
 * @apiNote user 정보 변경에 활용되는 페이지
 * 해당 컨트롤러의 모든 인입은 JWT 토큰 유효 검증 절차 수행
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

	private final UserService userService;

	/**
	 * 토큰으로 부터 유저 정보 획득하여 반환한다
	 *
	 * @return userDto
	 */
	@GetMapping(value = "/getUserInfo")
	@ResponseBody
	public ResponseEntity<SSO_USER_DTO> getUserInfo() {
		return ResponseEntity.ok(userService.getUserInfo());
	}

	/**
	 * 전체 구독 목록을 가져온다.
	 *
	 * @return set : 카테고리 별 구독 목록 id
	 */
	@GetMapping(value = "/getSubscribeList")
	@ResponseBody
	public ResponseEntity<Map<String, Iterable<?>>> getUserCategorySubscribeList() {
		return ResponseEntity.ok(userService.getUserCategorySubscribeList());
	}

	/**
	 * 카테고리 별 구독 목록을 가져온다.
	 *
	 * @param categoryLevelOne : 확인할 대상의 종류
	 * @return set : 카테고리 별 구독 목록 id
	 */
	@GetMapping(value = "/{category}/getSubscribeList")
	@ResponseBody
	public ResponseEntity<Iterable<?>> getUserSubscribeList(
		@PathVariable(name = "category") CategoryLevelOne categoryLevelOne) {
		return ResponseEntity.ok(userService.getUserSubscribeList(categoryLevelOne));
	}

	/**
	 * 선호 카테고리를 가져온다.
	 *
	 * @return string : 선호 카테고리
	 */
	@GetMapping(value = "/getSubCategory")
	public ResponseEntity<String> getUserSubCategory() {
		return ResponseEntity.ok(userService.getUserSubCategory());
	}

	/**
	 * 알림 설정을 가져온다.
	 *
	 * @return string : 선호 카테고리
	 */
	@GetMapping(value = "/getAlarmTimings")
	public ResponseEntity<String[]> getUserAlarmTimings() {
		return ResponseEntity.ok(userService.getUserAlarmTimings());
	}

	/**
	 * 구독한다
	 *
	 * @param categoryLevelOne : 구독할 대상의 종류
	 * @param valueMap         : 변경할 대상의 id, "value : id" 형식
	 * @return boolean : 대상 영화 구독 성공 여부
	 */
	@PostMapping("/{category}/sub")
	@ResponseBody
	public ResponseEntity<Integer> subscribe(@PathVariable(name = "category") CategoryLevelOne categoryLevelOne,
		@RequestBody Map<String, String> valueMap) {
		return ResponseEntity.ok(userService.subscribe(categoryLevelOne, valueMap.get("value")));
	}

	/**
	 * 구독 해지한다
	 *
	 * @param categoryLevelOne : 구독 해지할 대상의 종류
	 * @param valueMap         : 변경할 대상의 id, "value : id" 형식
	 * @return boolean : 대상 영화 구독 해지 성공 여부
	 */
	@PostMapping("{category}/cancelSub")
	public ResponseEntity<Integer> subscribeCancel(@PathVariable(name = "category") CategoryLevelOne categoryLevelOne,
		@RequestBody Map<String, String> valueMap) {
		return ResponseEntity.ok(userService.subscribeCancel(categoryLevelOne, valueMap.get("value")));
	}

	/**
	 * 구독 확인한다.
	 *
	 * @param categoryLevelOne : 구독 해지할 대상의 종류
	 * @param valueMap         : 변경할 대상의 id, "value : id" 형식
	 * @return boolean : 대상 영화 구독 해지 성공 여부
	 */
	@PostMapping("{category}/isSubscribe")
	public ResponseEntity<Boolean> subscribeCheck(@PathVariable(name = "category") CategoryLevelOne categoryLevelOne,
		@RequestBody Map<String, String> valueMap) {
		return ResponseEntity.ok(userService.subscribeCheck(categoryLevelOne, valueMap.get("value")));
	}

	/**
	 * 유저의 선호 카테고리를 변경한다
	 *
	 * @param valueMap : 변경할 대상의 category, "value : id" 형식
	 * @return boolean : 대상 카테고리 변경 성공 여부
	 */
	@PostMapping(value = "/changeCategory")
	public ResponseEntity<Boolean> changeUserCategory(@RequestBody Map<String, String> valueMap) {
		return ResponseEntity.ok(userService.changeUserCategory(valueMap.get("value")));
	}

	/**
	 * 유저의 알람 정보를 변경한다
	 *
	 * @param categoryLevelOne : 변경할 대상
	 * @param valueMap         : 변경할 대상의 알람 정보, "value : id" 형식
	 * @return boolean : 대상 알람 정보 변경 성공 여부
	 */
	@PostMapping(value = "/{category}/changeAlarm")
	public ResponseEntity<Boolean> changeUserAlarm(@PathVariable(name = "category") CategoryLevelOne categoryLevelOne,
		@RequestBody List<AlarmTiming> valueMap) {
		return ResponseEntity.ok(userService.changeUserAlarm(categoryLevelOne, valueMap));
	}

	/**
	 * 유저의 nickName 변경한다.
	 *
	 * @param valueMap : 변경할 대상의 닉네임, "value : id" 형식
	 * @return boolean : 대상 닉네임 변경 성공 여부
	 */
	@PostMapping(value = "/changeName")
	public ResponseEntity<Boolean> changeUserName(@RequestBody Map<String, String> valueMap) {
		return ResponseEntity.ok(userService.changeUserName(valueMap.get("value")));
	}

	/**
	 * 유저 공연예술 장르별 구독 목록 조회
	 *
	 * @param genre : 대상 장르
	 * @return String[] 공연예술 id
	 */
	@GetMapping("/performingArts/{genre}/getSubscribeList")
	public ResponseEntity<String[]> getSubscribeList(@PathVariable(name = "genre") String genre) {
		return ResponseEntity.ok(userService.getSubscribeList(genre));
	}

	/**
	 * 유저 디바이스 토큰 정보 저장
	 *
	 * @param valueMap : 토큰 정보 JSON 형태
	 * @return : 저장 성공 여부
	 */
	@PostMapping("/saveToken")
	public ResponseEntity<Boolean> saveDeviceToken(@RequestBody Map<String, String> valueMap) {
		return ResponseEntity.ok(userService.saveDeviceToken(valueMap.get("targetToken")));
	}

}
