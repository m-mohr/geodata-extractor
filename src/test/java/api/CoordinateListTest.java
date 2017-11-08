package api;

import de.lutana.geodataextractor.recognizer.coordinates.CoordinateFromText;
import de.lutana.geodataextractor.recognizer.coordinates.CoordinateList;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class CoordinateListTest {
	
    @org.junit.runners.Parameterized.Parameter(0)
    public Double[] data;
    @org.junit.runners.Parameterized.Parameter(1)
    public boolean isLongitude;
    @org.junit.runners.Parameterized.Parameter(2)
    public Double removed;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		Object[][] list = new Object[][] {
			// Longitude
			{new Double[] {-179.0, -178.0, 178.0, 179.0, 0.0}, true, 0.0},
			{new Double[] {90.0, 35.5, 36.0, 36.5, 37.0}, true, 90.0},
			{new Double[] {35.5, 36.0, 36.5, 37.0, 38.0}, true, null},
			{new Double[] {35.0, 35.5, 36.5, 37.0, 40.0}, true, 40.0},
			{new Double[] {35.0, 35.5, 36.5, 37.0, 40.0, 41.0}, true, null},
			// Latitude
			{new Double[] {85.0, 87.0, 89.0, -89.0, 0.0}, false, 0.0},
			{new Double[] {42.0, 42.0, 42.0, 31.0, 31.0}, false, null}
		};
		return Arrays.asList(list);
    }

	@org.junit.Test
    public void testRemoveOutliers() {
		CoordinateList expected = new CoordinateList();
		CoordinateList result = new CoordinateList();
		for(Double value : this.data) {
			CoordinateFromText c;
			if (isLongitude) {
				c = new CoordinateFromText(null, value);
			}
			else {
				c = new CoordinateFromText(value, null);
			}
			result.add(c);
			if (!value.equals(removed)) {
				expected.add(c);
			}
		}

		result.removeOutliers();
		
		Assert.assertEquals(expected, result);
    }
	
}
