package com.duoc.tiendalibros.libros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MsLibrosApplication {

  public static void main(String[] args) {
    SpringApplication.run(MsLibrosApplication.class, args);
  }
}
