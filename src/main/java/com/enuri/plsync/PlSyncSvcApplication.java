package com.enuri.plsync;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;

import com.enuri.plsync.banner.BannerPrinter;

@RefreshScope
@EnableDiscoveryClient
@EnableBatchProcessing
@SpringBootApplication
public class PlSyncSvcApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(PlSyncSvcApplication.class);
		app.setBannerMode(Mode.OFF);
		ConfigurableApplicationContext context = app.run(args);
	    app.setBannerMode(Mode.CONSOLE);
		new BannerPrinter(context).print(PlSyncSvcApplication.class);
	}

}
