package kr.ac.hansung.cse.minifarm.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class HealthController {
    @GetMapping("/health")
    public String health(){ return "ok"; }
}
