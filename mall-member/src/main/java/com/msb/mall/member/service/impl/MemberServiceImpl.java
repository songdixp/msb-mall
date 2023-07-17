package com.msb.mall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.constant.WeiboConstant;
import com.msb.common.dto.MemberLoginDTO;
import com.msb.common.dto.MemberRegisterDTO;
import com.msb.common.dto.SocialUserDTO;
import com.msb.common.utils.HttpUtils;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.mall.member.dao.MemberDao;
import com.msb.mall.member.entity.MemberEntity;
import com.msb.mall.member.exception.PhoneExistException;
import com.msb.mall.member.exception.UsernameExistException;
import com.msb.mall.member.service.MemberLevelService;
import com.msb.mall.member.service.MemberService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    MemberLevelService memberLevelService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 完成会员注册
     * @param dto commons中的DTO传输对象，auth传给member服务
     */
    @Override
    public void register(MemberRegisterDTO dto)
            throws PhoneExistException,UsernameExistException {
        MemberEntity member = new MemberEntity();
        //注册的时候是默认的会员等级id:1
        member.setLevelId(1L);
        checkUserNameUnique(dto.getUserName());
        checkUserPhoneUnique(dto.getPhone());

       // 没有异常 设置用户名、手机号、nickname
        member.setUsername(dto.getUserName());
        member.setMobile(dto.getPhone());
        member.setNickname(dto.getUserName());

        //密码加密
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String cryptPassword = encoder.encode(dto.getPassword());
        member.setPassword(cryptPassword);

        this.save(member);
        System.out.println("注册完成...");
    }

    /**
     * 1 根据账号查询到会员信息
     * 2 存在则校验密码是否正确
     * 封装成实体类
     */
    @Override
    public MemberEntity login(MemberLoginDTO dto) {
        String userName = dto.getUserName();
        System.out.println("能拿到userName = " + userName);
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("username", userName));
        System.out.println("memberEntity = " + memberEntity);
        if (memberEntity!=null){
            // 用户存在，校验密码是否正确

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean matches = encoder.matches(dto.getPassword(), memberEntity.getPassword());
            System.out.println("密码是否匹配 = " + matches);
            if ( matches){
                return memberEntity;
            }
        }

        // 密码不正确返回null
        return null;

    }

    /**
     * 社交登录
     * 实现token的获取，uid的获取
     */
    @Override
    public MemberEntity login(SocialUserDTO dto) {
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("social_uid", dto.getUid()));
        if (memberEntity!=null){
            // 不是第一次社交登录，更新数据库中的token、过期时间 expiresIn
            MemberEntity member = new MemberEntity();
            member.setId(memberEntity.getId());
            member.setExpiresIn(dto.getExpires_in());
            member.setAccessToken(dto.getAccess_token());
            this.updateById(member);
            // 返回登录用户的信息，同时也保存过期时间和token
            memberEntity.setAccessToken(dto.getAccess_token());
            memberEntity.setExpiresIn(dto.getExpires_in());
            return memberEntity;
        }

        // 第一次社交登录，进行注册
        MemberEntity member = new MemberEntity();
        //先保存传过来的字段信息
        member.setAccessToken(dto.getAccess_token());
        member.setExpiresIn(dto.getExpires_in());
        member.setSocialUid(dto.getUid());
        //然后保存weibo开放的字段信息
        Map<String,String> queryMap = new HashMap<>();
        queryMap.put("access_token", dto.getAccess_token());
        queryMap.put("uid", dto.getUid());
        try {
            HttpResponse httpResponse = HttpUtils.doGet(
                    WeiboConstant.HOST,
                    WeiboConstant.USERS_SHOW_PATH,
                    WeiboConstant.GET,
                    new HashMap<>(),
                    queryMap);
            if (httpResponse.getStatusLine().getStatusCode()==200){
                //登录成功,从相应结果中获取需要保存的字段
                String json = EntityUtils.toString(httpResponse.getEntity());
                JSONObject jsonObject = JSON.parseObject(json);
                String nickName = jsonObject.getString("screen_name");
                String gender = jsonObject.getString("gender");
                //保存数据库
                member.setNickname(nickName);
                member.setGender("m".equals(gender)?1:0); // 男:1 女:0

                this.save(member);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return member;
    }

    /**
     * 检查手机号是否重复
     */
    private void checkUserPhoneUnique(String phone) throws PhoneExistException {
        int mobile = this.count(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile>0){
            throw  new PhoneExistException();
        }
    }

    /**
     * 检查用户名是否重复
     */
    private void checkUserNameUnique(String userName) throws UsernameExistException {
        int username = this.count(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (username>0){
            throw new UsernameExistException();
        }


    }

}