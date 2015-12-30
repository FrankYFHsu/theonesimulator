/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package movement;

import core.Coord;
import core.ExponentialRNG;
import core.Settings;

/**
 * Modificated Random Direction movement model.
 * 
 * Original from Royer E, Melliar-Smith PM, Moser L.
 * "An analysis of the optimum node density for ad hoc mobile networks." <I>In
 * Proceedings of the IEEE ICC</I>, 2001.
 * 
 * However, this is the implementation of RD model as described in T.
 * Spyropoulos, K. Psounis, and C. S. Raghavendra.
 * "Performance analysis of mobility-assisted routing." <I>Proceedings of ACM
 * MobiHoc</I>, 2006, 49-60
 * 
 * @author YuFeng Hsu
 */
public class ModifiedRandomDirection extends MovementModel implements
		SwitchableMovement {

	private Coord lastWaypoint;

	/** Modified Random Direction's setting namespace ({@value} ) */
	public static final String MRD_NS = "ModifiedRandomDirection";
	
	/** default epoch length */
	public static final int DEFAULT_EPOCH_LENGTH = 100;

	/**
	 * Expected epoch length
	 * */
	public static final String EXPECTED_EPOCH_LENGTH = "expectedEpochLength";

	/**
	 * 1 / Expected epoch duration
	 * */
	private double lambda;

	public ModifiedRandomDirection(Settings settings) {
		super(settings);
		Settings prophetSettings = new Settings(MRD_NS);

		int expectedEpochLength;
		if (prophetSettings.contains(EXPECTED_EPOCH_LENGTH)) {
			expectedEpochLength = prophetSettings.getInt(EXPECTED_EPOCH_LENGTH);

		} else {
			expectedEpochLength = DEFAULT_EPOCH_LENGTH;
		}

		double expectedSpeed = (minSpeed + maxSpeed) / 2;
		lambda = 1.0 * expectedSpeed / expectedEpochLength;

	}

	private ModifiedRandomDirection(ModifiedRandomDirection rwp) {
		super(rwp);
		lambda = rwp.lambda;
	}

	/**
	 * Returns a possible (random) placement for a host
	 * 
	 * @return Random position on the map
	 */
	@Override
	public Coord getInitialLocation() {
		assert rng != null : "MovementModel not initialized!";
		double x = rng.nextDouble() * getMaxX();
		double y = rng.nextDouble() * getMaxY();
		Coord c = new Coord(x, y);

		this.lastWaypoint = c;
		return c;
	}

	@Override
	public Path getPath() {
		Path p;
		double speed = generateSpeed();
		p = new Path(speed);

		p.addWaypoint(lastWaypoint.clone());
		double maxX = getMaxX();
		double maxY = getMaxY();

		Coord c = null;
		while (true) {

			double angle = rng.nextDouble() * 2 * Math.PI;

			ExponentialRNG erng = new ExponentialRNG(rng, lambda);

			double duration = erng.nextExp();

			double distance = duration * speed;

			double x = lastWaypoint.getX() + distance * Math.cos(angle);
			double y = lastWaypoint.getY() + distance * Math.sin(angle);

			c = new Coord(x, y);

			if (x > 0 && y > 0 && x < maxX && y < maxY) {
				break;
			}
		}

		p.addWaypoint(c);

		this.lastWaypoint = c;
		return p;
	}

	@Override
	public ModifiedRandomDirection replicate() {
		return new ModifiedRandomDirection(this);
	}

	public Coord getLastLocation() {
		return lastWaypoint;
	}

	public void setLocation(Coord lastWaypoint) {
		this.lastWaypoint = lastWaypoint;
	}

	public boolean isReady() {
		return true;
	}
}
