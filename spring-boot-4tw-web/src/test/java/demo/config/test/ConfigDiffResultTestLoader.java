package demo.config.test;

import java.io.IOException;
import java.io.InputStream;

import demo.config.diff.ConfigDiffGenerator;
import demo.config.diff.ConfigDiffResult;
import demo.config.diff.support.ConfigurationMetadataRepositoryLoader;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.core.io.ClassPathResource;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

public class ConfigDiffResultTestLoader {

	public static ConfigDiffResult generateDiff(String left, String right) throws IOException {
		ConfigDiffGenerator configDiffGenerator = mockConfigDiffGenerator(left, right);
		return configDiffGenerator.generateDiff(left, right);
	}

	public static ConfigDiffGenerator mockConfigDiffGenerator(String left, String right) throws IOException {
		ConfigurationMetadataRepository leftRepo = loadRepository(left);
		ConfigurationMetadataRepository rightRepo = loadRepository(right);
		ConfigurationMetadataRepositoryLoader resolver = mock(ConfigurationMetadataRepositoryLoader.class);
		given(resolver.load(left)).willReturn(leftRepo);
		given(resolver.load(right)).willReturn(rightRepo);
		return new ConfigDiffGenerator(resolver);
	}

	private static ConfigurationMetadataRepository loadRepository(String name) throws IOException {
		ClassPathResource classPathResource = new ClassPathResource("meta-data/" + name + ".json");
		try (InputStream in = classPathResource.getInputStream()) {
			return ConfigurationMetadataRepositoryJsonBuilder.create().withJsonResource(in).build();
		}
	}
}
