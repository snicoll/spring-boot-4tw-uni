package demo.config.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class VersionStringComparator implements Comparator<String> {

	private static final List<String> QUALIFIERS = Arrays.asList("M", "RC", "BUILD-SNAPSHOT", "RELEASE");


	private String parseQualifier(String qualifier) {
		String result = qualifier.replaceAll("\\d+", "");
		return QUALIFIERS.indexOf(result) != -1 ? result : "RELEASE";
	}

	@Override
	public int compare(String versionA, String versionB) {
		String[] splitA = versionA.split("\\.");
		String[] splitB = versionB.split("\\.");
		int result;
		for (int i = 0; i < 3; i++) {
			result = Integer.parseInt(splitA[i], 10) - Integer.parseInt(splitB[i], 10);
			if (result != 0) {
				return result;
			}
		}
		String qualA = parseQualifier(splitA[3]);
		String qualB = parseQualifier(splitB[3]);
		result = QUALIFIERS.indexOf(qualA) - QUALIFIERS.indexOf(qualB);
		if (result != 0) {
			return result;
		}
		return splitA[3].compareTo(splitB[3]);
	}
}
