package api;

import de.lutana.geodataextractor.detector.coordinates.Coordinate;
import de.lutana.geodataextractor.detector.coordinates.CoordinateList;
import de.lutana.geodataextractor.detector.coordinates.CoordinateParser;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class CoordinateParserTest {
	
    @org.junit.runners.Parameterized.Parameter(0)
    public String pattern;
    @org.junit.runners.Parameterized.Parameter(1)
    public Coordinate location;
	
	private final CoordinateParser parser = new CoordinateParser();

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		Object[][] list = new Object[][] {
			// Invalid
			{"-1234", null},
			{"12°", null},
			{"55N", null},
			{"13\"W", null},
			{"90'E", null},
			{"12°N16°W", null},
			{"12AB12", null},
			{"88' 12.5\"W", null},
			{"12W°W", null},
			{"125I°W", null},
			{"12'5°W", null},
			{"81I°W ", null},
			{"B1°W", null},
			{"°N", null},
			// WGS84 (D, DM, DMS)
			{"0° N", new Coordinate(0d, null)},
			{"12° N", new Coordinate(12d, null)},
			{"12°S", new Coordinate(-12d, null)},
			{"90° 30' W", new Coordinate(null, -90.5)},
			{"1°30'40.5\"E", new Coordinate(null, 1.51125)},
			{"1°30'40.5\"E.", new Coordinate(null, 1.51125)},
			// Universal Transverse Mercator (UTM)
			{"17T 630084 4833438", new Coordinate(43.642567, -79.387139)},
			{"17N 630084 4833438", new Coordinate(43.642567, -79.387139)},
			{"31N 166021 0", new Coordinate(0.0, 0.0)},
			{"30 N 808084 14385", new Coordinate(0.13, -0.2324)},
			{"34 G 683473 4942631", new Coordinate(-45.6456, 23.3545)},
			{"25 L 404859 8588690", new Coordinate(-12.765, -33.8765)},
			{"02 C 506346 1057742", new Coordinate(-80.5434, -170.654)},
			{"08 Q 453580 2594272", new Coordinate(23.4578, -135.4545)},
			{"You can find it at UTM 57 X 450793 8586116.", new Coordinate(77.3450, 156.9876)},
			// OS (Ordnance Survey)
			{"NY 9545 9776", new Coordinate(55.27392830, -2.07162170)},
			{"NU1918 2813", new Coordinate(55.54650170, -1.69597330)},
			{"NU19182813", new Coordinate(55.54650170, -1.69597330)},
			// Military Grid Reference System (MGRS)
			{"4QFJ 12345 67890", new Coordinate(21.4097967, -157.9160812)},
			{"4QFJ1234567890", new Coordinate(21.4097967, -157.9160812)},
			{"15SUD0370514711", new Coordinate(38.9593911, -95.2654824)}
		};
		return Arrays.asList(list);
    }

	@org.junit.Test
    public void testDetect() {
		CoordinateList result = parser.parse(this.pattern);

		if (result.isEmpty()) {
			Assert.assertNull(this.location);
		}
		else {
			Assert.assertEquals(1, result.size());
			Assert.assertEquals(this.location, result.get(0));
		}
    }
	
}
