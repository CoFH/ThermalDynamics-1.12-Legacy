package cofh.thermaldynamics.debughelper;

import cofh.thermaldynamics.ThermalDynamics;

import java.util.HashSet;

public class ErrorHelper {

	private static HashSet<String> stringSet = new HashSet<String>();

	public static void reportProblemOnce(String message) {

		if (stringSet.add(message)) {
			ThermalDynamics.LOG.error(message);
		}
	}

}
