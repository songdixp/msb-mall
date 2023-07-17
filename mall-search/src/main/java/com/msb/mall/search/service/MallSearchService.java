package com.msb.mall.search.service;

import com.msb.mall.search.vo.SearchParamVO;
import com.msb.mall.search.vo.SearchResultVO;

public interface MallSearchService {
    SearchResultVO search(SearchParamVO searchParamVO);
}
