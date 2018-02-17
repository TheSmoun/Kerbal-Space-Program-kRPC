package com.github.thesmoun.ksp.utils;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.SpaceCenter.Resources;
import krpc.client.services.SpaceCenter.Vessel;

public final class StageUtils {

	public static Stream<Float> getFuelStream(final int stage, final String fuelName,
			final Vessel vessel, final Connection connection) throws StreamException, RPCException {
		final Resources resources = vessel.resourcesInDecoupleStage(stage, false);
		return connection.addStream(resources, "amount", fuelName);
	}
}
