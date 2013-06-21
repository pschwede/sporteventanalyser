package de.tudresden.inf.rn.mobilis.sea.pubsub.model.tree;

import de.tudresden.inf.rn.mobilis.sea.pubsub.model.tree.leaves.interfaces.ICurrentPlayerData;
import de.tudresden.inf.rn.mobilis.sea.pubsub.model.tree.leaves.interfaces.ICurrentPositionData;
import de.tudresden.inf.rn.mobilis.sea.pubsub.model.tree.leaves.interfaces.ICurrentTeamData;
import de.tudresden.inf.rn.mobilis.sea.pubsub.model.tree.nodes.impl.BallPosition;
import de.tudresden.inf.rn.mobilis.sea.pubsub.model.tree.nodes.impl.PlayerPosition;
import de.tudresden.inf.rn.mobilis.sea.pubsub.model.tree.nodes.impl.PlayerStatistic;
import de.tudresden.inf.rn.mobilis.sea.pubsub.model.tree.nodes.impl.TeamStatistic;

//TODO: This cmd^^
/**
 * The <code>StatisticsFacade</code> is used to bundle all available interfaces
 * of the <code>StatisticCollection</code> into one auxiliary proxy. For
 * multi-thread purposes it is also capable to delegate calls to a free
 * resource. So you may toggle the model to work with (the returned model will
 * not be filled with new data until you release it by toggling again)
 */
public class StatisticsFacade implements ICurrentPositionData,
		ICurrentPlayerData, ICurrentTeamData {

	/**
	 * The statistics model (this <code>StatisticCollection</code> will be
	 * filled with new data)
	 */
	private StatisticCollection statistics;

	/**
	 * This <code>StatisticCollection</code> will be refilled with the current
	 * data when <code>StatisticsFacade.getCurrentStatistics()</code> is called
	 */
	private StatisticCollection mStatistics;

	/**
	 * Mutex to handle access to this <code>DataNode</code>
	 */
	private Object currentPlayerDataMutex = new Object(),
			currentPositionDataMutex = new Object(),
			currentTeamDataMutex = new Object();

	/**
	 * This is an auxiliary mutex (used because mStatistics is communicated and
	 * therefore it should not be mutexed)
	 */
	private Object mStatisticsMutex = new Object();

	/**
	 * Constructor for this <code>StatisticFacade</code>
	 */
	public StatisticsFacade() {
		statistics = new StatisticCollection();
		mStatistics = new StatisticCollection();

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					synchronized (mStatisticsMutex) {
						try {
							// Wait till next copy
							mStatisticsMutex.wait();

							// Copy currentPlayerData-Node
							synchronized (currentPlayerDataMutex) {
								statistics.getCurrentPlayerData().copy(
										mStatistics.getCurrentPlayerData());
							}

							// Copy currentPositionData-Node
							synchronized (currentPositionDataMutex) {
								statistics.getCurrentPositionData().copy(
										mStatistics.getCurrentPositionData());
							}

							// Copy currentTeamData-Node
							synchronized (currentTeamDataMutex) {
								statistics.getCurrentTeamData().copy(
										mStatistics.getCurrentTeamData());
							}

							// Notify
							mStatisticsMutex.notifyAll();
						} catch (InterruptedException e) {
							// Ignore: while (true) "handles" this Exception
						}
					}
				}
			}

		}).start();
	}

	/**
	 * Get the current <code>StatisticCollection</code>. The
	 * <code>StatisticCollection</code> is a clone of the working model and
	 * therefore no changes are made there again
	 * 
	 * @return a clone of the current <code>StatisticCollection</code>
	 */
	public StatisticCollection getCurrentStatistics() {
		synchronized (mStatisticsMutex) {
			// Notify (copy)
			mStatisticsMutex.notifyAll();

			try {
				// Wait till copy is ready to be read
				mStatisticsMutex.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return mStatistics;
	}

	/**
	 * Register teams. The chosen name will be used as identifier (ID)
	 * 
	 * @param firstTeamname
	 *            the name of the first team
	 * @param secondTeamname
	 *            the name of the second team
	 */
	public synchronized void registerTeams(String firstTeamname,
			String secondTeamname) {
		synchronized (currentTeamDataMutex) {
			statistics.getCurrentTeamData().registerTeams(
					new TeamStatistic(firstTeamname, 50d, 100d),
					new TeamStatistic(secondTeamname, 50d, 100d));
		}
	}

	/**
	 * Register a new player. It is mandatory to call this method before setting
	 * the position of a player
	 * 
	 * @param id
	 *            the id of the player
	 */
	public synchronized void registerPlayer(int id) {
		// Register player: CurrentPositionData-leaf
		synchronized (currentPositionDataMutex) {
			statistics.getCurrentPositionData().registerPlayerPosition(
					new PlayerPosition(id, 0, 0, 0, 0));
		}

		// Register player: CurrentPositionData-leaf
		synchronized (currentPlayerDataMutex) {
			statistics.getCurrentPlayerData().registerPlayerStatistic(
					new PlayerStatistic(id, 0, 0, 0, 0, 0, 0, 0));
		}
	}

	@Override
	public synchronized void setPositionOfPlayer(int id, int positionX,
			int positionY, int velocityX, int velocityY) {
		synchronized (currentPositionDataMutex) {
			PlayerPosition playerPosition = statistics.getCurrentPositionData()
					.getPlayerPosition(id);
			if (playerPosition != null) {
				playerPosition.setPositionX(positionX);
				playerPosition.setPositionY(positionY);
				playerPosition.setVelocityX(velocityX);
				playerPosition.setVelocityY(velocityY);
			}
		}
	}

	@Override
	public synchronized void setPositionOfBall(int positionX, int positionY,
			int positionZ, int velocityX, int velocityY) {
		synchronized (currentPositionDataMutex) {
			BallPosition ballPosition = statistics.getCurrentPositionData()
					.getBallPosition();
			ballPosition.setPositionX(positionX);
			ballPosition.setPositionY(positionY);
			ballPosition.setPositionZ(positionZ);
			ballPosition.setVelocityX(velocityX);
			ballPosition.setVelocityY(velocityY);
		}
	}

	@Override
	public synchronized void setPlayerStatistic(int id, int passesMade,
			int passesReceived, int tacklings, int tacklesWon, int goalsScored,
			int ballContacts, long possessionTime) {
		synchronized (currentPlayerDataMutex) {
			PlayerStatistic playerStatistic = statistics.getCurrentPlayerData()
					.getPlayerStatistic(id);
			if (playerStatistic != null) {
				playerStatistic.setPassesMade(passesMade);
				playerStatistic.setPassesReceived(passesReceived);
				playerStatistic.setTacklings(tacklings);
				playerStatistic.setTacklesWon(tacklesWon);
				playerStatistic.setGoalsScored(goalsScored);
				playerStatistic.setBallContacts(ballContacts);
				playerStatistic.setPossessionTime(possessionTime);
			}
		}
	}

	@Override
	public synchronized void setPassesMade(int id, int passesMade) {
		synchronized (currentPlayerDataMutex) {
			PlayerStatistic playerStatistic = statistics.getCurrentPlayerData()
					.getPlayerStatistic(id);
			if (playerStatistic != null) {
				playerStatistic.setPassesMade(passesMade);
			}
		}
	}

	@Override
	public synchronized void setPassesReceived(int id, int passesReceived) {
		synchronized (currentPlayerDataMutex) {
			PlayerStatistic playerStatistic = statistics.getCurrentPlayerData()
					.getPlayerStatistic(id);
			if (playerStatistic != null) {
				playerStatistic.setPassesReceived(passesReceived);
			}
		}
	}

	@Override
	public synchronized void setTacklings(int id, int tacklings) {
		synchronized (currentPlayerDataMutex) {
			PlayerStatistic playerStatistic = statistics.getCurrentPlayerData()
					.getPlayerStatistic(id);
			if (playerStatistic != null) {
				playerStatistic.setTacklings(tacklings);
			}
		}
	}

	@Override
	public synchronized void setTacklesWon(int id, int tacklesWon) {
		synchronized (currentPlayerDataMutex) {
			PlayerStatistic playerStatistic = statistics.getCurrentPlayerData()
					.getPlayerStatistic(id);
			if (playerStatistic != null) {
				playerStatistic.setTacklesWon(tacklesWon);
			}
		}
	}

	@Override
	public synchronized void setGoalsScored(int id, int goalsScored) {
		synchronized (currentPlayerDataMutex) {
			PlayerStatistic playerStatistic = statistics.getCurrentPlayerData()
					.getPlayerStatistic(id);
			if (playerStatistic != null) {
				playerStatistic.setGoalsScored(goalsScored);
			}
		}
	}

	@Override
	public synchronized void setBallContacs(int id, int ballContacts) {
		synchronized (currentPlayerDataMutex) {
			PlayerStatistic playerStatistic = statistics.getCurrentPlayerData()
					.getPlayerStatistic(id);
			if (playerStatistic != null) {
				playerStatistic.setBallContacts(ballContacts);
			}
		}
	}

	@Override
	public synchronized void setPossessionTime(int id, long possessionTime) {
		synchronized (currentPlayerDataMutex) {
			PlayerStatistic playerStatistic = statistics.getCurrentPlayerData()
					.getPlayerStatistic(id);
			if (playerStatistic != null) {
				playerStatistic.setPossessionTime(possessionTime);
			}
		}
	}

	@Override
	public void setBallPossession(String teamname, double ballPossession) {
		synchronized (currentPlayerDataMutex) {
			TeamStatistic firstTeamStatistic = statistics.getCurrentTeamData()
					.getTeamStatisticOfFirstTeam();
			if (firstTeamStatistic != null) {
				TeamStatistic secondTeamStatistic = statistics
						.getCurrentTeamData().getTeamStatisticOfSecondTeam();

				if (firstTeamStatistic.getTeamname().equals(teamname)) {
					firstTeamStatistic.setBallPossession(ballPossession);
					secondTeamStatistic.setBallPossession(100 - ballPossession);
				} else if (secondTeamStatistic.getTeamname().equals(teamname)) {
					secondTeamStatistic.setBallPossession(ballPossession);
					firstTeamStatistic.setBallPossession(100 - ballPossession);
				}
			}
		}
	}

	@Override
	public void setPassingAccuracy(String teamname, double passingAccuracy) {
		synchronized (currentPlayerDataMutex) {
			TeamStatistic teamStatistic = statistics.getCurrentTeamData()
					.getTeamStatistic(teamname);
			if (teamStatistic != null) {
				teamStatistic.setPassingAccuracy(passingAccuracy);
			}
		}
	}
}
