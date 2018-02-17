package com.github.thesmoun.ksp.utils;

import org.javatuples.Triplet;

import krpc.client.Connection;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.KRPC;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Node;
import krpc.client.services.SpaceCenter.Vessel;

public final class OrbitUtils {

	private static final double KERBIN_ATHMOSPHERE_HEIGHT = 70500;
	
	public static void launchToOrbit(final double apoapsis, final double periapsis,
			final double turnStart, final double turnEnd, final double accuracy,
			final Stream<Float>[] stageStreams, final Connection connection,
			final KRPC krpc, final SpaceCenter spaceCenter, final Vessel vessel)
					throws RPCException, InterruptedException, StreamException {
		final Flight flight = vessel.flight(vessel.getSurfaceReferenceFrame());
		
		VesselUtils.launch(vessel);
		AltitudeUtils.waitForAltitude(turnStart, connection, krpc, flight);
		AltitudeUtils.turnBetween(turnStart, turnEnd, apoapsis, connection, krpc, flight, vessel, stageStreams);
		VesselUtils.throttle(vessel, 0.25F);
		AltitudeUtils.waitForApoapsis(apoapsis, connection, krpc, vessel);
		VesselUtils.throttle(vessel, 0F);
		AltitudeUtils.waitForAltitude(KERBIN_ATHMOSPHERE_HEIGHT, connection, krpc, flight);
		circularizeOrbit(apoapsis, periapsis, accuracy, connection, krpc, spaceCenter, vessel);
	}
	
	private static void circularizeOrbit(final double apoapsis, final double periapsis,
			final double accuracy, final Connection connection, final KRPC krpc,
			final SpaceCenter spaceCenter, final Vessel vessel) throws RPCException, InterruptedException {
		final double mu = vessel.getOrbit().getBody().getGravitationalParameter();
		final double r = vessel.getOrbit().getApoapsis();
		final double a1 = vessel.getOrbit().getSemiMajorAxis();
		final double a2 = r;
		final double v1 = Math.sqrt(mu * ((2.0 / r) - (1.0 / a1)));
		final double v2 = Math.sqrt(mu * ((2.0 / r) - (1.0 / a2)));
		final double deltaV = v2 - v1;
		
		final Node node = vessel.getControl().addNode(spaceCenter.getUT() + vessel.getOrbit().getTimeToApoapsis(),
				(float) deltaV, 0, 0);
		
		final double force = vessel.getAvailableThrust();
		final double isp = vessel.getSpecificImpulse() * 9.82;
		final double m0 = vessel.getMass();
		final double m1 = m0 / Math.exp(deltaV / isp);
		final double flowRate = force / isp;
		final double burnTime = (m0 - m1) / flowRate;
		
		vessel.getAutoPilot().setReferenceFrame(node.getReferenceFrame());
		vessel.getAutoPilot().setTargetDirection(new Triplet<Double, Double, Double>(0.0, 1.0, 0.0));
		vessel.getAutoPilot().wait_();
		
		final double burnUt = spaceCenter.getUT() + vessel.getOrbit().getTimeToApoapsis() - (burnTime / 2.0);
		final double leadTime = 5;
		spaceCenter.warpTo(burnUt - leadTime, 100000, 2);
		
		while (vessel.getOrbit().getTimeToApoapsis() - (burnTime / 2.0) > 0) {
			// wait
		}
		
		vessel.getControl().setThrottle(1);
		Thread.sleep((long) ((burnTime - 0.1) * 1000));
		vessel.getControl().setThrottle(0.05F);
		
		while (node.remainingBurnVector(node.getReferenceFrame()).getValue1() > accuracy) {
			// wait
		}
		
		vessel.getControl().setThrottle(0);
		node.remove();
	}
}
