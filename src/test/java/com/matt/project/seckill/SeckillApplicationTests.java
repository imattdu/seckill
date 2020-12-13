package com.matt.project.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest
class SeckillApplicationTests {

    @Test
    void contextLoads() {


        LocalDateTime l = LocalDateTime.now();
        String format = l.format(DateTimeFormatter.BASIC_ISO_DATE);
        System.out.println(format);
    }

}
