package demo.config.service;

import java.io.IOException;

import demo.config.diff.ConfigDiffGenerator;
import demo.config.diff.ConfigDiffResult;
import demo.config.diff.support.AetherDependencyResolver;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class ConfigurationDiffResultLoader {

	private final ConfigDiffGenerator diffGenerator;

	public ConfigurationDiffResultLoader() {
		this.diffGenerator = new ConfigDiffGenerator(AetherDependencyResolver.withAllRepositories(false));
	}

	/**
	 * Load the {@link ConfigDiffResult} between the {@code previousVersion} and the {@code nextVersion}.
	 */
	public ConfigDiffResult load(String previousVersion, String nextVersion) {
		try {
			Assert.hasText(previousVersion);
			Assert.hasText(nextVersion);
			return diffGenerator.generateDiff(previousVersion, nextVersion);
		}
		catch (IOException ex) {
			throw new RepositoryNotReachableException(ex);
		}
	}

}
