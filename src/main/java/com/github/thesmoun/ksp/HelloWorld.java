package com.github.thesmoun.ksp;

import com.github.thesmoun.ksp.scripts.OrbitScript;

import krpc.client.Connection;
import krpc.client.services.KRPC;
import krpc.client.services.SpaceCenter;
import krpc.client.services.SpaceCenter.Vessel;

public final class HelloWorld {

	public static void main(final String[] args) throws Throwable {
		final Connection connection = Connection.newInstance();
		final KRPC krpc = KRPC.newInstance(connection);
		final SpaceCenter spaceCenter = SpaceCenter.newInstance(connection);
		final Vessel vessel = spaceCenter.getActiveVessel();
		
		OrbitScript.execute(connection, krpc, spaceCenter, vessel, 85000);
		
		connection.close();
	}
}
