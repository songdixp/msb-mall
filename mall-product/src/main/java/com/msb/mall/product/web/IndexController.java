package com.msb.mall.product.web;

import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.CategoryService;
import com.msb.mall.product.vo.Catalog2VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;


    @GetMapping({"/","/index.html","/index","/home","/home.html"})
    public String index(Model model){
        // 配置文件中 前缀 classPath:/templates/
        // 后缀我习惯性仍然用html

        // 响应一级分类数据给index
        List<CategoryEntity> list = categoryService.getCategoryLevel1();
        model.addAttribute("categorys",list);
        return "index";
    }

    @ResponseBody
    @RequestMapping("/index/catalog.json")
    public Map<String, List<Catalog2VO>> getCatelog2Json(){
        return categoryService.getCatalog2JSON();
    }
}
