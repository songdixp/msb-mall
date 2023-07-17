package com.msb.mall.member.controller;

import com.alibaba.fastjson.JSON;
import com.msb.common.dto.MemberLoginDTO;
import com.msb.common.dto.MemberRegisterDTO;
import com.msb.common.dto.SocialUserDTO;
import com.msb.common.exception.BizCodeEnum;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;
import com.msb.mall.member.entity.MemberEntity;
import com.msb.mall.member.exception.PhoneExistException;
import com.msb.mall.member.exception.UsernameExistException;
import com.msb.mall.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 会员
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 完成auth校验之后进行注册会员
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterDTO dto)throws PhoneExistException, UsernameExistException {
        try {
            System.out.println("能不能拿到会员注册的dto = " + dto);
            memberService.register(dto);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(),
                    BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UsernameExistException e) {
            return R.error(BizCodeEnum.USERNAME_EXIST_EXCEPTION.getCode(),
                    BizCodeEnum.USERNAME_EXIST_EXCEPTION.getMsg());
        } catch (Exception e){
            return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(),
                    BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
        }
        return R.ok();

    }

    /**
     * 登录，将传过来的dto对象封装成 entity实体类
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginDTO dto){
        System.out.println("auth传过来的dto = " + dto);
        MemberEntity member = memberService.login(dto);
        if (member!=null){
            return R.ok().put("entity", JSON.toJSONString(member));
        }
        return R.error(BizCodeEnum.PASSWORD_FAILED_EXCEPTION.getCode(),
                BizCodeEnum.PASSWORD_FAILED_EXCEPTION.getMsg());
    }


    @PostMapping("/oauth2/login")
    public R socialLogin(@RequestBody SocialUserDTO dto){
        MemberEntity member = memberService.login(dto);
        if (member!=null){
            return R.ok().put("entity", JSON.toJSONString(member));
        }
        return R.error(BizCodeEnum.PASSWORD_FAILED_EXCEPTION.getCode(),
                BizCodeEnum.PASSWORD_FAILED_EXCEPTION.getMsg());
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
