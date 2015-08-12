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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorPolicy;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRequest;
import org.eclipse.aether.resolution.VersionResolutionException;
import org.eclipse.aether.resolution.VersionResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.spi.locator.ServiceLocator;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 */
public class AetherDependencyResolver {

	public static final RemoteRepository SPRING_IO_RELEASE = new RemoteRepository.Builder("release",
			"default", "https://repo.spring.io/release").build();

	public static final RemoteRepository SPRING_IO_MILESTONE = new RemoteRepository.Builder("milestone",
			"default", "https://repo.spring.io/milestone").build();

	public static final RemoteRepository SPRING_IO_SNAPSHOT = new RemoteRepository.Builder("snapshot",
			"default", "https://repo.spring.io/snapshot").build();

	private final RepositorySystem repositorySystem;

	private final RepositorySystemSession session;

	private final List<RemoteRepository> repositories;

	public AetherDependencyResolver(File localMavenRepository, List<RemoteRepository> remoteRepositories) {
		ServiceLocator serviceLocator = createServiceLocator();
		this.repositorySystem = createRepositorySystem(serviceLocator);
		this.session = createRepositorySystemSession(localMavenRepository, this.repositorySystem);
		this.repositories = remoteRepositories;
	}

	public static AetherDependencyResolver withAllRepositories(boolean useLocalRepo) {
		return create(useLocalRepo, Arrays.asList(SPRING_IO_RELEASE,
				SPRING_IO_MILESTONE, SPRING_IO_SNAPSHOT));
	}

	public static AetherDependencyResolver create(boolean useLocalRepo, List<RemoteRepository> repositories) {
		File localMavenRepository = useLocalRepo ? getM2RepoDirectory() : getTempM2RepoDirectory();
		return new AetherDependencyResolver(localMavenRepository, repositories);
	}

	public ArtifactResult resolveDependency(String dependency) throws ArtifactResolutionException {
		Artifact artifact = new DefaultArtifact(dependency);
		ArtifactRequest request =
				new ArtifactRequest(artifact, this.repositories, null);
		return this.repositorySystem.resolveArtifact(session, request);
	}

	public VersionResult resolveVersion(String dependency) throws VersionResolutionException {
		VersionRequest versionRequest = new VersionRequest(new DefaultArtifact(dependency), this.repositories, null);
		return this.repositorySystem.resolveVersion(session, versionRequest);
	}

	public List<String> resolveAvailableVersions(String groupId, String artifactId) throws IOException {
		String group = groupId.replace('.', '/');
		Set<String> result = new LinkedHashSet<>();
		for (RemoteRepository repository : repositories) {
			URL url = new URL(repository.getUrl() + "/" + group + "/" + artifactId + "/maven-metadata.xml");
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			int responseCode = httpConn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				try (InputStream in = httpConn.getInputStream()) {
					result.addAll(parse(in));
				}
			}
		}
		return result.stream().sorted(String::compareTo).collect(Collectors.toList());
	}

	private Set<String> parse(InputStream in) throws IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(in);
			Element versioning = (Element) doc.getElementsByTagName("versioning").item(0);
			Element version = (Element) versioning.getElementsByTagName("versions").item(0);
			NodeList versions = version.getElementsByTagName("version");
			Set<String> content = new LinkedHashSet<>();
			for (int temp = 0; temp < versions.getLength(); temp++) {
				Node versionEl = versions.item(temp);
				content.add(versionEl.getTextContent().trim());
			}
			return content;
		}
		catch (ParserConfigurationException e) {
			throw new IllegalStateException(e);
		}
		catch (SAXException e) {
			throw new IllegalStateException("Invalid document", e);
		}
	}

	private static RepositorySystem createRepositorySystem(ServiceLocator serviceLocator) {
		return serviceLocator.getService(RepositorySystem.class);
	}

	private static RepositorySystemSession createRepositorySystemSession(File localMavenRepository,
			RepositorySystem repositorySystem) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		LocalRepository localRepository = new LocalRepository(localMavenRepository);
		LocalRepositoryManager localRepositoryManager = repositorySystem
				.newLocalRepositoryManager(session, localRepository);
		session.setLocalRepositoryManager(localRepositoryManager);

		session.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(
				ArtifactDescriptorPolicy.STRICT));

		return session;
	}

	private static ServiceLocator createServiceLocator() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositorySystem.class, DefaultRepositorySystem.class);
		locator.addService(RepositoryConnectorFactory.class,
				BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		return locator;
	}

	private static File getM2RepoDirectory() {
		return new File(getMavenHome(), "repository");
	}

	private static File getMavenHome() {
		return new File(System.getProperty("user.home"), ".m2");
	}

	private static File getTempM2RepoDirectory() {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		File file = new File(tmpDir, "tmp-maven-repo");
		if (!file.exists() && !file.mkdirs()) {
			throw new IllegalStateException("Could not create temp directory " + file.getAbsolutePath());
		}
		return file;
	}

}