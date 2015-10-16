package demo.config;

import java.util.Random;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ExportMetricWriter;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.metrics.jmx.JmxMetricWriter;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.jmx.export.MBeanExporter;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public EmbeddedServletContainerCustomizer servletContainerCustomizer() {
		return container -> container.addErrorPages(new ErrorPage(HttpStatus.BAD_REQUEST, "/error/400.html"));
	}

	@Bean
	@ExportMetricWriter
	public MetricWriter metricWriter(MBeanExporter exporter) {
		return new JmxMetricWriter(exporter);
	}

	@Bean
	public HealthIndicator forTheWebHealthIndicator() {
		return () -> {
			if (new Random().nextBoolean()) {
				return Health.up().build();
			}
			else {
				return Health.down()
						.withDetail("Ooops", 42)
						.build();
			}
		};
	}

}