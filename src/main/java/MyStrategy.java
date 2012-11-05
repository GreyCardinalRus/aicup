import model.*;

//import static java.lang.StrictMath.PI;

public final class MyStrategy implements Strategy {
	static boolean IsDebug = true;
	static boolean isChicken = false;
	int numStrategy = 0;
	long tid = 0;
	Tank EnemyTank = null;
	Bonus nearbonus = null;
	Player EnemyPlayer = null;
	int countEnimy = 0;

	@Override
	public TankType selectTank(int tankIndex, int teamSize) {
		// if (1 == teamSize)
		// return TankType.TANK_DESTROYER;//HEAVY;
		//return TankType.TANK_DESTROYER;
		// return TankType.HEAVY;
		 return TankType.MEDIUM;
	}

	@Override
	public void move(Tank self, World world, Move move) {

//		if(self.getAngle()>1 && self.getAngle()<1.1)
//		{
//			move.setLeftTrackPower(1);
//			move.setRightTrackPower(-1);
//	
//		}
//		else
//		{
//			move.setLeftTrackPower(1);
//			move.setRightTrackPower(1);
//	
//		}
		getEnemy(self, world, move);

		getNearBonus(self, world);

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
//		if (IsDebug&&((self.getY() > (world.getHeight() - 0.5 * (self
//				.getWidth() + self.getHeight()))))||(self.getY() < (0.5 * (self.getWidth() + self
//						.getHeight()))))
//			System.out
//					.println(world.getTick()
//							+ " self.getAngle():"
//							+ Math.round(10 * self.getAngle())
//							/ 10
//							+ " self.getX()="
//							+ Math.round(self.getX())
//							+ " world.getWidth()= "
//							+ (world.getHeight() )
//							+ " self.getY()="
//							+ Math.round(self.getY())
//							+ " world.getHeight()= "
//							+ (world.getHeight() )
//							+ " RE=" + move.getRightTrackPower()
//							+ " LE=" + move.getLeftTrackPower());

	}

	private void myMoveTank(Tank self, World world, Move move, double lp,
			double rp) {
		move.setLeftTrackPower((lp>0&&rp<0?lp*self.getEngineRearPowerFactor():lp));
		move.setRightTrackPower((rp>0&&lp<0?rp*self.getEngineRearPowerFactor():rp));
		Tank[] tanks = world.getTanks();
		int i;
		for (i = 0; i < tanks.length; i++)
			if (!tanks[i].isTeammate() // Enemy!!
					&& tanks[i].getCrewHealth() > 0 // live!!!
					&& tanks[i].getHullDurability() > 0 // live!!!
			) {
				myFire(self, world, move, tanks[i]);
			}

	}

	private void myMove(Tank self, World world, Move move, double angleToBonus,
			double angleToEnemy) {
		Shell[] shells = world.getShells();
		int i;
		move.setTurretTurn(angleToEnemy);
		// Escape
		if(isChicken)
		for (i = 0; i < shells.length; i++) {
			if ((Math.abs(shells[i].getAngleTo(self)) < 5 / self
					.getDistanceTo(shells[i]))
					&& 50 < self.getDistanceTo(shells[i])) {
				if (Math.abs(self.getAngleTo(shells[i])) < 1) { // turn
					if (self.getAngleTo(shells[i]) < 0) {
						myMoveTank(self, world, move, 1, -1);
						if (IsDebug)
							System.out.println(world.getTick()
									+ " escape-rotate R "
									+ (int) self.getDistanceTo(shells[i]));
					} else {
						myMoveTank(self, world, move, -1, 1);
						if (IsDebug)
							System.out.println(world.getTick()
									+ " escape-rotate L "
									+ (int) self.getDistanceTo(shells[i]));
					}
				} else {// move
					if (move.getRightTrackPower() < 0
							&& move.getLeftTrackPower() < 0) {
						myMoveTank(self, world, move, -1, -1);
						if (IsDebug)
							System.out.println(world.getTick()
									+ " escape-rotate forward "
									+ (int) self.getDistanceTo(shells[i]));
					} else {
						myMoveTank(self, world, move, 1, 1);
						if (IsDebug)
							System.out.println(world.getTick()
									+ " escape-rotate Reverse "
									+ (int) self.getDistanceTo(shells[i]));
					}
				}
				return;
			}
		}

		// Feady to Fire- turn to Enemy
		if (self.getRemainingReloadingTime() < (self.getType() == TankType.MEDIUM ? 3
				: self.getType() == TankType.HEAVY ? 6 : 100)
				&& (angleToEnemy > 0.1 || angleToEnemy < -0.1)) {// Ready to
																	// fire
			if (angleToEnemy > 0.1) {
				myMoveTank(self, world, move, 1, -1);
			} else if (angleToEnemy < -0.1) {
				myMoveTank(self, world, move, -1, 1);
			} else {
				myMoveTank(self, world, move, -1, -1);
			}
//		} else if (countEnimy < 2 && isVisible(self, world, EnemyTank)
//				&& (Math.abs(EnemyTank.getTurretAngleTo(self)) < 0.1)
//				&& (self.getAngleTo(EnemyTank) > 0.5 || self
//						.getAngleTo(EnemyTank) < -0.5)) {
//			if (self.getAngleTo(EnemyTank) > 0.5)
//				myMoveTank(self, world, move, 1, -1);
//			else
//				myMoveTank(self, world, move, -1, 1);
//			move.setTurretTurn(angleToEnemy);
		} else { // Move to bonus
			if (nearbonus == null) {
				// Повернемся к миру передом
				if (self.getX() > (world.getWidth() - 0.5 * (self.getWidth() + self
						.getHeight()))) { // правая стена
					if (self.getAngle() <-2 )
						myMoveTank(self, world, move, 1, 1);
					else if (self.getAngle()  < -1.5)
						myMoveTank(self, world, move, 0, 1);
					else if (self.getAngle()  < 0)
						myMoveTank(self, world, move, -1, 0 );
					else if (self.getAngle()  < 1.5)
						myMoveTank(self, world, move, 0, -1 );
					else if (self.getAngle()  < 2)
						myMoveTank(self, world, move, 1, 0);
					else
						myMoveTank(self, world, move, 1, 1);
//					if (IsDebug)
//						System.out
//								.println(world.getTick()
//										+ " self.getAngle():"
//										+ Math.round(10 * self.getAngle())
//										/ 10
//										+ " self.getX()="
//										+ Math.round(self.getX())
//										+ " world.getWidth()= "
//										+ (world.getWidth() - 0.5 * (self
//												.getWidth() + self.getHeight()))
//										+ " RE=" + move.getRightTrackPower()
//										+ " LE=" + move.getLeftTrackPower());
					// return;
				} else if (self.getX() < (0.5 * (self.getWidth() + self
						.getHeight()))) { // левая стена
					if (self.getAngle() <-1.5 )
						myMoveTank(self, world, move, 0, -1);
					else if (self.getAngle()  < -1)
						myMoveTank(self, world, move, 1, 0);
					else if (self.getAngle()  < 1)
						myMoveTank(self, world, move, 1, 1 );
					else if (self.getAngle()  < 1.5)
						myMoveTank(self, world, move, 0, 1 );
					else
						myMoveTank(self, world, move, -1, 0);
//					if (IsDebug)
//						System.out
//								.println(world.getTick()
//										+ " self.getAngle():"
//										+ Math.round(10 * self.getAngle())
//										/ 10
//										+ " self.getX()="
//										+ Math.round(self.getX())
//										+ " world.getWidth()= "
//										+ (world.getWidth() - 0.5 * (self
//												.getWidth() + self.getHeight()))
//										+ " RE=" + move.getRightTrackPower()
//										+ " LE=" + move.getLeftTrackPower());
//					// return;

				} else if (self.getY() > (world.getHeight() - 0.5 * (self
						.getWidth() + self.getHeight()))) {// нижняя стена
					if (self.getAngle() <-2 )
						myMoveTank(self, world, move, 1, 0);
					else if (self.getAngle()  < -1)
						myMoveTank(self, world, move, 1, 1);
					else if (self.getAngle()  < 0)
						myMoveTank(self, world, move, 0, 1 );
					else if (self.getAngle()  < 1.5)
						myMoveTank(self, world, move, -1, 0 );
					else
						myMoveTank(self, world, move, 0, 1);
//					if (IsDebug)
//						System.out
//								.println(world.getTick()
//										+ " self.getAngle():"
//										+ Math.round(10 * self.getAngle())
//										/ 10
//										+ " self.getY()="
//										+ Math.round(self.getY())
//										+ " world.getHeight()= "
//										+ (world.getHeight() - 0.5 * (self
//												.getHeight() + self.getHeight()))
//										+ " RE=" + move.getRightTrackPower()
//										+ " LE=" + move.getLeftTrackPower());
//					// return;
				} else if (self.getY() < (0.5 * (self.getWidth() + self
						.getHeight()))) { // верхняя стена
					if (self.getAngle() <-1.5 )
						myMoveTank(self, world, move, -1, 0);
					else if (self.getAngle()  < 0)
						myMoveTank(self, world, move, 0, 1);
					else if (self.getAngle()  < 1)
						myMoveTank(self, world, move, 1, 0 );
					else if (self.getAngle()  < 2)
						myMoveTank(self, world, move, 1, 1 );
					else
						myMoveTank(self, world, move, 0, 1);
//					if (IsDebug)
//						System.out
//								.println(world.getTick()
//										+ " self.getAngle():"
//										+ Math.round(10 * self.getAngle())
//										/ 10
//										+ " self.getY()="
//										+ Math.round(self.getY())
//										+ " world.getWidth()= "
//										+ (world.getHeight() - 0.5 * (self
//												.getHeight() + self.getHeight()))
//										+ " RE=" + move.getRightTrackPower()
//										+ " LE=" + move.getLeftTrackPower());
					// return;
				} else
					myMoveTank(self, world, move, -1, -1);
//				// довернем пушку "обратно"
//				if (move.getRightTrackPower() < 0
//						&& move.getLeftTrackPower() > 0)
//					move.setTurretTurn(-5);
//				else if (move.getRightTrackPower() > 0
//						&& move.getLeftTrackPower() < 0)
//					move.setTurretTurn(5);
				// бонус есть -повернемся к нему передом -быстрее доедем!
			} else if (angleToBonus > 2.5 || angleToBonus < -2.5) {
				// reverse
				myMoveTank(self, world, move, -1, -1);
			} else if (((angleToBonus > 0.5 && angleToBonus < 1.5 || angleToBonus > -2.5
					&& angleToBonus < -1.5) && (self.getDistanceTo(nearbonus) < (self
					.getType() == TankType.TANK_DESTROYER ? 50 : 5000)))
					|| (angleToBonus > 0.5 && (self.getDistanceTo(nearbonus) > (self
							.getType() == TankType.TANK_DESTROYER ? 50 : 5000)))) {
				myMoveTank(self, world, move, 1, -1);
				// move.setLeftTrackPower(1 * self.getEngineRearPowerFactor());
				// move.setRightTrackPower(-1);
			} else if (((angleToBonus < -0.5 && angleToBonus > -1.5 || angleToBonus > 1.5
					&& angleToBonus < 2.5) && (self.getDistanceTo(nearbonus) < (self
					.getType() == TankType.TANK_DESTROYER ? 50 : 5000)))
					|| (angleToBonus < -0.5 && (self.getDistanceTo(nearbonus) > (self
							.getType() == TankType.TANK_DESTROYER ? 50 : 5000)))) {
				myMoveTank(self, world, move, -1, 1);
				// move.setLeftTrackPower(-1);
				// move.setRightTrackPower(1 * self.getEngineRearPowerFactor());
			} else // forward if (moveAngel > -0.5)
			{
				myMoveTank(self, world, move, 1, 1);
			}
		}
	}

	private void getEnemy(Tank self, World world, Move move) {
		Tank[] tanks = world.getTanks();
		Player[] players = world.getPlayers();
		// Obstacle[] obstacles = world.getObstacles();
		int i;
		int j;
		EnemyPlayer = null;
		EnemyTank = null;
		countEnimy = 0;
		// Tank EnimyMy=null;
		for (i = 0; i < tanks.length; i++)
			if (!tanks[i].isTeammate() // Enemy!!
					&& tanks[i].getCrewHealth() > 0 // live!!!
					&& tanks[i].getHullDurability() > 0 // live!!!
			) {
				if (Math.abs(tanks[i].getTurretAngleTo(self)) < 0.1)
					EnemyTank = tanks[i];
				countEnimy++;
			}

		if (self.getRemainingReloadingTime() == 0) {
			for (i = 0; i < tanks.length; i++)
				if (!tanks[i].isTeammate() // Enemy!!
						&& tanks[i].getCrewHealth() > 0 // live!!!
						&& tanks[i].getHullDurability() > 0 // live!!!
						// && isVisible(self, world, tanks[i])
						&& Math.abs(self.getTurretAngleTo(tanks[i])) < 10 / self
								.getDistanceTo(tanks[i])
						&& self.getDistanceTo(tanks[i]) > 0
						&& self.getDistanceTo(tanks[i]) < 300
						&& myFire(self, world, move, tanks[i])) {
					EnemyTank = tanks[i];
					// break;
				}

		}
		double nearAngle = 3;
		if (EnemyTank == null)
			for (i = 0; i < tanks.length; i++)
				if (!tanks[i].isTeammate() // Enemy!!
						&& tanks[i].getCrewHealth() > 0 // live!!!
						&& tanks[i].getHullDurability() > 0 // live!!!
						&& isVisible(self, world, tanks[i])
				// && myFire(self, world, move, tanks[i])
				)
					if (countEnimy < 3) {
						for (j = 0; j < players.length; j++) {
							if (tanks[i].getPlayerName().equals(
									players[j].getName())
									&& (EnemyPlayer == null || players[j]
											.getScore() > EnemyPlayer
											.getScore())) {
								EnemyPlayer = players[j];
								EnemyTank = tanks[i];
							}

						}
					} else {
						if (self.getTurretAngleTo(tanks[i]) < nearAngle) {
							nearAngle = self.getTurretAngleTo(tanks[i]);
							EnemyTank = tanks[i];
						}

					}
		if (EnemyTank == null)// not found visible...
			for (i = 0; i < tanks.length; i++)
				if (!tanks[i].isTeammate() // Enemy!!
						&& tanks[i].getCrewHealth() > 0 // live!!!
						&& tanks[i].getHullDurability() > 0 // live!!!
				// && isVisible(self, world, tanks[i])
				// && myFire(self, world, move, tanks[i])
				)
					if (countEnimy < 3) {
						for (j = 0; j < players.length; j++) {
							if (tanks[i].getPlayerName().equals(
									players[j].getName())
									&& (EnemyPlayer == null || players[j]
											.getScore() > EnemyPlayer
											.getScore())) {
								EnemyPlayer = players[j];
								EnemyTank = tanks[i];
							}

						}
					} else {
						if (self.getTurretAngleTo(tanks[i]) < nearAngle) {
							nearAngle = self.getTurretAngleTo(tanks[i]);
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
		int maxDistance = (int) (world.getHeight() / 2);
		if (countEnimy < 3)
			maxDistance *= 3;
		double minDistance = 10000;
		for (i = 0; i < bonuses.length; i++) {
			if (minDistance > self.getDistanceTo(bonuses[i])
					&& bonuses[i].getType() == BonusType.MEDIKIT
					&& self.getDistanceTo(bonuses[i]) < maxDistance
					&& isVisible(self, world, bonuses[i])) {
				nearMK = bonuses[i];
				minDistance = self.getDistanceTo(bonuses[i]);
			}
		}
		minDistance = 10000;

		for (i = 0; i < bonuses.length; i++) {
			if (minDistance > self.getDistanceTo(bonuses[i])
					&& bonuses[i].getType() == BonusType.REPAIR_KIT
					&& self.getDistanceTo(bonuses[i]) < maxDistance
					&& isVisible(self, world, bonuses[i])) {
				nearRK = bonuses[i];
				minDistance = self.getDistanceTo(bonuses[i]);
			}
		}
		minDistance = 10000;

		for (i = 0; i < bonuses.length; i++) {
			if (minDistance > self.getDistanceTo(bonuses[i])
					&& bonuses[i].getType() == BonusType.AMMO_CRATE
					&& self.getDistanceTo(bonuses[i]) < maxDistance
					&& isVisible(self, world, bonuses[i])) {
				nearAC = bonuses[i];
				minDistance = self.getDistanceTo(bonuses[i]);
			}
		}
		if (nearMK != null && self.getCrewHealth() < 100) {
			nearbonus = nearMK;
			minDistance = self.getDistanceTo(nearbonus);
		}
		if (nearRK != null&& ((self.getHullDurability() < self.getHullMaxDurability())
					&& (nearbonus == null || self.getCrewHealth() > 80
							&& self.getDistanceTo(nearRK) < (minDistance / 3)))){
				nearbonus = nearRK;
				minDistance = self.getDistanceTo(nearbonus);{
		}
		if ((nearAC != null) &&(nearbonus == null
					|| self.getHullDurability() > self.getHullMaxDurability() * 0.8
					&& self.getCrewHealth() > 50
					&& self.getDistanceTo(nearAC) < (minDistance / 3))){
				nearbonus = nearAC;
			minDistance = self.getDistanceTo(nearbonus);}
		}
		if(nearbonus == null&&nearMK != null)nearbonus = nearMK;
	}

	private boolean isVisible(Tank self, World world, Unit targetUnit) {
		Tank[] tanks = world.getTanks();
		Bonus[] bonuses = world.getBonuses();
		Obstacle[] obstacles = world.getObstacles();
		int i;
		double minAngle = self.getTurretAngleTo(targetUnit);
		double dist = self.getDistanceTo(targetUnit);

		for (i = 0; i < bonuses.length; i++) {
			if ((Math.abs(minAngle - self.getTurretAngleTo(bonuses[i])) < 10 / self
					.getDistanceTo(bonuses[i]))
					&& dist > self.getDistanceTo(bonuses[i]))
				return false;
		}
		for (i = 0; i < obstacles.length; i++) {
			if ((Math.abs(minAngle - self.getTurretAngleTo(obstacles[i])) < 10 / self
					.getDistanceTo(obstacles[i]))
					&& dist > self.getDistanceTo(obstacles[i]))
				return false;
		}
		for (i = 0; i < tanks.length; i++) {
			if ((Math.abs(minAngle - self.getTurretAngleTo(tanks[i])) < 100 / self
					.getDistanceTo(tanks[i]))
					&& dist > self.getDistanceTo(tanks[i])
					&& self.getDistanceTo(tanks[i]) > 0)
				return false;
		}

		return true;
	}

	private boolean myFire(Tank self, World world, Move move, Tank targetTank) {
		if (world.getTick() < 10)
			return false;
		Tank[] tanks = world.getTanks();
		Bonus[] bonuses = world.getBonuses();
		Obstacle[] obstacles = world.getObstacles();

		int i;
		double minAngle = self.getTurretAngleTo(targetTank);
		double dist = self.getDistanceTo(targetTank);
		move.setTurretTurn(minAngle);

		if ((self.getRemainingReloadingTime() > 0)
				|| Math.abs(minAngle) > 10 / dist)
			return false;

		for (i = 0; i < bonuses.length; i++) {
			if ((Math.abs(minAngle - self.getTurretAngleTo(bonuses[i])) < 10 / self
					.getDistanceTo(bonuses[i]))
					&& dist > self.getDistanceTo(bonuses[i]))
				return false;
		}
		for (i = 0; i < obstacles.length; i++) {
			if ((Math.abs(minAngle - self.getTurretAngleTo(obstacles[i])) < (obstacles[i]
					.getHeight() + obstacles[i].getWidth())
					* 0.5
					/ self.getDistanceTo(obstacles[i]))
					&& dist > self.getDistanceTo(obstacles[i]))
				return false;
		}
		for (i = 0; i < tanks.length; i++) {
			if ((Math.abs(minAngle - self.getTurretAngleTo(tanks[i])) < 10 / self
					.getDistanceTo(tanks[i]))
					&& dist > self.getDistanceTo(tanks[i])
					&& self.getDistanceTo(tanks[i]) > 0
					&& tanks[i] != targetTank && (tanks[i].isTeammate() // Friend!!
							|| tanks[i].getCrewHealth() == 0 // Dead!!!
					|| tanks[i].getHullDurability() == 0) // Dead!!!
			)
				return false;
		}
		if (100 < dist
				&& (Math.abs(targetTank.getAngleTo(self) - minAngle) > 1 / dist && (targetTank
						.getSpeedX() + targetTank.getSpeedY()) > 1)) {
			return false;
		}
		if (100 < dist)
			move.setFireType(FireType.REGULAR);
		else
			move.setFireType(FireType.PREMIUM_PREFERRED);
		return true;

	}

}
