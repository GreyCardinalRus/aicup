import model.*;

import static java.lang.StrictMath.PI;

public final class MyStrategy implements Strategy {
	static boolean IsDebug= true;
	int numStrategy = 0;
	long tid = 0;
	Tank EnemyTank = null;
	Bonus nearbonus = null;
	Player EnemyPlayer = null;

	@Override
	public void move(Tank self, World world, Move move) {
		Tank[] tanks = world.getTanks();
		Bonus[] bonuses = world.getBonuses();
		Player[] players = world.getPlayers();
		// Shell[] shells = world.getShells();
		// System.out.println("players="+players.length);
		double minAngle = 3.0;
		int i, j;
		EnemyPlayer = null;
		// int Maxrate = 0;
		// Tank tank = null;
		// Двигаемся к бонусам
		nearbonus = null;
		double minDistance = 10000;
		for (i = 0; i < bonuses.length; i++) {
			if (minDistance > self.getDistanceTo(bonuses[i])
					&& bonuses[i].getType() == BonusType.MEDIKIT) {
				nearbonus = bonuses[i];
				minDistance = self.getDistanceTo(bonuses[i]);
			}
		}

		if (nearbonus == null)
			for (i = 0; i < bonuses.length; i++) {
				if (minDistance > self.getDistanceTo(bonuses[i])
						&& bonuses[i].getType() == BonusType.REPAIR_KIT) {
					nearbonus = bonuses[i];
					minDistance = self.getDistanceTo(bonuses[i]);
				}
			}
		if (nearbonus == null)
			for (i = 0; i < bonuses.length; i++) {
				if (minDistance > self.getDistanceTo(bonuses[i])
						&& bonuses[i].getType() == BonusType.AMMO_CRATE) {
					nearbonus = bonuses[i];
					minDistance = self.getDistanceTo(bonuses[i]);
				}
			}

		double moveAngel = 0;
		if (!(nearbonus == null))
			moveAngel = self.getAngleTo(nearbonus);

		for (i = 0; i < tanks.length; i++)
			// Ищем Игрока с максимальным количество очков
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
						// System.out.println("Players="+MaxRatePlayer.getScore());
					}

				}
			if (EnemyTank != null) {
			if (tid != EnemyTank.getId() && EnemyPlayer.getScore() > 0)
				if(IsDebug) System.out.println("change Target:" + EnemyTank.getId()
						+ " Player:" + EnemyTank.getPlayerName());
			tid = EnemyTank.getId();
			minAngle = self.getTurretAngleTo(EnemyTank);
			double dist = self.getDistanceTo(EnemyTank);

			if (minAngle < (10 / dist) && minAngle > (-10 / dist)
					&& (self.getRemainingReloadingTime() == 0))
				move.setFireType(FireType.PREMIUM_PREFERRED);
			else
				move.setTurretTurn(minAngle);
		} else {
			if(IsDebug) System.out.println("lost Target:");
			move.setTurretTurn(1);
		}

		if (self.getRemainingReloadingTime() < 10 &&(minAngle > 0.1||minAngle < -0.1)){// Ready to fire
			if (minAngle > 0.1) {
				move.setLeftTrackPower(1 * self.getEngineRearPowerFactor());
				move.setRightTrackPower(-1);
			} else if (moveAngel < -0.1) {
				move.setLeftTrackPower(-1);
				move.setRightTrackPower(1 * self.getEngineRearPowerFactor());
			} else {
				move.setLeftTrackPower(-1);
				move.setRightTrackPower(-1);
			}
		} 
		else {
			if (moveAngel > 3) {
				move.setLeftTrackPower(-1);
				move.setRightTrackPower(-1);
			} else if (moveAngel > 0.5) {
				move.setLeftTrackPower(1 * self.getEngineRearPowerFactor());
				move.setRightTrackPower(-1);
			} else if (moveAngel > -0.5) {
				if (((self.getAngle() > 2.0 || self.getAngle() < -2.0)
						&& self.getY() < (world.getHeight() - 0.5 * (self
								.getWidth() + self.getHeight()))
						|| (self.getAngle() > -1.0 && self.getAngle() < 1.0)
						&& self.getY() > 0.5 * (self.getWidth() + self
								.getHeight())
						|| (self.getAngle() > 1.0 && self.getAngle() < 2.0)
						&& self.getX() < (world.getWidth() - 0.5 * (self
								.getWidth() + self.getHeight())) || (self
						.getAngle() > -2.0 && self.getAngle() < -1.0)
						&& self.getX() > (0.5 * (self.getWidth() + self
								.getHeight())))) {
					move.setLeftTrackPower(1);
					move.setRightTrackPower(1);
				}
				else
				{
					if(IsDebug) System.out.println("Forward("+world.getTick()+")"+"self.getAngle()="+(int)self.getAngle()+" self.getY()="+(int)self.getY()+" world.getHeight()="+world.getHeight());
				}
			} else if (moveAngel > -3) {
				move.setLeftTrackPower(-1);
				move.setRightTrackPower(1 * self.getEngineRearPowerFactor());
			} else {
				if (((self.getAngle() > 2.0 || self.getAngle() < -2.0)
						&& self.getY() > ( 0.5 * (self
								.getWidth() + self.getHeight()))
						|| (self.getAngle() > -1.0 && self.getAngle() < 1.0)
						&& self.getY() <( world.getHeight() -0.5 * (self.getWidth() + self
								.getHeight()))
						|| (self.getAngle() > 1.0 && self.getAngle() < 2.0)
						&& self.getX() > ( 0.5 * (self
								.getWidth() + self.getHeight())) || (self
						.getAngle() > -2.0 && self.getAngle() < -1.0)
						&& self.getX() < (world.getWidth() -0.5 * (self.getWidth() + self
								.getHeight())))) {
					move.setLeftTrackPower(-1);
					move.setRightTrackPower(-1);
				}
				else
				{
					if(IsDebug) System.out.println("Reverse("+world.getTick()+")"+"self.getAngle()="+(int)self.getAngle()+" self.getY()="+(int)self.getY()+" world.getHeight()="+world.getHeight());
				}
			}
		}

	}

	@Override
	public TankType selectTank(int tankIndex, int teamSize) {
		 //if (1 == teamSize)
		 //return TankType.HEAVY;
		return TankType.MEDIUM;
	}

	void turnBudy(double angle) {

	}

}
