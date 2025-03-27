package org.knock.knock_back.dto.dto.crawling;

import lombok.Data;

@Data
public class CrawlingConfig {

    private String name;
    private String url;
    private String cssQuery1;
    private String cssQuery2;
    private String cssQuery3;
    private String titleQuery;
    private String dateQuery;
    private String dateExtract;
    private String reservationQuery;
    private String reservationExtract;
    private String reservationPrefix;
    private String detailPrefix;
    private String plotQuery;
    private String posterQuery;
    private String posterExtract;

}
