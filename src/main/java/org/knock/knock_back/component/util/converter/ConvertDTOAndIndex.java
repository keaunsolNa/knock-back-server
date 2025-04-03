package org.knock.knock_back.component.util.converter;

import org.knock.knock_back.dto.document.performingArts.KOPIS_INDEX;
import org.knock.knock_back.dto.document.user.SSO_USER_INDEX;
import org.knock.knock_back.dto.dto.performingArts.KOPIS_DTO;
import org.knock.knock_back.dto.dto.user.SSO_USER_DTO;
import org.springframework.stereotype.Component;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.document.category.CATEGORY_LEVEL_TWO_INDEX;
import org.knock.knock_back.dto.document.movie.KOFIC_INDEX;
import org.knock.knock_back.dto.document.movie.MOVIE_INDEX;
import org.knock.knock_back.dto.dto.category.CATEGORY_LEVEL_TWO_DTO;
import org.knock.knock_back.dto.dto.movie.MOVIE_DTO;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

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
     * MOVIE DTO -> INDEX
     * @param dtos 변환할 MOVIE_DTO 객체
     * @return SET<MOVIE_INDEX> 반환할 MOVIE_INDEX 객체
     */
    public Set<MOVIE_INDEX> MovieDtoToIndex(Iterable<MOVIE_DTO> dtos) {

        Set<MOVIE_INDEX> result = new HashSet<>();
        for (MOVIE_DTO dto : dtos) {

            MOVIE_INDEX index =
                    new MOVIE_INDEX(
                            dto.getMovieId()
                            , dto.getMovieNm()
                            , stringDateConvertLongTimeStamp.Converter(dto.getOpeningTime())
                            , dto.getKOFICCode()
                            , dto.getReservationLink()
                            , dto.getPosterBase64()
                            , dto.getDirectors()
                            , dto.getActors()
                            , dto.getCompanyNm()
                            , CategoryLevelOne.MOVIE
                            , dto.getCategoryLevelTwo() == null ? null : CLTDtoToCLTIndex(dto.getCategoryLevelTwo())
                            , dto.getRunningTime()
                            , dto.getPlot()
                            , dto.getFavorites()
                    );
            result.add(index);
        }

        return result;
    }

    /**
     * MOVIE INDEX -> DTO
     *
     * @param indexs 변환할 MOVIE_INDEX 객체
     * @return SET<MOVIE_DTO> 반환할 MOVIE_DTO 객체
     */
    public Set<MOVIE_DTO> MovieIndexToDTO(Iterable<MOVIE_INDEX> indexs) {

        Set<MOVIE_DTO> result = new LinkedHashSet<>();
        for (MOVIE_INDEX index : indexs) {
            result.add(MovieIndexToDTO(index));
        }

        return result;
    }

    /**
     * MOVIE INDEX -> DTO
     *
     * @param index 변환할 MOVIE_INDEX 객체
     * @return MOVIE_DTO 반환할 MOVIE_DTO 객체
     */
    public MOVIE_DTO MovieIndexToDTO(MOVIE_INDEX index)
    {

        MOVIE_DTO dto = new MOVIE_DTO();
        dto.setMovieId(index.getMovieId());
        dto.setMovieNm(index.getMovieNm());
        dto.setOpeningTime(stringDateConvertLongTimeStamp.Converter(index.getOpeningTime()));
        dto.setKOFICCode(index.getKOFICCode());
        dto.setReservationLink(index.getReservationLink());

        if (index.getPosterBase64().contains("cf.lottecinema."))
        {
            SrcDirectToByteImg srcDirectToByteImg = new SrcDirectToByteImg();
            dto.setPosterBase64(index.getPosterBase64());
            dto.setImg(srcDirectToByteImg.srcImgPathToByteImg(index.getPosterBase64()));
        }
        else
        {
            dto.setPosterBase64(index.getPosterBase64());
        }

        dto.setDirectors(index.getDirectors());
        dto.setActors(index.getActors());
        dto.setCompanyNm(index.getCompanyNm());
        dto.setCategoryLevelOne(index.getCategoryLevelOne());
        dto.setCategoryLevelTwo(null == index.getCategoryLevelTwo() ? null : CLTIndexToCLTDTO(index.getCategoryLevelTwo()));
        dto.setRunningTime(index.getRunningTime());
        dto.setPlot(index.getPlot());
        dto.setFavorites(index.getFavorites());
        dto.setFavoritesCount(null == index.getFavorites() || index.getFavorites().isEmpty() ? 0 : index.getFavorites().size());
        return dto;
    }


    /**
     * KOFIC INDEX -> MOVIE DTO
     *
     * @param index 변환할 KOFIC 객체
     * @return SET<MOVIE_INDEX> 반환할 MOVIE_INDEX 객체
     */
    public MOVIE_DTO koficIndexToMovieDTO(KOFIC_INDEX index) {

        MOVIE_DTO dto = new MOVIE_DTO();

        dto.setMovieId(index.getMovieId());
        dto.setMovieNm(index.getMovieNm());
        dto.setOpeningTime(stringDateConvertLongTimeStamp.Converter(index.getOpeningTime()));
        dto.setKOFICCode(index.getKOFICCode());
        dto.setDirectors(index.getDirectors());
        dto.setActors(index.getActors());
        dto.setCompanyNm(index.getCompanyNm());
        dto.setCategoryLevelTwo(null == index.getCategoryLevelTwo() ? null : CLTIndexToCLTDTO(index.getCategoryLevelTwo()));
        dto.setRunningTime(null == index.getRunningTime() ? 0 : index.getRunningTime());
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
     * CATEGORY_LEVEL_TWO DTO -> CATEGORY_LEVEL_TWO INDEX
     *
     * @param dto 변환할 CATEGORY_LEVEL_TWO 객체
     * @return Set<CATEGORY_LEVEL_TWO> 반환할 CATEGORY_LEVEL_TWO INDEX 객체
     */
    public Set<CATEGORY_LEVEL_TWO_INDEX> CLTDtoToCLTIndex(Iterable<CATEGORY_LEVEL_TWO_DTO> dto) {

        Set<CATEGORY_LEVEL_TWO_INDEX> result = new HashSet<>();

        for (CATEGORY_LEVEL_TWO_DTO innerDto : dto) {

            CATEGORY_LEVEL_TWO_INDEX index =
                    new CATEGORY_LEVEL_TWO_INDEX
                            (
                                    innerDto.getId()
                                    , innerDto.getNm()
                                    , innerDto.getParentNm()
                                    , innerDto.getFavoriteUsers()
                            );
            result.add(index);
        }


        return result;
    }

    /**
     * CATEGORY_LEVEL_TWO DTO -> CATEGORY_LEVEL_TWO INDEX
     *
     * @param dto 변환할 CATEGORY_LEVEL_TWO 객체
     * @return CATEGORY_LEVEL_TWO 반환할 CATEGORY_LEVEL_TWO INDEX 객체
     */
    public CATEGORY_LEVEL_TWO_INDEX CLTDtoToCLTIndexOne(CATEGORY_LEVEL_TWO_DTO dto) {

        return new CATEGORY_LEVEL_TWO_INDEX
                (
                        dto.getId()
                        , dto.getNm()
                        , dto.getParentNm()
                        , dto.getFavoriteUsers()
                );
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
     * @return Iterable<KOPIS_DTO>
     */
    public Iterable<KOPIS_DTO> kopisIndexToKopisDTO(Iterable<KOPIS_INDEX> indexes) {
        Set<KOPIS_DTO> result = new HashSet<>();
        for (KOPIS_INDEX innerIndex : indexes) {
            result.add(kopisIndexToKopisDTO(innerIndex));
        }

        return result;
    }

    /**
     * Iterable<KOPIS_INDEX> to Iterable<KOPIS_DTO>
     * @return Iterable<KOPIS_DTO>
     */
    public Iterable<KOPIS_INDEX> kopisDtoToKopisIndex(Iterable<KOPIS_DTO> indexes) {
        Set<KOPIS_INDEX> result = new HashSet<>();
        for (KOPIS_DTO innerDto : indexes) {
            result.add(kopisIndexToKopisDTO(innerDto));
        }

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
        kopisDto.setFavoritesCount(null == index.getFavorites() || index.getFavorites().isEmpty() ? 0 : index.getFavorites().size());

        return kopisDto;

    }

    /**
     * KOPIS DTO -> KOPIS INDEX
     *
     * @param dto 변환할 KOPIS 객체
     * @return KOPIS 반환할 KOPIS INDEX 객체
     */
    public KOPIS_INDEX kopisIndexToKopisDTO(KOPIS_DTO dto) {
        KOPIS_INDEX kopisIndex = new KOPIS_INDEX();

        kopisIndex.setId(dto.getId());
        kopisIndex.setCode(dto.getCode());
        kopisIndex.setName(dto.getName());
        kopisIndex.setFrom(dto.getFrom());
        kopisIndex.setTo(dto.getTo());
        kopisIndex.setDirectors(dto.getDirectors());
        kopisIndex.setActors(dto.getActors());
        kopisIndex.setCompanyNm(dto.getCompanyNm());
        kopisIndex.setHoleNm(dto.getHoleNm());
        kopisIndex.setPoster(dto.getPoster());
        kopisIndex.setStory(dto.getStory());
        kopisIndex.setArea(dto.getArea());
        kopisIndex.setPrfState(dto.getPrfState());
        kopisIndex.setDtguidance(dto.getDtguidance());
        kopisIndex.setRelates(dto.getRelates());
        kopisIndex.setRunningTime(dto.getRunningTime());
        kopisIndex.setCategoryLevelOne(dto.getCategoryLevelOne());
        kopisIndex.setCategoryLevelTwo(CLTDtoToCLTIndexOne(dto.getCategoryLevelTwo()));

        return kopisIndex;

    }
}
