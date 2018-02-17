package com.github.thesmoun.ksp.utils;

import krpc.client.RPCException;
import krpc.client.services.SpaceCenter.Vessel;

public final class VesselUtils {

	public static void launch(final Vessel vessel) throws RPCException, InterruptedException {
		vessel.getControl().setSAS(false);
		vessel.getControl().setRCS(false);
		vessel.getControl().setThrottle(1);
		
		System.out.println("3...");
		Thread.sleep(1000);
		System.out.println("2...");
		Thread.sleep(1000);
		System.out.println("1...");
		Thread.sleep(1000);
		System.out.println("Lift off");
		
		vessel.getControl().activateNextStage();
		vessel.getAutoPilot().engage();
		vessel.getAutoPilot().targetPitchAndHeading(90, 90);
	}
	
	public static void throttle(final Vessel vessel, final float throttle) throws RPCException {
		vessel.getControl().setThrottle(throttle);
	}
}
