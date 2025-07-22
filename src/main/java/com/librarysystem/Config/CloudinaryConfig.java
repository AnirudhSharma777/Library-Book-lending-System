package com.librarysystem.Config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfig {


    @Value("${cloudinary.api_key}")
    private String API_KEY;
    @Value("${cloudinary.api_secret}")
    private String API_SECRET;
    @Value("${cloudinary.cloud_name}")
    private String NAME;


    @Bean
    public Cloudinary cloudinary(){
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name",NAME);
        config.put("api_key",API_KEY);
        config.put("api_secret",API_SECRET);

        return new Cloudinary(config);
    }

}
