package com.example.lib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.lib.storage.StorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class LibApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(LibApplication.class, args);
	}

}
		