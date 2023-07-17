package com.msb.mall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// @SpringBootTest
class MallMemberApplicationTests {

    @Test
    void contextLoads() {
        // 有默认加盐 $1$ 的处理
        String md5Crypt = Md5Crypt.md5Crypt("12345".getBytes());
        System.out.println("md5Crypt"+md5Crypt);

        // 没有加盐
        String md5Hex = DigestUtils.md5Hex("12345");
        System.out.println("md5Hex = " + md5Hex);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String en1 = encoder.encode("12345");
        String en2 = encoder.encode("12345");

        System.out.println("encoder.matches(\"12345\", en1) = " + encoder.matches("12345", en1));
        System.out.println("encoder.matches(\"12345\", en2) = " + encoder.matches("12345", en2));
    }

}
