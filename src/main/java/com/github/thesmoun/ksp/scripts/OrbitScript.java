package com.github.thesmoun.ksp.scripts;

import com.github.thesmoun.ksp.utils.OrbitUtils;

import krpc.client.Connection;
import krpc.client.Stream;
import krpc.client.services.KRPC;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Resources;
import krpc.client.services.SpaceCenter.Vessel;

public final class OrbitScript {

	private static final double TURN_START_ALT = 250;
	private static final double TURN_END_ALT = 45000;
	
	@SuppressWarnings("unchecked")
	public static void execute(final Connection connection, final KRPC krpc, final SpaceCenter spaceCenter,
			final Vessel vessel, final double targetAltitude) throws Exception {
		final Resources stage2Resources = vessel.resourcesInDecoupleStage(2, false);
		final Stream<Float> srbFuel = connection.addStream(stage2Resources, "amount", "SolidFuel");
		
		OrbitUtils.launchToOrbit(targetAltitude, targetAltitude, TURN_START_ALT, TURN_END_ALT, 2,
				new Stream[] { srbFuel }, connection, krpc, spaceCenter, vessel);
	}
}
