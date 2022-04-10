package com.lp;

import com.lp.listener.MyApplicationStartingEventListener;
import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class SpringbootNettyApplication {

	public static void main(String[] args) {
//		SpringApplication.run(SpringbootNettyApplication.class, args);
		SpringApplication app = new SpringApplication(SpringbootNettyApplication.class);
		app.addListeners(new MyApplicationStartingEventListener());
		app.run(args);
	}

}
