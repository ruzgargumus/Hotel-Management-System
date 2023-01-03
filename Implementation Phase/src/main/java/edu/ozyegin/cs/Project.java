package edu.ozyegin.cs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ SqlConfig.class })
public class Project {
  public static void main(String[] args) {
    SpringApplication.run(Project.class, args);
  }
}
