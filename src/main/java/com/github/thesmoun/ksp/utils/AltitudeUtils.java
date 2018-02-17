package com.github.thesmoun.ksp.utils;

import krpc.client.Connection;
import krpc.client.Event;
import krpc.client.RPCException;
import krpc.client.Stream;
import krpc.client.StreamException;
import krpc.client.services.KRPC;
import krpc.client.services.KRPC.Expression;
import krpc.client.services.SpaceCenter.Flight;
import krpc.client.services.SpaceCenter.Vessel;
import krpc.schema.KRPC.ProcedureCall;

public final class AltitudeUtils {

	public static void waitForAltitude(final double alt, final Connection connection,
			final KRPC krpc, final Flight flight) throws RPCException, StreamException {
		final ProcedureCall altCall = connection.getCall(flight, "getMeanAltitude");
		final Expression altExpression = Expression.greaterThanOrEqual(connection,
				Expression.call(connection, altCall),
				Expression.constantDouble(connection, alt));
		
		final Event altEvent = krpc.addEvent(altExpression);
		synchronized (altEvent.getCondition()) {
			altEvent.waitFor();
		}
	}
	
	public static void waitForApoapsis(final double alt, final Connection connection,
			final KRPC krpc, final Vessel vessel) throws RPCException, StreamException {
		final ProcedureCall altCall = connection.getCall(vessel.getOrbit(), "getApoapsisAltitude");
		final Expression altExpression = Expression.greaterThanOrEqual(connection,
				Expression.call(connection, altCall),
				Expression.constantDouble(connection, alt));
		
		final Event altEvent = krpc.addEvent(altExpression);
		synchronized (altEvent.getCondition()) {
			altEvent.waitFor();
		}
	}
	
	public static void turnBetween(final double startAlt, final double endAlt, final double apoapsis,
			final Connection connection, final KRPC krpc, final Flight flight, final Vessel vessel,
			final Stream<Float>[] stageResourceStreams) throws StreamException, RPCException {
		final Stream<Double> altStream = connection.addStream(flight, "getMeanAltitude");
		final Stream<Double> apoapsisStream = connection.addStream(vessel.getOrbit(), "getApoapsisAltitude");
		
		final int numberOfStages = stageResourceStreams != null ? stageResourceStreams.length : 0;
		final boolean[] stagesSeparated = new boolean[numberOfStages];
		
		double turnAngle = 0;
		double currentAlt = altStream.get();
		while (currentAlt > startAlt && currentAlt < endAlt) {
			final double frac = (currentAlt - startAlt) / (endAlt - startAlt);
			final double newTurnAngle = frac * 90.0;
			if (Math.abs(newTurnAngle - turnAngle) > 0.5) {
				turnAngle = newTurnAngle;
				vessel.getAutoPilot().targetPitchAndHeading((float) (90 - turnAngle), 90);
			}
			
			final int nextStageToSeparate = findNextStageToSeparate(stagesSeparated, stageResourceStreams);
			if (nextStageToSeparate >= 0) {
				vessel.getControl().activateNextStage();
				stagesSeparated[nextStageToSeparate] = true;
			}
			
			if (apoapsisStream.get() > apoapsis * 0.9)
				break;
			
			currentAlt = altStream.get();
		}
	}
	
	private static int findNextStageToSeparate(final boolean[] separated, final Stream<Float>... streams)
			throws RPCException, StreamException {
		if (separated.length != streams.length)
			throw new IllegalArgumentException("both arrays must have the same size");
		
		for (int i = 0; i < separated.length; i++) {
			if (separated[i])
				continue;
			
			if (streams[i].get() < 0.1)
				return i;
		}
		
		return -1;
	}
}
