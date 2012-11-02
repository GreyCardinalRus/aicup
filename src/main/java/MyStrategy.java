import model.*;

import static java.lang.StrictMath.PI;

public final class MyStrategy implements Strategy {
	int numStrategy = 0;

	@Override
	public void move(Tank self, World world, Move move) {
		Tank[] tanks = world.getTanks();
		Bonus[] bonuses = world.getBonuses();
		Player[] players = world.getPlayers();
		// Shell[] shells = world.getShells();

		double minAngle = 3.0;
		int i, j;
		Player MaxRatePlayer = null;
		// int Maxrate = 0;
		Tank tank = null;
		// Двигаемся к бонусам
		Bonus nearbonus = null;
		double minDistance = 10000;
		for (i = 0; i < bonuses.length; i++) {
			if (minDistance > self.getDistanceTo(bonuses[i])) {
				nearbonus = bonuses[i];
				minDistance = self.getDistanceTo(bonuses[i]);
			}
		}
		double moveAngel = 0;
		if (!(nearbonus == null))
			moveAngel = self.getAngleTo(nearbonus);

		for (i = 0; i < tanks.length; i++)
			// Ищем Игрока с максимальным количество очков
			if (self != tanks[i] && !tanks[i].isTeammate()
					&& tanks[i].getCrewHealth() > 0
					&& tanks[i].getHullDurability() > 0) {
				for (j = 0; j < players.length; j++) {
					if (MaxRatePlayer == null
							|| players[j].getScore() > MaxRatePlayer.getScore()) {
						MaxRatePlayer = players[j];
					}
				}
			}
		if (MaxRatePlayer == null || MaxRatePlayer.getScore() == 0) {
			MaxRatePlayer = null;
			for (i = 0; i < tanks.length; i++) {
				if (!tanks[i].isTeammate() && tanks[i].getCrewHealth() > 0
						&& tanks[i].getHullDurability() > 0) {
					if (minAngle > self.getTurretAngleTo(tanks[i])) {
						tank = tanks[i];
						minAngle = self.getTurretAngleTo(tanks[i]);
					}
				}
			}
		} else {
			for (i = 0; i < tanks.length; i++) {
				if (tanks[i].getPlayerName().equals(MaxRatePlayer.getName())) {
					tank = tanks[i];
					// minAngle = self.getTurretAngleTo(tanks[i]);
				}
			}

		}
		if (tank != null) {
			minAngle = self.getTurretAngleTo(tank);
			double dist = self.getDistanceTo(tank);

			if (minAngle < (1 / dist) && minAngle > (-1 / dist)
					&& (self.getRemainingReloadingTime() == 0))
				move.setFireType(FireType.PREMIUM_PREFERRED);
			else
				move.setTurretTurn(minAngle);
		}
		if (self.getRemainingReloadingTime() < 10)
			moveAngel = minAngle; // если можем стрелять - повернемся всем
									// корпусом!
		if (moveAngel > 2) {
			move.setLeftTrackPower(-1);
			move.setRightTrackPower(-1);
		} else if (moveAngel > 1) {
			move.setLeftTrackPower(1);
			move.setRightTrackPower(-1);
		} else if (moveAngel > -1) {
			move.setLeftTrackPower(1);
			move.setRightTrackPower(1);
		} else if (moveAngel > -2) {
			move.setLeftTrackPower(-1);
			move.setRightTrackPower(1);
		} else {
			move.setLeftTrackPower(-1);
			move.setRightTrackPower(-1);
		}

	}

	@Override
	public TankType selectTank(int tankIndex, int teamSize) {
		if (1 == teamSize)
			return TankType.MEDIUM;
		return TankType.MEDIUM;
	}

	void turnBudy(double angle) {

	}

}
