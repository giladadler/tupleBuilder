package com.aternity.tupleBuilder.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RequestMapping("/")
    public String home(@RequestParam(name="name", required=false, defaultValue="Gilad ") String name) {
        return "test";
    }
}