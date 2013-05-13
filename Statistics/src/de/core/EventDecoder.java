package de.core;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.espertech.esper.client.EPRuntime;

public class EventDecoder
{
	private Map<Integer, Entity> cache;
	private Map<Integer, Player> playerList;
	private EPRuntime cepRT;

	public EventDecoder(EPRuntime cepRT)
	{
		cache = new HashMap<Integer, Entity>();
		playerList = new HashMap<Integer, Player>();
		this.cepRT = cepRT;
	}

	public void decode(int lines, String filePath)
	{
		try
		{
			GZipReader reader = new GZipReader(filePath);

			String[] data;

			for (int i = 0; i < lines; i++)
			{
				// System.out.println(reader.readRawNextLine());

				data = reader.readNextLine();

				if (data != null)
				{
					// print data
					// printData(data);

					Entity entity = decodeData(data);

					cepRT.sendEvent(entity);

					if (!cache.containsKey(entity.getId()))
					{
						cache.put(entity.getId(), entity);
					}
					else
					{
						cache.get(entity.getId()).update(entity);
					}

					// float dist = cache.get(entity.getId()).getTotalDistance();
					//
					// if (dist != 0)
					// {
					// System.out.println(dist);
					// }

					// if (data[0].startsWith("98"))
					// {
					// reader.printStatistic(data);
					// }
				}
			}

			// for (Entry<Integer, Entity> e : cache.entrySet())
			// {
			// System.out.println(e.getValue());
			// }

			System.out.println(cache.size() + " verchieden IDs");
		}
		catch (Exception e)
		{
			Logger.getLogger("GZipReader").log(Level.SEVERE, "Error: {0}", new Object[] { e.getMessage() });
		}
	}

	public Entity decodeData(String[] data)
	{
		final int length = data.length;

		if (data == null || length < 13)
		{
			Logger.getLogger("GZipReader").log(Level.WARNING, "Warning: {0} not vaild format!", new Object[] { data });
		}

		int id = Integer.parseInt(data[0]);
		long timeStamp = Long.parseLong(data[1]);
		return new Entity(id, timeStamp, Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4]), Integer.parseInt(data[7]), Integer.parseInt(data[8]), Integer.parseInt(data[9]), Integer.parseInt(data[10]), Integer.parseInt(data[11]), Integer.parseInt(data[12]));
	}

	public static void printData(String[] data)
	{
		System.out.println("==============================");
		System.out.println("Sender->" + data[0]);
		System.out.println("Zeitstempel->" + data[1]);
		System.out.println("Position (x)->" + data[2]);
		System.out.println("Position (y)->" + data[3]);
		System.out.println("Position (z)->" + data[4]);
		System.out.println("Geschwindigkeit->" + data[5]);
		System.out.println("Beschleunigung->" + data[6]);
		System.out.println("Geschw. (x)->" + data[7]);
		System.out.println("Geschw. (y)->" + data[8]);
		System.out.println("Geschw. (z)->" + data[9]);
		System.out.println("Beschl. (x)->" + data[10]);
		System.out.println("Beschl. (y)->" + data[11]);
		System.out.println("Beschl. (z)->" + data[12]);
	}

	public void createPlayerList()
	{
		// key = left Leg, Leg 0 = left, 1 = right
		// Player(int id, String team, String name, int age, PlayingPosition playingPosition, int preferedFoot, int leftFootID, int
		// rightFootID)

		// Team A
		playerList.put(13, new Player(13, "A", "Nick Gertje", 29, PlayingPosition.GK, 1, 13, 14));
		playerList.put(47, new Player(47, "A", "Dennis Dotterweich", 21, PlayingPosition.LB, 0, 47, 16));
		playerList.put(49, new Player(49, "A", "Niklas Waelzlein", 19, PlayingPosition.DF, 1, 49, 88));
		playerList.put(19, new Player(19, "A", "Wili Sommer", 23, PlayingPosition.RB, 1, 19, 52));
		playerList.put(53, new Player(53, "A", "Philipp Harlass", 24, PlayingPosition.LM, 0, 53, 54));
		playerList.put(23, new Player(23, "A", "Roman Hartleb", 22, PlayingPosition.CM, 1, 23, 24));
		playerList.put(57, new Player(57, "A", "Erik Engelhardt", 21, PlayingPosition.RM, 1, 57, 58));
		playerList.put(59, new Player(59, "A", "Sandro Schneider", 27, PlayingPosition.CF, 0, 59, 28));

		// Team V
		playerList.put(61, new Player(61, "B", "Leon Krapf", 26, PlayingPosition.GK, 0, 61, 62));
		playerList.put(63, new Player(63, "B", "Kevin Baer", 18, PlayingPosition.LB, 0, 63, 64));
		playerList.put(65, new Player(65, "B", "Luca Ziegler", 29, PlayingPosition.SW, 1, 65, 66));
		playerList.put(67, new Player(67, "B", "Ben Mueller", 26, PlayingPosition.RB, 1, 67, 68));
		playerList.put(69, new Player(69, "B", "Vale Reitstetter", 22, PlayingPosition.CM, 1, 69, 38));
		playerList.put(71, new Player(71, "B", "Christopher Lee", 23, PlayingPosition.CM, 1, 71, 40));
		playerList.put(73, new Player(73, "B", "Leon Heinze", 27, PlayingPosition.FW, 0, 73, 74));
		playerList.put(75, new Player(75, "B", "Leo Langhans", 21, PlayingPosition.RW, 1, 75, 44));
	}
}
