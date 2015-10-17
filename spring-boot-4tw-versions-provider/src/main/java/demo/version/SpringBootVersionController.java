package demo.version;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@CrossOrigin
public class SpringBootVersionController {

	private final SpringBootVersionService bootVersionService;

	@Autowired
	public SpringBootVersionController(SpringBootVersionService bootVersionService) {
		this.bootVersionService = bootVersionService;
	}

	@RequestMapping("/springboot/versions")
	@ResponseBody
	public List<String> fetchBootVersions() {
		return bootVersionService.fetchBootVersions();
	}

}
