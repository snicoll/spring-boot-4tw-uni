package demo.version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import demo.config.diff.support.AetherDependencyResolver;
import org.eclipse.aether.repository.RemoteRepository;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("4tw.versionsprovider")
public class SpringBootVersionProperties {

	private static final List<RemoteRepository> DEFAULT_REPOSITORIES =
			Collections.singletonList(AetherDependencyResolver.SPRING_IO_RELEASE);

	/**
	 * Repository URLs to use to fetch version metadata, indexed by repository name.
	 */
	private final Map<String, String> repositories = new LinkedHashMap<>();

	public Map<String, String> getRepositories() {
		return repositories;
	}

	public List<RemoteRepository> resolveRepositories() {
		if (this.repositories.isEmpty()) {
			return DEFAULT_REPOSITORIES;
		}
		List<RemoteRepository> result = new ArrayList<>();
		this.repositories.forEach((name, url) ->
				result.add(new RemoteRepository.Builder(name, "default", url).build()));
		return result;
	}

}
