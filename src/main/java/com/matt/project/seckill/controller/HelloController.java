package com.matt.project.seckill.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author matt
 * @create 2020-12-06 10:57
 */
@RestController
public class HelloController {

    @GetMapping("/")
    public String hello(){

        return "hello word";
    }
}
