package com.msb.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.dto.MemberLoginDTO;
import com.msb.common.dto.MemberRegisterDTO;
import com.msb.common.dto.SocialUserDTO;
import com.msb.common.utils.PageUtils;
import com.msb.mall.member.entity.MemberEntity;
import com.msb.mall.member.exception.PhoneExistException;
import com.msb.mall.member.exception.UsernameExistException;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 会员
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterDTO dto) throws PhoneExistException, UsernameExistException;

    /**
     * 普通登录
     * @param dto
     * @return
     */
    MemberEntity login(@RequestBody MemberLoginDTO dto);

    /**
     * 社交的登录
     * @param dto
     * @return
     */
    MemberEntity login(SocialUserDTO dto);
}

