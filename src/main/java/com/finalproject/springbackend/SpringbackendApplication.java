package com.finalproject.springbackend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
@SpringBootApplication
public class SpringbackendApplication {

	/**
	 * 애플리케이션 시작 시 시간대를 한국 시간(KST)으로 설정
	 */
	@PostConstruct
	public void init() {
		// JVM의 기본 시간대를 한국 시간으로 설정
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		System.out.println("🕐 애플리케이션 시간대가 한국 시간(Asia/Seoul)으로 설정되었습니다.");
	}

	public static void main(String[] args) {
		// 1. .env 파일 찾아 메모리에 로드
		Dotenv dotenv = Dotenv.load();
		// 2. .env 파일에 있는 모든 줄(KEY=VALUE)을 읽어 자바 시스템 속성으로 등록
		dotenv.entries().forEach(entry ->
				System.setProperty(
						entry.getKey(),
						entry.getValue()
				)
		);
		//3. 설정 준비 후 Spring Boot 시작
		SpringApplication.run(SpringbackendApplication.class, args);
	}
}

/*
http://43.203.121.250:9021/login
 *     "permissions": [
        "stream:raw_logs",
        "stream:auth_logs",
        "admin:all",
        "stream:unauth_logs",
        "region:ohio",
        "stream:auth_failed_logs",
        "region:seoul"
    ]
 */