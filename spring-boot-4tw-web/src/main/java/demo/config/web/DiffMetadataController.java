package demo.config.web;

import javax.validation.Valid;

import demo.config.diff.ConfigDiffResult;
import demo.config.diffview.ConfigDiff;
import demo.config.diffview.DiffViewConverter;
import demo.config.service.ConfigurationDiffResultLoader;
import demo.config.validation.Version;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DiffMetadataController {

	private final ConfigurationDiffResultLoader resultLoader;

	private final DiffViewConverter converter;

	@Autowired
	public DiffMetadataController(ConfigurationDiffResultLoader resultLoader,
			DiffViewConverter converter) {
		this.resultLoader = resultLoader;
		this.converter = converter;
	}

	@RequestMapping("/")
	public String diffMetadata(@Valid @ModelAttribute DiffRequest diffRequest, Model model) {

		if (diffRequest.isVersionSet()) {
			ConfigDiffResult result = resultLoader.load(
					diffRequest.fromVersion, diffRequest.toVersion);
			ConfigDiff configDiff = converter.convert(result);

			model.addAttribute("previousVersion", diffRequest.fromVersion);
			model.addAttribute("nextVersion", diffRequest.toVersion);
			model.addAttribute("diffs", configDiff.getGroups());
		}
		else {
			model.addAttribute("previousVersion", "");
			model.addAttribute("nextVersion", "");
			model.addAttribute("diffs", null);
		}
		return "diff";
	}


	static class DiffRequest {

		@Version
		private String fromVersion;

		@Version
		private String toVersion;

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
	}

}
