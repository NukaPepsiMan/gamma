package aruba.cloud.gamma;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = GammaApplication.class)
@Import(TestContainersConfig.class)
public class GammaApplicationTests {

}
