package com.msb.mall.search.controller;

import com.msb.mall.search.service.MallSearchService;
import com.msb.mall.search.vo.SearchParamVO;
import com.msb.mall.search.vo.SearchResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {
    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping(value={"/list.html","/"})
    public String searchList(SearchParamVO paramVO, Model model){
        SearchResultVO resultVO = mallSearchService.search(paramVO);
        model.addAttribute("result", resultVO);
        return "index";
    }


}
