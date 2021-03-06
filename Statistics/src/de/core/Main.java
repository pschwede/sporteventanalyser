package de.core;

import de.tudresden.inf.rn.mobilis.sea.pubsub.model.tree.StatisticsFacade;

/**
 * Auslesen vonner Megadatei f�r ein Programm, welches es eigentlich schon vom Fraunhofer gibt
 * 
 * @author Alrik Geselle
 * @version 1.0
 */
/**
 * @author Alrik Geselle, Richard John, Tommy Kubika
 * 
 */
public class Main
{
	private Esper esper;
	private GameInformation gameInformation;

	public Main(StatisticsFacade statisticsFacade)
	{
		this.esper = new Esper();

		gameInformation = new GameInformation(statisticsFacade);

		for (int player : Config.PLAYERSENSORIDS)
		{
			esper.getAllFromSensorId(player, 100, gameInformation);
			// esperTest.getAllEventsFromSensorId(player, gameInformation); // every event
		}

		for (int ball : Config.BALLIDS)
		{
			esper.getAllFromSensorId(ball, 100, gameInformation);
			// esperTest.getAllEventsFromSensorId(ball, gameInformation); // every event
		}
	}

	public void sendEvent(Object object)
	{
		esper.sendEvent(object);
	}

	public void setInterruptionBegin(long timestamp)
	{
		getGameInformation().setInterruptionBegin(timestamp);
	}

	public void setInterruptionEnd(long timestamp)
	{
		getGameInformation().setInterruptionEnd(timestamp);
	}

	public GameInformation getGameInformation()
	{
		return gameInformation;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Main main = new Main(null);

		/**
		 * Start decoding the local file async
		 */
		LocalEventDecoder eventDecoder = new LocalEventDecoder(main.esper);
		eventDecoder.decodeFileAsynchronous(100000000, "full-game.gz");
	}
}