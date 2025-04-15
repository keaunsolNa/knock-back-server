package org.knock.knock_back.service.layerClass;

import lombok.RequiredArgsConstructor;
import org.knock.knock_back.component.util.converter.ConvertDTOAndIndex;
import org.knock.knock_back.dto.Enum.AlarmTiming;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.Enum.PerformingArtsGenre;
import org.knock.knock_back.dto.document.movie.MOVIE_INDEX;
import org.knock.knock_back.dto.document.performingArts.KOPIS_INDEX;
import org.knock.knock_back.dto.document.user.SSO_USER_INDEX;
import org.knock.knock_back.dto.dto.movie.MOVIE_DTO;
import org.knock.knock_back.dto.dto.performingArts.KOPIS_DTO;
import org.knock.knock_back.dto.dto.user.SSO_USER_DTO;
import org.knock.knock_back.repository.movie.KOFICRepository;
import org.knock.knock_back.repository.movie.MovieRepository;
import org.knock.knock_back.repository.performingArts.KOPISRepository;
import org.knock.knock_back.repository.user.SSOUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author nks
 * @apiNote user 정보 변경에 활용되는 service
 */
@RequiredArgsConstructor
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final ConvertDTOAndIndex convertDTOAndIndex;
    private final MovieRepository movieRepository;
    private final KOPISRepository kopisRepository;
    private final KOFICRepository koficRepository;
    private final SSOUserRepository ssoUserRepository;

    /**
     * 토큰으로 부터 유저 정보 획득하여 반환한다
     * @return userDto
     */
    public SSO_USER_DTO getUserInfo()
    {

        try
        {
            SSO_USER_INDEX user = getCurrentUser();
            return convertDTOAndIndex.userIndexToUserDTO(user);
        }

        catch (Exception e)
        {
            logger.warn("토큰에서 유저 정보 가져오기 중 에러 발생, {}", e.getMessage());
            return null;
        }
    }

    /**
     * 전체 구독 목록을 가져온다.
     * @return set : 카테고리 별 구독 목록 id
     */
    public Map<String, Iterable<?>> getUserCategorySubscribeList()
    {
        try
        {

            SSO_USER_INDEX user = getCurrentUser();
            Map<CategoryLevelOne, Set<String>> map = user.getSubscribeList();

            Map<String, Iterable<?>> userSubscribeList = new HashMap<>();

            for (CategoryLevelOne category : map.keySet())
            {
                Set<String> list = map.get(category);
                Set<?> set = makeSet(category, list);
                userSubscribeList.put(category.name(), set);
            }

            return userSubscribeList;
        }

        catch (Exception e)
        {
            logger.warn("전체 구독 목록 가져오기 중 에러 발생, {}", e.getMessage());
            return null;
        }

    }

    /**
     * 카테고리 별 구독 목록을 가져온다.
     * @param categoryLevelOne : 확인할 대상의 종류
     * @return set : 카테고리 별 구독 목록 id
     */
    public Iterable<?> getUserSubscribeList(CategoryLevelOne categoryLevelOne)
    {
        try
        {
            SSO_USER_INDEX user = getCurrentUser();

            Set<String> targetList = user.getSubscribeList().get(categoryLevelOne);

            return targetList
                    .stream()
                    .filter(id -> isOver(id, categoryLevelOne))
                    .toList();
        }

        catch (Exception e)
        {
            logger.warn("구독 목록 가져오기 중 에러 발생, {}", e.getMessage());
            return null;
        }

    }

    /**
     * 선호 구독 대상을 가져온다.
     * @return string : 선호 카테고리
     */
    public String getUserSubCategory()
    {
        try
        {

            SSO_USER_INDEX user = getCurrentUser();

            return user.getFavoriteLevelOne().name();
        }

        catch (Exception e)
        {
            logger.warn("구독 대상 가져오기 중 에러 발생, {}", e.getMessage());
            return null;
        }

    }

    /**
     * 카테고리 별 알림 타이밍을 가져온다.
     * @return string : 선호 카테고리
     */
    public String[] getUserAlarmTimings()
    {
        try
        {

            SSO_USER_INDEX user = getCurrentUser();

            AlarmTiming[] alarmTimings = user.getAlarmTimings();
            return Arrays.stream(alarmTimings).map(Enum::name).toArray(String[]::new);

        }

        catch (Exception e)
        {
            logger.warn("카테고리 별 구독 가져오기 중 에러 발생, {}", e.getMessage());
            return null;
        }

    }
    
    /**
     * 구독 한다
     * @param categoryLevelOne : 구독할 대상의 종류
     * @param id : 변경할 대상의 id
     * @return boolean : 대상 영화 구독 성공 여부
     */
    public Integer subscribe(CategoryLevelOne categoryLevelOne, String id)
    {
        try
        {
            SSO_USER_INDEX user = getCurrentUser();

            user.getSubscribeList().get(categoryLevelOne).add(id);
            ssoUserRepository.save(user);

            return CategoryLevelOneUpdate(categoryLevelOne, id, user.getId(), true);
        }

        catch (Exception e)
        {
            logger.warn("구독 중 에러 발생, {}", e.getMessage());
            return -1;
        }
    }

    /**
     * 구독 해지 한다
     * @param categoryLevelOne : 구독 해지할 대상의 종류
     * @param id : 변경할 대상의 id
     * @return boolean : 대상 영화 구독 해지 성공 여부
     */
    public Integer subscribeCancel(CategoryLevelOne categoryLevelOne, String id)
    {
        try
        {
            SSO_USER_INDEX user = getCurrentUser();
            user.getSubscribeList().get(categoryLevelOne).remove(id);
            ssoUserRepository.save(user);
            return CategoryLevelOneUpdate(categoryLevelOne, id, user.getId(), false);
        }

        catch (Exception e)
        {
            logger.warn("구독 취소 중 에러 발생, {}", e.getMessage());
            return -1;
        }
    }

    /**
     * 구독 확인 한다.
     * @param categoryLevelOne : 구독 해지할 대상의 종류
     * @param id : 변경할 대상의 id
     * @return boolean : 대상 영화 구독 해지 성공 여부
     */
    public Boolean subscribeCheck(CategoryLevelOne categoryLevelOne, String id)
    {
        try
        {
            SSO_USER_INDEX user = getCurrentUser();
            return user.getSubscribeList().get(categoryLevelOne).contains(id);
        }

        catch (Exception e)
        {
            logger.warn("구독 확인 중 에러 발생, {}", e.getMessage());
            return false;
        }
    }

    /**
     * 유저의 선호 카테고리를 변경 한다
     * @param categoryName : 변경할 대상의 category
     * @return boolean : 대상 카테고리 변경 성공 여부
     */
    public Boolean changeUserCategory(String categoryName)
    {
        try
        {

            SSO_USER_INDEX user = getCurrentUser();

            user.updateFavoriteLevelOne(CategoryLevelOne.valueOf(categoryName.toUpperCase()));
            ssoUserRepository.save(user);

            return true;
        }

        catch (Exception e)
        {
            logger.warn("선호 카테고리 변경 중 에러 발생, {}", e.getMessage());
            return false;
        }
    }

    /**
     * 유저의 알람 정보를 변경한다
     * @param categoryLevelOne : 변경할 대상
     * @param alarmValues : 변경할 대상의 알람 정보
     * @return boolean : 대상 알람 정보 변경 성공 여부
     */
    public Boolean changeUserAlarm(CategoryLevelOne categoryLevelOne, List<AlarmTiming> alarmValues)
    {

        try
        {

            logger.info("[{}]", alarmValues);
            SSO_USER_INDEX user = getCurrentUser();

            int idx = categoryLevelOne.equals(CategoryLevelOne.MOVIE) ? 0
                    : categoryLevelOne.equals(CategoryLevelOne.PERFORMING_ARTS) ? 1 : 2;

            AlarmTiming[] alarmTimings = user.getAlarmTimings();
            alarmTimings[idx] = alarmValues.get(idx);
            user.updateAlarmTimings(alarmTimings);

            ssoUserRepository.save(user);

            return true;
        }

        catch (Exception e)
        {
            logger.warn("알람 정보 변경 중 에러 발생, {}", e.getMessage());
            return false;
        }
    }

    /**
     * 유저의 nickName 변경한다.
     * @param nickName : 변경할 대상의 닉네임
     * @return boolean : 대상 닉네임 변경 성공 여부
     */
    public Boolean changeUserName(String nickName)
    {
        try
        {

            SSO_USER_INDEX user = getCurrentUser();
            user.updateNickName(nickName);
            ssoUserRepository.save(user);

            return true;
        }

        catch (Exception e)
        {
            logger.warn("닉네임 변경 중 에러 발생, {}", e.getMessage());
            return false;
        }
    }

    /**
     * 장르별 구독 id 가져온다.
     * @param performingArtsGenre : 대상 장르
     * @return String[] id 배열
     */
    public String[] getSubscribeList(String performingArtsGenre)
    {

        List<String> returnValue;

        try
        {

            SSO_USER_INDEX user = getCurrentUser();
            Set<String> list = user.getSubscribeList().get(CategoryLevelOne.PERFORMING_ARTS);
            returnValue = new ArrayList<>();

            for (String id : list)
            {

                KOPIS_INDEX kopis = new KOPIS_INDEX();

                String value = PerformingArtsGenre.fromEng(performingArtsGenre);

                if (kopisRepository.findById(id).isPresent()) kopis = kopisRepository.findById(id).get();
                if (kopis.getCategoryLevelTwo().getNm().equals(value)) returnValue.add(id);

            }

        }

        catch (Exception e)
        {
            logger.warn("장르별 구독 id 가져오기 중 에러 발생, {}", e.getMessage());
            return new String[0];
        }

        return returnValue.toArray(new String[0]);
    }


    /**
     * 로그인한 유저의 deviceToken을 유저에 저장한다.
     * @param targetToken : deviceToken 값
     * @return : 저장 성공 여부
     */
    public Boolean saveDeviceToken(String targetToken) {

        try
        {

            SSO_USER_INDEX user = getCurrentUser();

            if (null == user.getDeviceToken() || user.getDeviceToken().isEmpty()) user.setDeviceToken(new ArrayList<>());

            if (user.getDeviceToken().contains(targetToken)) return true;
            user.getDeviceToken().add(targetToken);

            ssoUserRepository.save(user);

            return true;
        }

        catch (Exception e)
        {
            logger.warn("디바이스 토큰 저장 중 에러 발생, {}", e.getMessage());
            return false;
        }

    }

    /**
     * MOVIE, PERFORMING_ARTS 등의 카테고리 index 변경
     * @param target : 변경할 category
     * @param targetId : 변경할 category ID
     * @param userId : 변경할 user ID
     * @param flag : 구독 / 구독 취소
     * @return Integer : 변경 후 대상 구독 수
     */
    private Integer CategoryLevelOneUpdate (CategoryLevelOne target, String targetId, String userId, boolean flag)
    {

        switch (target)
        {
            case CategoryLevelOne.MOVIE ->
            {

                MOVIE_INDEX movieIndex = movieRepository.findById(targetId).orElseThrow();

                if (null == movieIndex.getFavorites() || movieIndex.getFavorites().isEmpty())
                {
                    movieIndex.setFavorites(new ArrayList<>());
                }

                if (flag) movieIndex.getFavorites().add(userId);
                else movieIndex.getFavorites().remove(userId);

                movieRepository.save(movieIndex);

                return movieIndex.getFavorites().size();
            }

            case CategoryLevelOne.PERFORMING_ARTS ->
            {
                KOPIS_INDEX performingIndex = kopisRepository.findById(targetId).orElseThrow();

                if (null == performingIndex.getFavorites() || performingIndex.getFavorites().isEmpty())
                {
                    performingIndex.setFavorites(new HashSet<>());
                }

                if (flag) performingIndex.getFavorites().add(userId);
                else performingIndex.getFavorites().remove(userId);

                kopisRepository.save(performingIndex);

                return performingIndex.getFavorites().size();
            }

        }

        return -1;
    }

    /**
     * 대상 카테고리의 id 목록을 받아 대상 Iterable<DTO> 형태로 반환
     * @param target : 변경할 category
     * @param list : 변경할 category ID
     * @return Set<?> : 생성된 DTO 목록
     */
    private Set<?> makeSet (CategoryLevelOne target, Set<String> list)
    {
        switch (target)
        {

            case CategoryLevelOne.MOVIE ->
            {

                Set<MOVIE_DTO> set = Set.of();
                try
                {
                    Iterable<MOVIE_INDEX> movies = movieRepository.findAllById(list);
                    set = new HashSet<>(convertDTOAndIndex.MovieIndexToDTO(movies));
                    set = set.stream()
                            .filter(dto -> isOver(dto.getMovieId(), CategoryLevelOne.MOVIE))
                            .collect(Collectors.toSet());

                } 
                catch (Exception e)
                {
                    logger.warn("카테고리 id 목록으로 영화 인덱스 객체 가져오기 중 에러 발생, {}", e.getMessage());
                }


                return set;
            }

            case CategoryLevelOne.PERFORMING_ARTS ->
            {

                Set<KOPIS_DTO> set = Set.of();
                try
                {
                    Iterable<KOPIS_INDEX> performingArts = kopisRepository.findAllById(list);
                    set = new HashSet<>(convertDTOAndIndex.kopisIndexToKopisDTO(performingArts));
                    set = set.stream()
                            .filter(dto -> isOver(dto.getId(), CategoryLevelOne.PERFORMING_ARTS))
                            .collect(Collectors.toSet());
                }
                catch (Exception e)
                {
                    logger.warn("카테고리 id 목록으로 공연예술 인덱스 객체 가져오기 중 에러 발생, {}", e.getMessage());
                }

                return set;
            }
        }

        return null;
    }

    private SSO_USER_INDEX getCurrentUser()
    {
        return (SSO_USER_INDEX) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private boolean isOver(String id, CategoryLevelOne category)
    {
        Date currentDate = new Date();
        if (category.equals(CategoryLevelOne.MOVIE))
        {

            if (kopisRepository.findById(id).isPresent())
            {
                KOPIS_INDEX movieIndex = kopisRepository.findById(id).get();
                return movieIndex.getFrom().before(currentDate) && movieIndex.getTo().after(currentDate);
            }
        }
        else if (category.equals(CategoryLevelOne.PERFORMING_ARTS))
        {

            if (koficRepository.findById(id).isEmpty())
            {
                KOPIS_INDEX kopisIndex = kopisRepository.findById(id).get();
                return kopisIndex.getFrom().before(currentDate) && kopisIndex.getTo().after(currentDate);
            }
        }

        return false;
    }

}
