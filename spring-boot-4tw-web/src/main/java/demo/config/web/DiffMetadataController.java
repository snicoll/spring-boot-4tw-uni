package demo.config.web;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonView;
import demo.config.diff.ConfigDiffResult;
import demo.config.model.ConfigurationDiff;
import demo.config.model.ConfigurationDiffHandler;
import demo.config.model.DiffView;
import demo.config.service.ConfigurationDiffResultLoader;
import demo.config.validation.Version;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DiffMetadataController {

	private final ConfigurationDiffResultLoader resultLoader;

	private final ConfigurationDiffHandler handler;

	private final CounterService counterService;

	@Autowired
	public DiffMetadataController(ConfigurationDiffResultLoader resultLoader,
			CounterService counterService) {
		this.resultLoader = resultLoader;
		this.counterService = counterService;
		this.handler = new ConfigurationDiffHandler();
	}

	@RequestMapping("/diff/{fromVersion}/{toVersion}/")
	@JsonView(DiffView.Summary.class)
	public ResponseEntity<ConfigurationDiff> diffMetadata(
			@Valid @ModelAttribute DiffRequest diffRequest) throws BindException {
		ConfigDiffResult result = resultLoader.load(diffRequest.fromVersion, diffRequest.toVersion);
		ConfigurationDiff configurationDiff = handler.handle(result);
		logMetrics(diffRequest);

		return ResponseEntity.ok().eTag(createDiffETag(configurationDiff)).body(configurationDiff);
	}

	private String createDiffETag(ConfigurationDiff diff) {
		return "\"" + diff.getFromVersion() + "#" + diff.getToVersion() + "\"";
	}

	private void logMetrics(DiffRequest diffRequest) {
		this.counterService.increment("diff.from." + diffRequest.getFromVersion());
		this.counterService.increment("diff.to." + diffRequest.getToVersion());
	}

	static class DiffRequest {

		@Version
		private String fromVersion;

		@Version
		private String toVersion;

		public String getFromVersion() {
			return fromVersion;
		}

		public void setFromVersion(String fromVersion) {
			this.fromVersion = fromVersion;
		}

		public String getToVersion() {
			return toVersion;
		}

		public void setToVersion(String toVersion) {
			this.toVersion = toVersion;
		}

	}

}
