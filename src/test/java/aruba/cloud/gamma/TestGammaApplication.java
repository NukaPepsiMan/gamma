package aruba.cloud.gamma;

import org.springframework.boot.SpringApplication;

public class TestGammaApplication {

	public static void main(String[] args) {
		SpringApplication.from(GammaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
