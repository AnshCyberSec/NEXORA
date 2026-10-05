package com.nexora.auth;

import com.nexora.auth.entity.Role;
import com.nexora.auth.enums.RoleType;
import com.nexora.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	@Component
	@RequiredArgsConstructor
	public static class RoleInitializer implements CommandLineRunner {

		private final RoleRepository roleRepository;

		@Override
		public void run(String... args) {
			for (RoleType roleType : RoleType.values()) {
				if (!roleRepository.findByName(roleType).isPresent()) {
					Role role = new Role(roleType);
					roleRepository.save(role);
					System.out.println("Role created: " + roleType);
				}
			}
		}
	}
}