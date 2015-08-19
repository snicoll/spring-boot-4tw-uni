package demo.version;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import demo.config.diff.support.AetherDependencyResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class SpringBootVersionService {

	private static final Logger logger = LoggerFactory.getLogger(SpringBootVersionService.class);

	private final AetherDependencyResolver dependencyResolver;

	private final List<String> repositoryUrls;

	@Autowired
	public SpringBootVersionService(SpringBootVersionProperties properties) {
		List<RemoteRepository> repositories = properties.resolveRepositories();
		this.dependencyResolver = AetherDependencyResolver.create(false, repositories);
		this.repositoryUrls = repositories.stream()
				.map(RemoteRepository::getUrl).collect(Collectors.toList());
	}

	public List<String> getRepositoryUrls() {
		return this.repositoryUrls;
	}

	@Cacheable("boot-versions")
	public List<String> fetchBootVersions() {
		try {
			logger.info("Fetching Spring Boot versions from {}", repositoryUrls);
			return dependencyResolver.resolveAvailableVersions("org.springframework.boot", "spring-boot");
		}
		catch (IOException e) {
			throw new IllegalStateException("Failed to fetch Spring Boot versions", e);
		}
	}

}
