package co.com.menor.commerce_core_bd;

import co.com.menor.security_core.security.annotation.EnableSecurityCore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

import java.util.TimeZone;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableSecurityCore
public class CommerceCoreServiceBdApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
        SpringApplication.run(CommerceCoreServiceBdApplication.class, args);
    }
}