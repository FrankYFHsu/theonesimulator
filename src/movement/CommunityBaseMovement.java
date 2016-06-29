package movement;

import core.Coord;
import core.Settings;

/**
 * Implementation of Community-based mobility model as depicted in
 * <I>Performance Analysis of Mobility-assisted Routing</I> by Thrasyvoulos
 * Spyropoulus et al. <B>Each node performs Random Waypoint movement during each
 * of local and roaming states</B>.
 *
 * @author MPC-LAB
 */
public class CommunityBaseMovement extends MovementModel {

	/** how many waypoints should there be per path */
	private static final int PATH_LENGTH = 1;
	private Coord lastWaypoint;

	private int areaX;
	private int areaY;
	private int communitySize;
	private double probHomeToOut;
	private double[] probHomeToOuts;
	private double probOutToHome;
	private double[] probOutToHomes;
	private Coord localCommunity;

	/** "local" state or "roaming" state */
	private boolean insideLocalCommunity;

	private double communityMinX;
	private double communityMaxX;
	private double communityMinY;
	private double communityMaxY;

	public CommunityBaseMovement(Settings settings) {
		super(settings);

		communitySize = settings.getInt("baseRange");
		probHomeToOuts = settings.getCsvDoubles("probHomeToOut");
		probOutToHomes = settings.getCsvDoubles("probOutToHome");

	}

	protected CommunityBaseMovement(CommunityBaseMovement cbm) {
		super(cbm);

		this.communitySize = cbm.communitySize;
		this.probHomeToOuts = cbm.probHomeToOuts;
		this.probOutToHomes = cbm.probOutToHomes;

		this.probHomeToOut = (probHomeToOuts[1] - probHomeToOuts[0])
				* rng.nextDouble() + probHomeToOuts[0];
		this.probOutToHome = (probOutToHomes[1] - probOutToHomes[0])
				* rng.nextDouble() + probOutToHomes[0];

	}

	/**
	 * Returns a possible (random) placement for a host
	 * 
	 * @return Random position on the map
	 */
	@Override
	public Coord getInitialLocation() {
		assert rng != null : "MovementModel not initialized!";
		Coord c = initLocalCommunityCoord();
		this.insideLocalCommunity = true;
		this.lastWaypoint = c;
		return c;
	}

	/**
	 * initial the coordinates of local community
	 * 
	 * @return the coordinates of local community
	 */
	public Coord initLocalCommunityCoord() {

		double x = rng.nextDouble() * getMaxX();
		double y = rng.nextDouble() * getMaxY();

		while (x - (0.5 * communitySize) < 0
				|| x + (0.5 * communitySize) > getMaxX()) {
			x = rng.nextDouble() * getMaxX();
		}

		while (y - (0.5 * communitySize) < 0
				|| y + (0.5 * communitySize) > getMaxY()) {
			y = rng.nextDouble() * getMaxY();
		}

		communityMinX = x - (0.5 * communitySize);
		communityMaxX = x + (0.5 * communitySize);
		communityMinY = y - (0.5 * communitySize);
		communityMaxY = y + (0.5 * communitySize);

		localCommunity = new Coord(x, y);

		return localCommunity.clone();

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
	public CommunityBaseMovement replicate() {
		return new CommunityBaseMovement(this);
	}

	/**
	 * select coordinates in local community area
	 * 
	 * @return
	 */
	private Coord getNextLocalCoord() {

		double nextX = localCommunity.getX()
				+ ((rng.nextDouble() - 0.5) * communitySize);
		double nextY = localCommunity.getY()
				+ ((rng.nextDouble() - 0.5) * communitySize);

		insideLocalCommunity = true;

		Coord next = new Coord(nextX, nextY);

		return next;

	}

	/**
	 * select coordinates out of local community area
	 * 
	 * @return
	 */
	private Coord getNextRoamingCoord() {

		double nextX = rng.nextDouble() * getMaxX();
		double nextY = rng.nextDouble() * getMaxY();

		while (nextX > communityMinX && nextX < communityMaxX
				&& nextY > communityMinY && nextY < communityMaxY) {
			/* still in Community, select again */
			nextX = rng.nextDouble() * getMaxX();
			nextY = rng.nextDouble() * getMaxY();
		}
		insideLocalCommunity = false;
		return new Coord(nextX, nextY);

	}

	protected Coord randomCoord() {

		if (insideLocalCommunity) {
			/* local state */
			double stateprob = rng.nextDouble();

			if (stateprob > probHomeToOut)
				return getNextLocalCoord();// will be inside Community

			return getNextRoamingCoord();// will be outside Community

		} else {
			/* roaming state */
			double stateprob = rng.nextDouble();

			if (stateprob > probOutToHome)
				return getNextRoamingCoord();// will be outside Community

			return getNextLocalCoord(); // will be inside Community

		}

	}
}
