/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo.config.diff.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionResolutionException;
import org.eclipse.aether.resolution.VersionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;
import org.springframework.core.io.Resource;

/**
 *
 * @author Stephane Nicoll
 */
public class ConfigurationMetadataRepositoryLoader {

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationMetadataRepositoryLoader.class);


	private final AetherDependencyResolver dependencyResolver;

	public ConfigurationMetadataRepositoryLoader(AetherDependencyResolver dependencyResolver) {
		this.dependencyResolver = dependencyResolver;
	}

	public ConfigurationMetadataRepository load(String version) throws IOException {
		ConfigurationMetadataRepositoryJsonBuilder builder = ConfigurationMetadataRepositoryJsonBuilder.create();
		load(builder, "org.springframework.boot:spring-boot", version, true);
		load(builder, "org.springframework.boot:spring-boot-actuator", version, true);
		load(builder, "org.springframework.boot:spring-boot-autoconfigure", version, true);
		load(builder, "org.springframework.boot:spring-boot-devtools", version, false);
		return builder.build();
	}

	public String resolveVersion(String version) {
		try {
			VersionResult versionResult = dependencyResolver
					.resolveVersion("org.springframework.boot:spring-boot:" + version);
			return versionResult.getVersion();
		}
		catch (VersionResolutionException e) {
			logger.error("Could not resolve version '" + version + "'", e);
		}
		return version;
	}

	private Resource load(ConfigurationMetadataRepositoryJsonBuilder builder, String moduleId,
			String version, boolean mandatory)
			throws IOException {
		String coordinates = moduleId + ":" + version;
		try {
			ArtifactResult artifactResult = dependencyResolver.resolveDependency(coordinates);
			File file = artifactResult.getArtifact().getFile();
			JarFile jarFile = new JarFile(file);
			ZipEntry entry = jarFile.getEntry("META-INF/spring-configuration-metadata.json");
			if (entry != null) {
				logger.info("Adding meta-data from '" + coordinates + "'");
				try (InputStream stream = jarFile.getInputStream(entry)) {
					builder.withJsonResource(stream);
				}
			}
			else {
				logger.info("No meta-data found for '" + coordinates + "'");
			}
		}
		catch (ArtifactResolutionException e) {
			if (mandatory) {
				throw new UnknownSpringBootVersion("Could not load '" + coordinates + "'", version);
			}
			logger.info("Ignoring '" + coordinates + " (not found)");
		}
		return null;
	}
}
