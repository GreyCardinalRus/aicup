import model.*;

//import static java.lang.StrictMath.PI;

public final class MyStrategy implements Strategy {
	static boolean IsDebug = true;
	int numStrategy = 0;
	long tid = 0;
	Tank EnemyTank = null;
	Bonus nearbonus = null;
	Player EnemyPlayer = null;

	@Override
	public TankType selectTank(int tankIndex, int teamSize) {
		// if (1 == teamSize)
		// return TankType.TANK_DESTROYER;//HEAVY;
		return TankType.MEDIUM;
	}

	@Override
	public void move(Tank self, World world, Move move) {

		getNearBonus(self, world);

		getEnemy(self, world, move);

		double angleToBonus = 3, angleToEnemy = 3;
		if (!(nearbonus == null))
			angleToBonus = self.getAngleTo(nearbonus);

		if (EnemyTank != null) {
			angleToEnemy = self.getTurretAngleTo(EnemyTank);
			myFire(self, world, move, EnemyTank);
		} else {
			if (IsDebug)
				System.out.println("lost Target:");
			move.setTurretTurn(1);
		}

		myMove(self, move, angleToBonus, angleToEnemy);

	}

	private void myMove(Tank self, Move move, double angleToBonus,
			double angleToEnemy) {
		if (self.getRemainingReloadingTime() < 5
				&& (angleToEnemy > 0.1 || angleToEnemy < -0.1)) {// Ready to
																	// fire
			if (angleToEnemy > 0.1) {
				move.setLeftTrackPower(1 * self.getEngineRearPowerFactor());
				move.setRightTrackPower(-1);
			} else if (angleToBonus < -0.1) {
				move.setLeftTrackPower(-1);
				move.setRightTrackPower(1 * self.getEngineRearPowerFactor());
			} else {
				move.setLeftTrackPower(0);
				move.setRightTrackPower(0);
			}
		} else {
			if ((nearbonus == null)) {
				move.setLeftTrackPower(-1);
				move.setRightTrackPower(-1);
			} else if (angleToBonus > 2.5 || angleToBonus < -2.5) { // reverce
				// if (((self.getAngle() > 2.0 || self.getAngle() < -2.0)
				// && self.getY() > (0.5 * (self.getWidth() + self
				// .getHeight()))
				// || (self.getAngle() > -1.0 && self.getAngle() < 1.0)
				// && self.getY() < (world.getHeight() - 0.5 * (self
				// .getWidth() + self.getHeight()))
				// || (self.getAngle() > 1.0 && self.getAngle() < 2.0)
				// && self.getX() > (0.5 * (self.getWidth() + self
				// .getHeight())) || (self.getAngle() > -2.0 && self
				// .getAngle() < -1.0)
				// && self.getX() < (world.getWidth() - 0.5 * (self
				// .getWidth() + self.getHeight()))))
				{
					move.setLeftTrackPower(-1);
					move.setRightTrackPower(-1);
					// } else {
					// if (IsDebug)
					// System.out.println("Reverse(" + world.getTick() + ")"
					// + "self.getAngle()=" + (int) self.getAngle()
					// + " self.getY()=" + (int) self.getY()
					// + " world.getHeight()=" + world.getHeight());
				}
			} else if (angleToBonus > 0.5 && angleToBonus < 1.5
					|| angleToBonus > -2.5 && angleToBonus < -1.5) {
				move.setLeftTrackPower(1 * self.getEngineRearPowerFactor());
				move.setRightTrackPower(-1);
			} else if (angleToBonus < -0.5 && angleToBonus > -1.5
					|| angleToBonus > 1.5 && angleToBonus < 2.5) {
				move.setLeftTrackPower(-1);
				move.setRightTrackPower(1 * self.getEngineRearPowerFactor());
			} else // forward if (moveAngel > -0.5)
			{
				// if (((self.getAngle() > -2.0 && self.getAngle() < -1.0)
				// && self.getY() < (world.getHeight() - 0.5 * (self
				// .getWidth() + self.getHeight()))
				// || (self.getAngle() > 1.0 && self.getAngle() < 2.0)
				// && self.getY() > 0.5 * (self.getWidth() + self
				// .getHeight())
				// || (self.getAngle() > -1.0 && self.getAngle() < 1.0)
				// && self.getX() < (world.getWidth() - 0.5 * (self
				// .getWidth() + self.getHeight())) || (self
				// .getAngle() < -2.0 || self.getAngle() > 2.0)
				// && self.getX() > (0.5 * (self.getWidth() + self
				// .getHeight()))))
				{
					move.setLeftTrackPower(1);
					move.setRightTrackPower(1);
					// } else {
					// if (IsDebug)
					// System.out.println("Forward(" + world.getTick() + ")"
					// + "self.getAngle()=" + (int) self.getAngle()
					// + " self.getY()=" + (int) self.getY()
					// + " world.getHeight()=" + world.getHeight()
					// + " self.getX()=" + (int) self.getX()
					// + " world.getWidth()=" + world.getWidth());
				}
			}
		}
	}

	private void getEnemy(Tank self, World world, Move move) {
		Tank[] tanks = world.getTanks();
		Player[] players = world.getPlayers();
		int i;
		int j;
		EnemyPlayer = null;
		if (self.getRemainingReloadingTime() == 0) {
			for (i = 0; i < tanks.length; i++)
				if (!tanks[i].isTeammate() // Enemy!!
						&& tanks[i].getCrewHealth() > 0 // live!!!
						&& tanks[i].getHullDurability() > 0 // live!!!
						&& Math.abs(self.getTurretAngleTo(tanks[i])) < 20 / self
								.getDistanceTo(tanks[i])
						&& self.getDistanceTo(tanks[i]) < 100
						&& myFire(self, world, move, tanks[i])) {
					EnemyTank = tanks[i];
					break;
				}

		}

		for (i = 0; i < tanks.length; i++)
			if (!tanks[i].isTeammate() // Enemy!!
					&& tanks[i].getCrewHealth() > 0 // live!!!
					&& tanks[i].getHullDurability() > 0 // live!!!
			)
				for (j = 0; j < players.length; j++) {
					if (tanks[i].getPlayerName().equals(players[j].getName())
							&& (EnemyPlayer == null || players[j].getScore() > EnemyPlayer
									.getScore())) {
						EnemyPlayer = players[j];
						EnemyTank = tanks[i];
					}

				}
	}

	private void getNearBonus(Tank self, World world) {
		Bonus[] bonuses = world.getBonuses();
		int i;
		Bonus nearMK = null;
		Bonus nearRK = null;
		Bonus nearAC = null;
		nearbonus = null;

		double minDistance = 10000;
		for (i = 0; i < bonuses.length; i++) {
			if (minDistance > self.getDistanceTo(bonuses[i])
					&& bonuses[i].getType() == BonusType.MEDIKIT) {
				nearMK = bonuses[i];
				minDistance = self.getDistanceTo(bonuses[i]);
			}
		}
		minDistance = 10000;

		for (i = 0; i < bonuses.length; i++) {
			if (minDistance > self.getDistanceTo(bonuses[i])
					&& bonuses[i].getType() == BonusType.REPAIR_KIT) {
				nearRK = bonuses[i];
				minDistance = self.getDistanceTo(bonuses[i]);
			}
		}
		minDistance = 10000;

		for (i = 0; i < bonuses.length; i++) {
			if (minDistance > self.getDistanceTo(bonuses[i])
					&& bonuses[i].getType() == BonusType.AMMO_CRATE) {
				nearAC = bonuses[i];
				minDistance = self.getDistanceTo(bonuses[i]);
			}
		}
		if (nearMK != null) {
			nearbonus = nearMK;
			minDistance = self.getDistanceTo(nearbonus);
		}
		if (nearRK != null) {
			if (nearbonus == null || self.getCrewHealth() > 50
					&& self.getDistanceTo(nearRK) < (minDistance / 3))
				nearbonus = nearRK;
			minDistance = self.getDistanceTo(nearbonus);
		}
		if (nearAC != null) {
			if (nearbonus == null
					|| self.getHullDurability() > self.getHullMaxDurability() * 0.5
					&& self.getCrewHealth() > 50
					&& self.getDistanceTo(nearAC) < (minDistance / 3))
				nearbonus = nearAC;
			minDistance = self.getDistanceTo(nearbonus);
		}
	}

	boolean myFire(Tank self, World world, Move move, Tank targetTank) {
		Tank[] tanks = world.getTanks();
		Bonus[] bonuses = world.getBonuses();
		Shell[] shells = world.getShells();

		int i;
		double minAngle = self.getTurretAngleTo(targetTank);
		move.setTurretTurn(minAngle);

		if ((self.getRemainingReloadingTime() > 0)||Math.abs(minAngle) > 20 / self
				.getDistanceTo(targetTank))
						return false;
		double dist = self.getDistanceTo(targetTank);

		for (i = 0; i < bonuses.length; i++) {
			if ((Math.abs(minAngle - self.getTurretAngleTo(bonuses[i])) < 0.1)
					&& dist > self.getDistanceTo(bonuses[i]))
				return false;
		}
		for (i = 0; i < shells.length; i++) {
			if ((Math.abs(minAngle - self.getTurretAngleTo(shells[i])) < 0.1)
					&& dist > self.getDistanceTo(shells[i]))
				return false;
		}
		for (i = 0; i < tanks.length; i++) {
			if ((Math.abs(minAngle - self.getTurretAngleTo(tanks[i])) < 0.1)
					&& dist > self.getDistanceTo(tanks[i])
					&& (tanks[i].isTeammate() // Friend!!
							|| tanks[i].getCrewHealth() == 0 // Dead!!!
					|| tanks[i].getHullDurability() == 0) // Dead!!!
			)
				return false;
		}
		if (100 < dist&&Math.abs(targetTank.getAngleTo(self)-minAngle)>0.1&&(targetTank.getSpeedX()+targetTank.getSpeedY())>100) {
			return false;
		}
		if (100 < dist)
			move.setFireType(FireType.REGULAR);
		else
			move.setFireType(FireType.PREMIUM_PREFERRED);
		return true;

	}

}
