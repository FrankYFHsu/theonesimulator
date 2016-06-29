/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package movement;

import core.Coord;
import core.Settings;

/**
 * Random waypoint movement model. Creates zig-zag paths within the <B>confined</B> simulation
 * area.
 */
public class ConfinedRandomWaypoint extends MovementModel {
	/** how many waypoints should there be per path */
	private static final int PATH_LENGTH = 1;
	private Coord lastWaypoint;

	private double[] areaX;
	private double[] areaY;

	private double minX;
	private double maxX;
	private double minY;
	private double maxY;

	public ConfinedRandomWaypoint(Settings settings) {
		super(settings);
		areaX = settings.getCsvDoubles("areaX");
		areaY = settings.getCsvDoubles("areaY");
	}

	protected ConfinedRandomWaypoint(ConfinedRandomWaypoint rwp) {
		super(rwp);

		this.areaX = rwp.areaX;
		this.areaY = rwp.areaY;

		minX = areaX[0];
		maxX = areaX[1];
		minY = areaY[0];
		maxY = areaY[1];

	}

	/**
	 * Returns a possible (random) placement for a host
	 * 
	 * @return Random position on the map
	 */
	@Override
	public Coord getInitialLocation() {
		assert rng != null : "MovementModel not initialized!";
		Coord c = randomCoord();

		this.lastWaypoint = c;
		return c;
	}

	@Override
	public Path getPath() {
		Path p;
		p = new Path(generateSpeed());
		p.addWaypoint(lastWaypoint.clone());
		Coord c = lastWaypoint;

		for (int i = 0; i < PATH_LENGTH; i++) {
			c = randomCoord();
			p.addWaypoint(c);
		}

		this.lastWaypoint = c;
		return p;
	}

	@Override
	public ConfinedRandomWaypoint replicate() {
		return new ConfinedRandomWaypoint(this);
	}

	protected Coord randomCoord() {

		double nextX = (maxX - minX) * rng.nextDouble() + minX;
		double nextY = (maxY - minY) * rng.nextDouble() + minY;

		return new Coord(nextX, nextY);
	}
}
