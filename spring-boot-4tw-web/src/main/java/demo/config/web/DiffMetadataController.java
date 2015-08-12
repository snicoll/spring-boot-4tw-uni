package demo.config.web;

import demo.config.diff.ConfigDiffResult;
import demo.config.diffview.ConfigDiff;
import demo.config.diffview.DiffViewConverter;
import demo.config.service.ConfigurationDiffResultLoader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
	public String diffMetadata(String fromVersion, String toVersion, Model model) {

		if (fromVersion != null || toVersion != null) {
			ConfigDiffResult result = resultLoader.load(fromVersion, toVersion);
			ConfigDiff configDiff = converter.convert(result);

			model.addAttribute("previousVersion", fromVersion);
			model.addAttribute("nextVersion", toVersion);
			model.addAttribute("diffs", configDiff.getGroups());
		}
		else {
			model.addAttribute("previousVersion", "");
			model.addAttribute("nextVersion", "");
			model.addAttribute("diffs", null);
		}
		return "diff";
	}

}
