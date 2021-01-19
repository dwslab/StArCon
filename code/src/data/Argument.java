package data;

import utils.Stance;

public class Argument {
	protected String claim, topic;
	protected Stance stance;

	public Argument(String topic, String claim, String stance) {
		this.topic = topic;
		this.claim = claim;
		if (stance.toLowerCase().startsWith("p")) {
			this.stance = Stance.PRO;
		} else if (stance.toLowerCase().startsWith("c")) {
			this.stance = Stance.CON;
		} else {
			System.err.println("Unknown Stance: " + stance);
			System.exit(0);
		}
	}
	
	public Argument(String topic, String claim) {
		this.topic = topic;
		this.claim = claim;
		this.stance = Stance.UNKNOWN;
	}
	
	public Argument(Argument arg) {
		this.topic = arg.topic;
		this.claim = arg.claim;
		this.stance = arg.stance;
	}

	public String getClaim() {
		return claim;
	}

	public String getTopic() {
		return topic;
	}

	public Stance getStance() {
		return stance;
	}

	public String get(Role role) {
		if (role == Role.CLAIM) {
			return claim;
		} else {
			return topic;
		}
	}

	public boolean equals(Object o) {
		if (!(o instanceof Argument)) {
			return false;
		}
		Argument arg = (Argument) o;

		return (arg.claim == claim) && (arg.topic == topic);
	}

	public int hashCode() {
		return claim.hashCode() + 2 * topic.hashCode();
	}

	public enum Role {
		TOPIC, CLAIM;
	}
	
	public String toString() {
		return "[" + topic + "] [" + claim + "] <" + stance + ">";
	}
}
