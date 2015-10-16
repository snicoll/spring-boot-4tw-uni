package demo.config.web;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;

import demo.config.diff.ConfigDiffResult;
import demo.config.diff.ConfigDiffType;
import demo.config.diffview.ConfigDiff;
import demo.config.diffview.DiffViewConverter;
import demo.config.diffview.GroupDiff;
import demo.config.service.ConfigurationDiffResultLoader;
import demo.config.validation.Version;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DiffMetadataController {

	private final ConfigurationDiffResultLoader resultLoader;

	private final DiffViewConverter converter;

	private final CounterService counterService;

	@Autowired
	public DiffMetadataController(ConfigurationDiffResultLoader resultLoader,
			DiffViewConverter converter, CounterService counterService) {
		this.resultLoader = resultLoader;
		this.converter = converter;
		this.counterService = counterService;
	}

	@RequestMapping("/")
	public String diffMetadata(@Valid @ModelAttribute DiffRequest diffRequest, Model model) {

		if (diffRequest.isVersionSet()) {
			ConfigDiffResult result = resultLoader.load(
					diffRequest.fromVersion, diffRequest.toVersion);
			ConfigDiff configDiff = converter.convert(result);

			model.addAttribute("previousVersion", diffRequest.fromVersion);
			model.addAttribute("nextVersion", diffRequest.toVersion);
			model.addAttribute("full", diffRequest.full);
			List<GroupDiff> groups = configDiff.getGroups().stream()
					.filter(g -> diffRequest.full || g.getDiffType() != ConfigDiffType.EQUALS)
					.collect(Collectors.toList());
			model.addAttribute("diffs", groups);

			logMetrics(diffRequest);
		}
		else {
			model.addAttribute("previousVersion", "1.3.0.M1");
			model.addAttribute("nextVersion", "1.3.0.M3");
			model.addAttribute("diffs", null);
		}
		return "diff";
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

		private boolean full;

		public boolean isVersionSet() {
			return this.fromVersion != null || this.toVersion != null;
		}

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

		public boolean isFull() {
			return full;
		}

		public void setFull(boolean full) {
			this.full = full;
		}
	}

}
