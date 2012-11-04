import model.*;

//import static java.lang.StrictMath.PI;

public final class MyStrategy implements Strategy {
	static boolean IsDebug = false;
	int numStrategy = 0;
	long tid = 0;
	Tank EnemyTank = null;
	Bonus nearbonus = null;
	Player EnemyPlayer = null;
	boolean oneEnimy = false;

	@Override
	public TankType selectTank(int tankIndex, int teamSize) {
		// if (1 == teamSize)
		// return TankType.TANK_DESTROYER;//HEAVY;
		// return TankType.TANK_DESTROYER;
		// return TankType.HEAVY;
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

		myMove(self, world, move, angleToBonus, angleToEnemy);

	}

	private void myMoveTank(Tank self, World world, Move move, double lp,
			double rp) {
		if ((lp > 0 && rp < 0) || (lp < 0 && rp > 0) || lp == 0 || rp == 0) {
			move.setLeftTrackPower(lp);
			move.setRightTrackPower(rp);
		} else {

			if (self.getSpeedX() > 0
					&& self.getX() > (world.getWidth() - 0.5 * (self.getWidth() + self
							.getHeight()))) {// Повернемся
												// к миру
												// передом
				if (self.getAngle() > 0 && (lp > 0 && rp > 0)) {
					move.setLeftTrackPower(1);
					move.setRightTrackPower(-1);
				} else {
					move.setLeftTrackPower(-1);
					move.setRightTrackPower(1);

				}
			} else if (self.getSpeedX() < 0
					&& self.getX() < (0.5 * (self.getWidth() + self.getHeight()))) {// Повернемся
																					// к
																					// миру
																					// передом
				if (self.getAngle() > 0 && (lp > 0 && rp > 0)) {
					move.setLeftTrackPower(-1);
					move.setRightTrackPower(1);
				} else {
					move.setLeftTrackPower(1);
					move.setRightTrackPower(-1);

				}
			} else if (self.getSpeedY() > 0
					&& self.getY() > (world.getHeight() - 0.5 * (self
							.getWidth() + self.getHeight()))) {// Повернемся
																// к миру
																// передом
				if (self.getAngle() > 1.5 && (lp > 0 && rp > 0)) {
					move.setLeftTrackPower(1);
					move.setRightTrackPower(-1);
				} else {
					move.setLeftTrackPower(-1);
					move.setRightTrackPower(1);

				}
			} else if (self.getSpeedY() < 0
					&& self.getY() < (0.5 * (self.getWidth() + self.getHeight()))) {// Повернемся
																					// к
																					// миру
																					// передом
				if (self.getAngle() > -1.5 && (lp > 0 && rp > 0)) {
					move.setLeftTrackPower(1);
					move.setRightTrackPower(-1);
				} else {
					move.setLeftTrackPower(-1);
					move.setRightTrackPower(1);

				}
			} else {
				if (lp > 0 && rp > 0) {
					move.setLeftTrackPower(1);
					move.setRightTrackPower(1);
				} else {
					move.setLeftTrackPower(-1);
					move.setRightTrackPower(-1);
				}
			}

			// } else {
			// // if (((self.getAngle() > 2.0 || self.getAngle() < -2.0)
			// // && self.getY() > (0.5 * (self.getWidth() + self
			// // .getHeight()))
			// // || (self.getAngle() > -1.0 && self.getAngle() < 1.0)
			// // && self.getY() < (world.getHeight() - 0.5 * (self
			// // .getWidth() + self.getHeight()))
			// // || (self.getAngle() > 1.0 && self.getAngle() < 2.0)
			// // && self.getX() > (0.5 * (self.getWidth() + self
			// // .getHeight())) || (self.getAngle() > -2.0 && self
			// // .getAngle() < -1.0)
			// // && self.getX() < (world.getWidth() - 0.5 * (self
			// // .getWidth() + self.getHeight()))))
			// {
			// move.setLeftTrackPower(-1);
			// move.setRightTrackPower(-1);
			// // } else {
			// // if (IsDebug)
			// // System.out.println("Reverse(" + world.getTick() + ")"
			// // + "self.getAngle()=" + (int) self.getAngle()
			// // + " self.getY()=" + (int) self.getY()
			// // + " world.getHeight()=" + world.getHeight());
			// }

		}

	}

	private void myMove(Tank self, World world, Move move, double angleToBonus,
			double angleToEnemy) {
		Shell[] shells = world.getShells();
		int i;
		for (i = 0; i < shells.length; i++) {
			if ((Math.abs(shells[i].getAngleTo(self)) < 5 / self
					.getDistanceTo(shells[i]))
					&& 50 < self.getDistanceTo(shells[i])) {
				if (Math.abs(self.getAngleTo(shells[i])) < 1) { // turn
					if (self.getAngleTo(shells[i]) < 0) {
						move.setLeftTrackPower(1 * self
								.getEngineRearPowerFactor());
						move.setRightTrackPower(-1);
						if (IsDebug)
							System.out.println(world.getTick()
									+ " escape-rotate R "
									+ (int) self.getDistanceTo(shells[i]));
					} else {
						move.setLeftTrackPower(-1);
						move.setRightTrackPower(1 * self
								.getEngineRearPowerFactor());
						if (IsDebug)
							System.out.println(world.getTick()
									+ " escape-rotate L "
									+ (int) self.getDistanceTo(shells[i]));
					}
				} else {// move
					if (move.getRightTrackPower() < 0
							&& move.getLeftTrackPower() < 0) {
						move.setLeftTrackPower(-1);
						move.setRightTrackPower(-1);
						if (IsDebug)
							System.out.println(world.getTick()
									+ " escape-rotate forward "
									+ (int) self.getDistanceTo(shells[i]));
					} else {
						move.setLeftTrackPower(1);
						move.setRightTrackPower(1);
						if (IsDebug)
							System.out.println(world.getTick()
									+ " escape-rotate Reverse "
									+ (int) self.getDistanceTo(shells[i]));
					}
				}
				return;
			}
		}

		if (self.getRemainingReloadingTime() < 3
				&& (angleToEnemy > 0.1 || angleToEnemy < -0.1)) {// Ready to
																	// fire
			if (angleToEnemy > 0.1) {
				move.setLeftTrackPower(1 * self.getEngineRearPowerFactor());
				move.setRightTrackPower(-1);
			} else if (angleToEnemy < -0.1) {
				move.setLeftTrackPower(-1);
				move.setRightTrackPower(1 * self.getEngineRearPowerFactor());
			} else {
				myMoveTank(self, world, move, -1, -1);
			}
		} else {
			if ((nearbonus == null)) {
				myMoveTank(self, world, move, -1, -1);
			} else if (angleToBonus > 2.5 || angleToBonus < -2.5) {
				// reverse
				myMoveTank(self, world, move, -1, -1);
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
				myMoveTank(self, world, move, 1, 1);
			}
		}
	}

	private void getEnemy(Tank self, World world, Move move) {
		Tank[] tanks = world.getTanks();
		Player[] players = world.getPlayers();
		int i;
		int j;
		EnemyPlayer = null;
		// oneEnimy = true;
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

	private boolean myFire(Tank self, World world, Move move, Tank targetTank) {
		Tank[] tanks = world.getTanks();
		Bonus[] bonuses = world.getBonuses();
		Obstacle[] obstacles = world.getObstacles();

		int i;
		double minAngle = self.getTurretAngleTo(targetTank);
		double dist = self.getDistanceTo(targetTank);
		move.setTurretTurn(minAngle);

		if ((self.getRemainingReloadingTime() > 0)
				|| Math.abs(minAngle) > 20 / dist)
			return false;

		for (i = 0; i < bonuses.length; i++) {
			if ((Math.abs(minAngle - self.getTurretAngleTo(bonuses[i])) < 1 / self
					.getDistanceTo(bonuses[i]))
					&& dist > self.getDistanceTo(bonuses[i]))
				return false;
		}
		for (i = 0; i < obstacles.length; i++) {
			if ((Math.abs(minAngle - self.getTurretAngleTo(obstacles[i])) < 1 / self
					.getDistanceTo(obstacles[i]))
					&& dist > self.getDistanceTo(obstacles[i]))
				return false;
		}
		for (i = 0; i < tanks.length; i++) {
			if ((Math.abs(minAngle - self.getTurretAngleTo(tanks[i])) < 1 / self
					.getDistanceTo(tanks[i]))
					&& dist > self.getDistanceTo(tanks[i])
					&& tanks[i] != self
					&& self.getDistanceTo(tanks[i]) > 0
					&& tanks[i] != targetTank && (tanks[i].isTeammate() // Friend!!
							|| tanks[i].getCrewHealth() == 0 // Dead!!!
					|| tanks[i].getHullDurability() == 0) // Dead!!!
			)
				return false;
		}
		if (100 < dist
				&& Math.abs(targetTank.getAngleTo(self) - minAngle) > 0.1
				&& (targetTank.getSpeedX() + targetTank.getSpeedY()) > 200) {
			return false;
		}
		if (100 < dist)
			move.setFireType(FireType.REGULAR);
		else
			move.setFireType(FireType.PREMIUM_PREFERRED);
		return true;

	}

}
