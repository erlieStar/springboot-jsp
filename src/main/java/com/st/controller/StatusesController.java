package com.st.controller;


import com.st.util.DateTimeUtil;
import com.st.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("statuses")
public class StatusesController {


    private final String WEIBO_URL = "q=${q}&sort=${sort}&starttime=${starttime}&endtime=${endtime}&page=${page}&count=${count}&keyid=26&keyuid=2147483647";

    @RequestMapping("index")
    public String index() {
        return  "index";
    }


    @ResponseBody
    @RequestMapping("limited")
    public String limited(@RequestParam String q, @RequestParam String starttime, @RequestParam String endtime,
                          @RequestParam String sort, @RequestParam String page, @RequestParam String count) {

        if (StringUtils.isEmpty(q) || StringUtils.isEmpty(starttime) || StringUtils.isEmpty(endtime) ||
                StringUtils.isEmpty(sort) || StringUtils.isEmpty(page) || StringUtils.isEmpty(count)) {
            return "请把参数都填完";
        }

        if (!DateTimeUtil.judgeTime(starttime) || !DateTimeUtil.judgeTime(endtime)) {
            return "时间格式不对";
        }

        if (!DateTimeUtil.judgeLegal(starttime, endtime)) {
            return "开始时间比结束时间大";
        }

        String startTime = DateTimeUtil.str2second(starttime);
        String endTime = DateTimeUtil.str2secondPlusOneDay(endtime);
        String url = WEIBO_URL.replace("${q}", q).replace("${starttime}", startTime).replace("${endtime}", endTime)
                .replace("${sort}", sort).replace("${page}", page).replace("${count}", count);

        log.info("url is {}", url);

//        return HttpClientUtil.get(url);

        return url;
    }

}
