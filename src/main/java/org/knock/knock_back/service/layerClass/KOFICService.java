package org.knock.knock_back.service.layerClass;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.knock.knock_back.service.crawling.movie.KOFIC;

/**
 * @author nks
 * @apiNote KOFIC Service
 *          크롤링을 위한 Service 객체
 *          Multi Thread, Async 방식 사용을 위해 
 *          별도 Service 생성하여 호출
 */
@Service
public class KOFICService {

    private final KOFIC kofic;

    @Autowired
    public KOFICService(KOFIC kofic) {
        this.kofic = kofic;
    }

    @Async
    public void startCrawling() {
        kofic.requestAPI();
    }

}
