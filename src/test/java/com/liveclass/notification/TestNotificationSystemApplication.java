package com.liveclass.notification;

import org.springframework.boot.SpringApplication;

public class TestNotificationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.from(NotificationSystemApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
