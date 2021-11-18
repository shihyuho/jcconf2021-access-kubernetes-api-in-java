package com.github.shihyuho.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }
}

@RequestMapping("/pods")
@RestController
class PodController {

  @GetMapping("/{namespace}")
  void listPods(@PathVariable("namespace") String namespace) {
  }

  @PostMapping("/{namespace}/{name}")
  void createPod(@PathVariable("namespace") String namespace, @PathVariable("name") String name) {
  }

  @DeleteMapping("/{namespace}/{name}")
  void deletePod(@PathVariable("namespace") String namespace, @PathVariable("name") String name) {
  }
}
