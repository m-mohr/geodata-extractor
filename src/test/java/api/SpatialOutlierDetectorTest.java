package api;

import de.lutana.geodataextractor.util.SpatialOutlierDetector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class SpatialOutlierDetectorTest {
	
    @org.junit.runners.Parameterized.Parameter(0)
    public Double[] data;
    @org.junit.runners.Parameterized.Parameter(1)
    public boolean isLongitude;
    @org.junit.runners.Parameterized.Parameter(2)
    public Double[] removedExpected;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		Object[][] list = new Object[][] {
			{new Double[] {-179.0, -178.0, 178.0, 179.0, 0.0}, true, new Double[] {0.0}},
			{new Double[] {90.0, 35.5, 36.0, 36.5, 37.0}, true, new Double[] {90.0}},
			{new Double[] {35.5, 36.0, 36.5, 37.0, 38.0}, true, new Double[] {}},
			{new Double[] {35.0, 35.5, 36.5, 37.0, 40.0}, true, new Double[] {40.0}},
			{new Double[] {35.0, 35.5, 36.5, 37.0, 40.0, 41.0}, true, new Double[] {}},
			{new Double[] {85.0, 87.0, 89.0, -89.0, 0.0}, false, new Double[] {0.0}}
		};
		return Arrays.asList(list);
    }

	@org.junit.Test
    public void testDetect() {
		SpatialOutlierDetector sod = new SpatialOutlierDetector();
		List<Double> list = new ArrayList<>();
		list.addAll(Arrays.asList(data)); // ToDo: Stupid hack as Arrays.asList returns a fixed size list.
		List<Double> removedResult = sod.detectWgs84Outliers(list, isLongitude);
		
		Assert.assertEquals(Arrays.asList(removedExpected), removedResult);
    }
	
}
