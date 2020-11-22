package com.example.client.grpcclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GrpCclientApplication implements CommandLineRunner {

	@Autowired
	private GrpcClient grpcClient;

	public static void main(String[] args) {
		SpringApplication.run(GrpCclientApplication.class, args);
	}

	public void run(String ...args) throws Exception {
		grpcClient.connect();
	}

}
