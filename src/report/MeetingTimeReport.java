package report;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import report.ContactTimesReport.ConnectionInfo;
import core.ConnectionListener;
import core.DTNHost;
import core.Settings;
import core.SimClock;

public class MeetingTimeReport extends Report implements ConnectionListener {

	/**
	 * Granularity -setting id ({@value} ). Defines how many simulated seconds
	 * are grouped in one reported interval.
	 */
	public static final String GRANULARITY = "granularity";
	/** How many seconds are grouped in one group */
	protected double granularity;
	private MeetingEntry globalMeetingTime;

	/**
	 * Constructor.
	 */
	public MeetingTimeReport() {
		Settings settings = getSettings();
		if (settings.contains(GRANULARITY)) {
			this.granularity = settings.getDouble(GRANULARITY);
		} else {
			this.granularity = 1.0;
		}

		init();
	}

	@Override
	protected void init() {
		super.init();
		this.globalMeetingTime = new MeetingEntry(0, granularity);

	}

	@Override
	public void hostsConnected(DTNHost host1, DTNHost host2) {

		globalMeetingTime.hostsConnected(host1, host2);

	}

	@Override
	public void hostsDisconnected(DTNHost host1, DTNHost host2) {

		globalMeetingTime.hostsDisconnected(host1, host2);

	}

	@Override
	public void done() {
		double avgmeetingtime = globalMeetingTime.getAvgMeetingTime();

		write("avg meeting time =  " + avgmeetingtime);

		super.done();
	}

	class MeetingEntry {

		protected HashMap<ConnectionInfo, ConnectionInfo> connections;
		private HashSet<String> firstconnections;
		private Vector<Integer> nrofContacts;

		/** How many seconds are grouped in one group */
		protected double granularity = 1.0;

		public int LastFromID;
		public int LastToID;

		// private double timestamp;
		private int weight;
		private double avgMeetingTime;

		// private boolean dummy;

		/**
		 * Constructor. Creates a meeting time entry
		 * 
		 * @param meetingTime
		 *            The actual meeting time
		 */
		public MeetingEntry(double meetingTime, double granularity) {
			this.weight = 0;
			// this.timestamp = 0;
			this.avgMeetingTime = 0;
			// this.dummy = false;
			this.connections = new HashMap<ConnectionInfo, ConnectionInfo>();
			this.firstconnections = new HashSet<String>();
			this.nrofContacts = new Vector<Integer>();
			this.granularity = granularity;
			// update(meetingTime, SimClock.getTime(),0,0);
		}

		/**
		 * Updates the average meeting time by recomputing it with the current
		 * meeting time of the met host
		 * 
		 * @param meetingTime
		 *            The current meeting time
		 * 
		 */
		public void update(double meetingTime, int from, int to) {
			avgMeetingTime = (((weight * avgMeetingTime) + meetingTime) / (weight + 1));
			// print();
			weight++;

		}

		/**
		 * Returns the average meeting time of an entry
		 * 
		 * @return the average meeting time
		 */
		public double getAvgMeetingTime() {

			return avgMeetingTime;
		}

		public int getWeight() {
			return weight;
		}

		/**
		 * Print the entry to the command line
		 */
		public void print() {
			System.out.println("\t\t\t\t" + avgMeetingTime + "@" + " "
					+ getSimTime());
		}

		public void hostsConnected(DTNHost host1, DTNHost host2) {
			ConnectionInfo ci = this.removeConnection(host1, host2);
			if (ci != null) { // connected again

				ci.connectionEnd();
				// increaseTimeCount(ci.getConnectionTime());
				update(ci.getConnectionTime(), host1.getAddress(),
						host2.getAddress());
			}
		}

		public void hostsDisconnected(DTNHost host1, DTNHost host2) {

			// start counting time to next connection

			String A = host1.getAddress() + "," + host2.getAddress();
			String B = host2.getAddress() + "," + host1.getAddress();

			if (firstconnections.contains(A) || firstconnections.contains(B)) {
				// System.out.println(A);
				return;
			}

			firstconnections.add(A);
			firstconnections.add(B);
			this.addConnection(host1, host2);
		}

		protected void addConnection(DTNHost host1, DTNHost host2) {
			ConnectionInfo ci = new ConnectionInfo(host1, host2);

			assert !connections.containsKey(ci) : "Already contained "
					+ " a connection of " + host1 + " and " + host2;

			connections.put(ci, ci);
		}

		protected ConnectionInfo removeConnection(DTNHost host1, DTNHost host2) {
			ConnectionInfo ci = new ConnectionInfo(host1, host2);
			ci = connections.remove(ci);
			return ci;
		}

		/**
		 * Increases the amount of times a certain time value has been seen.
		 * 
		 * @param time
		 *            The time value that was seen
		 */
		protected void increaseTimeCount(double time) {
			int index = (int) (time / this.granularity);

			if (index >= this.nrofContacts.size()) {
				/*
				 * if biggest index so far, fill array with nulls up to index+2
				 * to keep the last time count always zero
				 */
				this.nrofContacts.setSize(index + 2);
			}

			Integer curValue = this.nrofContacts.get(index);
			if (curValue == null) { // no value found -> put the first
				this.nrofContacts.set(index, 1);
			} else { // value found -> increase the number by one
				this.nrofContacts.set(index, curValue + 1);
			}
		}

		protected double getSimTime() {
			return SimClock.getTime();
		}

		protected class ConnectionInfo {
			private double startTime;
			private double endTime;
			private DTNHost h1;
			private DTNHost h2;

			public ConnectionInfo(DTNHost h1, DTNHost h2) {
				this.h1 = h1;
				this.h2 = h2;
				this.startTime = getSimTime();
				this.endTime = -1;
			}

			/**
			 * Should be called when the connection ended to record the time.
			 * Otherwise {@link #getConnectionTime()} will use end time as the
			 * time of the request.
			 */
			public void connectionEnd() {
				this.endTime = getSimTime();
			}

			/**
			 * Returns the time that passed between creation of this info and
			 * call to {@link #connectionEnd()}. Unless connectionEnd() is
			 * called, the difference between start time and current sim time is
			 * returned.
			 * 
			 * @return The amount of simulated seconds passed between creation
			 *         of this info and calling connectionEnd()
			 */
			public double getConnectionTime() {
				if (this.endTime == -1) {
					return getSimTime() - this.startTime;
				} else {
					return this.endTime - this.startTime;
				}
			}

			/**
			 * Returns true if the other connection info contains the same
			 * hosts.
			 */
			public boolean equals(Object other) {
				if (!(other instanceof ConnectionInfo)) {
					return false;
				}

				ConnectionInfo ci = (ConnectionInfo) other;

				if ((h1 == ci.h1 && h2 == ci.h2)) {
					return true;
				} else if ((h1 == ci.h2 && h2 == ci.h1)) {
					// bidirectional connection the other way
					return true;
				}
				return false;
			}

			/**
			 * Returns the same hash for ConnectionInfos that have the same two
			 * hosts.
			 * 
			 * @return Hash code
			 */
			public int hashCode() {
				String hostString;

				if (this.h1.compareTo(this.h2) < 0) {
					hostString = h1.toString() + "-" + h2.toString();
				} else {
					hostString = h2.toString() + "-" + h1.toString();
				}

				return hostString.hashCode();
			}

			/**
			 * Returns a string representation of the info object
			 * 
			 * @return a string representation of the info object
			 */
			public String toString() {
				return this.h1 + "<->" + this.h2 + " [" + this.startTime + "-"
						+ (this.endTime > 0 ? this.endTime : "n/a") + "]";
			}
		}

	}

}
