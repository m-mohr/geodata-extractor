package api;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.util.GeoTools;
import java.io.IOException;
import org.junit.Assert;

public class GeoToolsTest {

	@org.junit.Test
	public void testRoundLatLon() throws IOException {
		double expected = 66.6667;
		double rounded = GeoTools.roundLatLon(66.6666666666);
		Assert.assertEquals(expected, rounded, 0.00009);
	}

	@org.junit.Test
	public void testUnion() throws IOException {
		Location expected = new Location(-124.8, 98.5, 24.7, 49.4);
		
		LocationCollection collection = new LocationCollection();
		collection.add(new Location(98.5, 39.76));
		collection.add(new Location(-124.8, 67.0, 24.7, 49.4));

		Location result = GeoTools.union(collection);

		Assert.assertEquals(expected, result);
	}

	@org.junit.Test
	public void testUnionReversed() throws IOException {
		Location expected = new Location(-124.8, 98.5, 24.7, 49.4);
		
		LocationCollection collection = new LocationCollection();
		collection.add(new Location(-124.8, 67.0, 24.7, 49.4));
		collection.add(new Location(98.5, 39.76));

		Location result = GeoTools.union(collection);

		Assert.assertEquals(expected, result);
	}
	
	
}
