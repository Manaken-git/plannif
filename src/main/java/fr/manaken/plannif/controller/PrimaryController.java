package fr.manaken.plannif.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrimaryController {

    @GetMapping("/test/{id}")
    public void test(@PathVariable Integer id) {

    }
}
