package com.lp;

import com.lp.annotation.MyLog;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@MyLog
public class SpringbootNettyApplicationTests {

	@Test
	public void contextLoads() {
	}


	@Test
	public void aspectTest() {
		System.out.println("dsf");
	}

}
