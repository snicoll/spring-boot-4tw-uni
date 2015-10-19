package demo.config;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import demo.config.springboot.SpringBootVersionProperties;
import demo.config.springboot.SpringBootVersionService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.metrics.jmx.JmxMetricWriter;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(SpringBootVersionProperties.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	@ExportMetricWriter
	public MetricWriter metricWriter(MBeanExporter exporter) {
		return new JmxMetricWriter(exporter);
	}

	@Bean
	public HealthIndicator releaseRepositoryHealthIndicator(SpringBootVersionService springBootVersionService) {
		return new AbstractHealthIndicator() {
			@Override
			protected void doHealthCheck(Health.Builder builder) throws Exception {
				RestTemplate restTemplate = new RestTemplate();
				for (String url : springBootVersionService.getRepositoryUrls()) {
					ResponseEntity<String> entity = restTemplate
							.getForEntity(url, String.class);
					builder.up().withDetail(url, entity.getStatusCode());
				}
			}
		};
	}

	@Bean
	public JCacheManagerCustomizer cacheManagerCustomizer() {
		return cm -> {
			MutableConfiguration<Object, Object> configuration = new MutableConfiguration<>()
					.setExpiryPolicyFactory(CreatedExpiryPolicy
							.factoryOf(Duration.ONE_HOUR))
					.setStoreByValue(false)
					.setStatisticsEnabled(true);
			cm.createCache("diffs", configuration);
			cm.createCache("boot-versions", configuration);
		};
	}

}