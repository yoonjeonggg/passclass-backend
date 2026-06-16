package app_programming_development.Class;

import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class PassClassApplicationTests {

	@MockBean
	JavaMailSender javaMailSender;

	@MockBean
	FirebaseApp firebaseApp;

	@MockBean
	Bucket bucket;

	@Test
	void contextLoads() {
	}

}
