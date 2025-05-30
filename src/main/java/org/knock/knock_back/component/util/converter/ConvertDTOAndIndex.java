package org.knock.knock_back.component.util.converter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;
import org.knock.knock_back.dto.document.category.CATEGORY_LEVEL_TWO_INDEX;
import org.knock.knock_back.dto.document.movie.MOVIE_INDEX;
import org.knock.knock_back.dto.document.performingArts.KOPIS_INDEX;
import org.knock.knock_back.dto.document.user.SSO_USER_INDEX;
import org.knock.knock_back.dto.dto.category.CATEGORY_LEVEL_TWO_DTO;
import org.knock.knock_back.dto.dto.movie.MOVIE_DTO;
import org.knock.knock_back.dto.dto.performingArts.KOPIS_DTO;
import org.knock.knock_back.dto.dto.user.SSO_USER_DTO;
import org.springframework.stereotype.Component;

/**
 * @author nks
 * @apiNote MOVIE DTO <-> INDEX
 */
@Component
public class ConvertDTOAndIndex {

	private final StringDateConvertLongTimeStamp stringDateConvertLongTimeStamp;

	public ConvertDTOAndIndex(StringDateConvertLongTimeStamp stringDateConvertLongTimeStamp) {
		this.stringDateConvertLongTimeStamp = stringDateConvertLongTimeStamp;
	}

	/**
	 * MOVIE INDEX -> DTO
	 *
	 * @param indexs 변환할 MOVIE_INDEX 객체
	 * @return SET<MOVIE_DTO> 반환할 MOVIE_DTO 객체
	 */
	public List<MOVIE_DTO> MovieIndexToDTO(Iterable<MOVIE_INDEX> indexs) {

		List<MOVIE_DTO> result = new ArrayList<>();
		for (MOVIE_INDEX index : indexs) {
			result.add(MovieIndexToDTO(index));
		}

		// from 기준 오름차순 정렬
		result.sort(Comparator.comparing(MOVIE_DTO::getOpeningTime));

		return result;
	}

	/**
	 * MOVIE INDEX -> DTO
	 *
	 * @param index 변환할 MOVIE_INDEX 객체
	 * @return MOVIE_DTO 반환할 MOVIE_DTO 객체
	 */
	public MOVIE_DTO MovieIndexToDTO(MOVIE_INDEX index) {

		MOVIE_DTO dto = new MOVIE_DTO();
		dto.setMovieId(index.getId());
		dto.setMovieNm(index.getMovieNm());
		dto.setOpeningTime(stringDateConvertLongTimeStamp.Converter(index.getOpeningTime()));
		dto.setKOFICCode(index.getKOFICCode());
		dto.setReservationLink(index.getReservationLink());

		if (null == index.getPosterBase64()) {
			dto.setPosterBase64(null);
		} else if (index.getPosterBase64().contains("cf.lottecinema.")) {
			SrcDirectToByteImg srcDirectToByteImg = new SrcDirectToByteImg();
			dto.setPosterBase64(null);
			dto.setImg(srcDirectToByteImg.srcImgPathToByteImg(index.getPosterBase64()));
		} else {
			dto.setPosterBase64(index.getPosterBase64());
		}

		dto.setDirectors(index.getDirectors());
		dto.setActors(index.getActors());
		dto.setCompanyNm(index.getCompanyNm());
		dto.setCategoryLevelOne(index.getCategoryLevelOne());
		dto.setCategoryLevelTwo(
			null == index.getCategoryLevelTwo() ? null : CLTIndexToCLTDTO(index.getCategoryLevelTwo()));
		dto.setRunningTime(index.getRunningTime());

		String text = StringEscapeUtils.unescapeHtml4(index.getPlot());

		// <br>, <br/>, <br /> → 줄바꿈
		text = text.replaceAll("(?i)<br\\s*/?>", "\n");

		// &nbsp; → 공백
		text = text.replaceAll("&nbsp;", " ");

		// <b>태그 → 제거 (단순히 태그만 없애고 내용은 유지)
		text = text.replaceAll("(?i)</?b>", "");

		// 기타 자주 쓰는 단순 태그 제거 (<i>, <em>, <u> 등)
		text = text.replaceAll("(?i)</?(i|em|u|strong|span)>", "");

		// 여러 줄바꿈 연속 → 2줄까지만 유지
		text = text.replaceAll("(?m)\n{3,}", "\n\n");

		dto.setPlot(text);
		dto.setFavorites(index.getFavorites());
		dto.setFavoritesCount(
			null == index.getFavorites() || index.getFavorites().isEmpty() ? 0 : index.getFavorites().size());
		return dto;
	}

	/**
	 * CATEGORY_LEVEL_TWO INDEX -> CATEGORY_LEVEL_TWO DTO
	 *
	 * @param index 변환할 CATEGORY_LEVEL_TWO 객체
	 * @return CATEGORY_LEVEL_TWO 반환할 CATEGORY_LEVEL_TWO DTO 객체
	 */
	public Set<CATEGORY_LEVEL_TWO_DTO> CLTIndexToCLTDTO(Iterable<CATEGORY_LEVEL_TWO_INDEX> index) {

		Set<CATEGORY_LEVEL_TWO_DTO> result = new HashSet<>();

		for (CATEGORY_LEVEL_TWO_INDEX innerIndex : index) {

			CATEGORY_LEVEL_TWO_DTO dto = new CATEGORY_LEVEL_TWO_DTO();
			dto.setId(innerIndex.getId());
			dto.setNm(innerIndex.getNm());
			dto.setFavoriteUsers(innerIndex.getFavoriteUsers());
			dto.setParentNm(innerIndex.getParentNm());

			result.add(dto);
		}

		return result;
	}

	/**
	 * CATEGORY_LEVEL_TWO INDEX -> CATEGORY_LEVEL_TWO DTO
	 *
	 * @param index 변환할 CATEGORY_LEVEL_TWO 객체
	 * @return CATEGORY_LEVEL_TWO 반환할 CATEGORY_LEVEL_TWO INDEX 객체
	 */
	public CATEGORY_LEVEL_TWO_DTO CLTDtoToCLTIndexOne(CATEGORY_LEVEL_TWO_INDEX index) {

		CATEGORY_LEVEL_TWO_DTO dto = new CATEGORY_LEVEL_TWO_DTO();
		dto.setId(index.getId());
		dto.setNm(index.getNm());
		dto.setParentNm(index.getParentNm());
		dto.setFavoriteUsers(index.getFavoriteUsers());

		return dto;
	}

	/**
	 * SSO_USER INDEX -> SSO_USER DTO
	 *
	 * @param userIndex 변환할 CATEGORY_LEVEL_TWO 객체
	 * @return Set<CATEGORY_LEVEL_TWO> 반환할 CATEGORY_LEVEL_TWO INDEX 객체
	 */
	public SSO_USER_DTO userIndexToUserDTO(SSO_USER_INDEX userIndex) {

		SSO_USER_DTO userDto = new SSO_USER_DTO();
		userDto.setId(userIndex.getId());
		userDto.setName(userIndex.getName());
		userDto.setEmail(userIndex.getEmail());
		userDto.setNickName(userIndex.getNickName());
		userDto.setPicture(userIndex.getPicture());
		userDto.setLoginType(userIndex.getLoginType());
		userDto.setRole(userIndex.getRole());
		userDto.setFavoriteLevelOne(userIndex.getFavoriteLevelOne());
		userDto.setAlarmTimings(userIndex.getAlarmTimings());
		userDto.setLastLoginTime(userIndex.getLastLoginTime());
		userDto.setSubscribeList(userIndex.getSubscribeList());

		return userDto;
	}

	/**
	 * Iterable<KOPIS_INDEX> to Iterable<KOPIS_DTO>
	 *
	 * @return Iterable<KOPIS_DTO>
	 */
	public List<KOPIS_DTO> kopisIndexToKopisDTO(Iterable<KOPIS_INDEX> indexes) {
		List<KOPIS_DTO> result = new ArrayList<>();

		for (KOPIS_INDEX innerIndex : indexes) {
			result.add(kopisIndexToKopisDTO(innerIndex));
		}

		// from 기준 오름차순 정렬
		result.sort(Comparator.comparing(KOPIS_DTO::getFrom));

		return result;
	}

	/**
	 * KOPIS INDEX -> KOPIS DTO
	 *
	 * @param index 변환할 KOPIS 객체
	 * @return KOPIS 반환할 KOPIS DTO 객체
	 */
	public KOPIS_DTO kopisIndexToKopisDTO(KOPIS_INDEX index) {
		KOPIS_DTO kopisDto = new KOPIS_DTO();
		kopisDto.setId(index.getId());
		kopisDto.setCode(index.getCode());
		kopisDto.setName(index.getName());
		kopisDto.setFrom(index.getFrom());
		kopisDto.setTo(index.getTo());
		kopisDto.setDirectors(index.getDirectors());
		kopisDto.setActors(index.getActors());
		kopisDto.setCompanyNm(index.getCompanyNm());
		kopisDto.setHoleNm(index.getHoleNm());
		kopisDto.setPoster(index.getPoster());
		kopisDto.setStory(index.getStory());
		kopisDto.setArea(index.getArea());
		kopisDto.setPrfState(index.getPrfState());
		kopisDto.setDtguidance(index.getDtguidance());
		kopisDto.setRelates(index.getRelates());
		kopisDto.setStyurls(index.getStyurls());
		kopisDto.setRunningTime(index.getRunningTime());
		kopisDto.setCategoryLevelOne(index.getCategoryLevelOne());
		kopisDto.setCategoryLevelTwo(CLTDtoToCLTIndexOne(index.getCategoryLevelTwo()));
		kopisDto.setFavoritesCount(
			null == index.getFavorites() || index.getFavorites().isEmpty() ? 0 : index.getFavorites().size());

		return kopisDto;

	}
}
