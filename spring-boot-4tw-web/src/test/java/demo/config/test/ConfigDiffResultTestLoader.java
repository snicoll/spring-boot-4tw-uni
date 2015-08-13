package demo.config.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import demo.config.diff.ConfigDiffGenerator;
import demo.config.diff.ConfigDiffResult;
import demo.config.diff.support.ConfigurationMetadataRepositoryLoader;
import demo.config.diff.support.UnknownSpringBootVersion;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.core.io.ClassPathResource;

import static org.mockito.BDDMockito.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

public class ConfigDiffResultTestLoader {

	public static ConfigDiffResult generateDiff(String left, String right) throws IOException {
		ConfigDiffGenerator configDiffGenerator = mockConfigDiffGenerator(left, right);
		return configDiffGenerator.generateDiff(left, right);
	}

	public static ConfigDiffGenerator mockConfigDiffGenerator(Collection<String> validVersions,
			Collection<String> invalidVersions) throws IOException {
		ConfigurationMetadataRepositoryLoader resolver = mock(ConfigurationMetadataRepositoryLoader.class);
		for (String validVersion : validVersions) {
			ConfigurationMetadataRepository repo = loadRepository(validVersion);
			given(resolver.load(validVersion)).willReturn(repo);
		}
		for (String invalidVersion : invalidVersions) {
			given(resolver.load(invalidVersion)).willThrow(
					new UnknownSpringBootVersion("Test exception", invalidVersion));
		}
		given(resolver.resolveVersion(anyString())).will(invocation -> invocation.getArguments()[0]);
		return new ConfigDiffGenerator(resolver);
	}

	public static ConfigDiffGenerator mockConfigDiffGenerator(String left, String right) throws IOException {
		return mockConfigDiffGenerator(Arrays.asList(left, right), Collections.emptyList());
	}

	private static ConfigurationMetadataRepository loadRepository(String name) throws IOException {
		ClassPathResource classPathResource = new ClassPathResource("meta-data/" + name + ".json");
		try (InputStream in = classPathResource.getInputStream()) {
			return ConfigurationMetadataRepositoryJsonBuilder.create().withJsonResource(in).build();
		}
	}
}
