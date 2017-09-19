package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.slf4j.LoggerFactory;

/**
 * Just a dumb country detector for the captions.
 * 
 * Using this to test the framework until we have something useful implemented.
 * 
 * @author Matthias Mohr
 */
public class DumbCountryTextDetector implements TextDetector {
	
	private final HashMap<String, Location> countryMap;
	
	public DumbCountryTextDetector() {
		this.countryMap = new HashMap<>();
		this.countryMap.put("Afghanistan", new Location(60.5284298033, 75.1580277851, 29.318572496, 38.4862816432));
		this.countryMap.put("Angola", new Location(11.6400960629, 24.0799052263, -17.9306364885, -4.43802336998));
		this.countryMap.put("Albania", new Location(19.3044861183, 21.0200403175, 39.624997667, 42.6882473822));
		this.countryMap.put("United Arab Emirates", new Location(51.5795186705, 56.3968473651, 22.4969475367, 26.055464179));
		this.countryMap.put("Argentina", new Location(-73.4154357571, -53.628348965, -55.25, -21.8323104794));
		this.countryMap.put("Armenia", new Location(43.5827458026, 46.5057198423, 38.7412014837, 41.2481285671));
		this.countryMap.put("Antarctica", new Location(-180.0, 180.0, -90.0, -63.2706604895));
		this.countryMap.put("French Southern and Antarctic Lands", new Location(68.72, 70.56, -49.775, -48.625));
		this.countryMap.put("Australia", new Location(113.338953078, 153.569469029, -43.6345972634, -10.6681857235));
		this.countryMap.put("Austria", new Location(9.47996951665, 16.9796667823, 46.4318173285, 49.0390742051));
		this.countryMap.put("Azerbaijan", new Location(44.7939896991, 50.3928210793, 38.2703775091, 41.8606751572));
		this.countryMap.put("Burundi", new Location(29.0249263852, 30.752262811, -4.49998341229, -2.34848683025));
		this.countryMap.put("Belgium", new Location(2.51357303225, 6.15665815596, 49.5294835476, 51.4750237087));
		this.countryMap.put("Benin", new Location(0.772335646171, 3.79711225751, 6.14215770103, 12.2356358912));
		this.countryMap.put("Burkina Faso", new Location(-5.47056494793, 2.17710778159, 9.61083486576, 15.1161577418));
		this.countryMap.put("Bangladesh", new Location(88.0844222351, 92.6727209818, 20.670883287, 26.4465255803));
		this.countryMap.put("Bulgaria", new Location(22.3805257504, 28.5580814959, 41.2344859889, 44.2349230007));
		this.countryMap.put("Bahamas", new Location(-78.98, -77.0, 23.71, 27.04));
		this.countryMap.put("Bosnia and Herzegovina", new Location(15.7500260759, 19.59976, 42.65, 45.2337767604));
		this.countryMap.put("Belarus", new Location(23.1994938494, 32.6936430193, 51.3195034857, 56.1691299506));
		this.countryMap.put("Belize", new Location(-89.2291216703, -88.1068129138, 15.8869375676, 18.4999822047));
		this.countryMap.put("Bolivia", new Location(-69.5904237535, -57.4983711412, -22.8729187965, -9.76198780685));
		this.countryMap.put("Brazil", new Location(-73.9872354804, -34.7299934555, -33.7683777809, 5.24448639569));
		this.countryMap.put("Brunei", new Location(114.204016555, 115.450710484, 4.007636827, 5.44772980389));
		this.countryMap.put("Bhutan", new Location(88.8142484883, 92.1037117859, 26.7194029811, 28.2964385035));
		this.countryMap.put("Botswana", new Location(19.8954577979, 29.4321883481, -26.8285429827, -17.6618156877));
		this.countryMap.put("Central African Republic", new Location(14.4594071794, 27.3742261085, 2.2676396753, 11.1423951278));
		this.countryMap.put("Canada", new Location(-140.99778, -52.6480987209, 41.6751050889, 83.23324));
		this.countryMap.put("Switzerland", new Location(6.02260949059, 10.4427014502, 45.7769477403, 47.8308275417));
		this.countryMap.put("Chile", new Location(-75.6443953112, -66.95992, -55.61183, -17.5800118954));
		this.countryMap.put("China", new Location(73.6753792663, 135.026311477, 18.197700914, 53.4588044297));
		this.countryMap.put("Ivory Coast", new Location(-8.60288021487, -2.56218950033, 4.33828847902, 10.5240607772));
		this.countryMap.put("Cameroon", new Location(8.48881554529, 16.0128524106, 1.72767263428, 12.8593962671));
		this.countryMap.put("Congo (Kinshasa)", new Location(12.1823368669, 31.1741492042, -13.2572266578, 5.25608775474));
		this.countryMap.put("Congo (Brazzaville)", new Location(11.0937728207, 18.4530652198, -5.03798674888, 3.72819651938));
		this.countryMap.put("Colombia", new Location(-78.9909352282, -66.8763258531, -4.29818694419, 12.4373031682));
		this.countryMap.put("Costa Rica", new Location(-85.94172543, -82.5461962552, 8.22502798099, 11.2171192489));
		this.countryMap.put("Cuba", new Location(-84.9749110583, -74.1780248685, 19.8554808619, 23.1886107447));
		this.countryMap.put("Cyprus", new Location(32.2566671079, 34.0048808123, 34.5718694118, 35.1731247015));
		this.countryMap.put("Czech Rep.", new Location(12.2401111182, 18.8531441586, 48.5553052842, 51.1172677679));
		this.countryMap.put("Germany", new Location(5.98865807458, 15.0169958839, 47.3024876979, 54.983104153));
		this.countryMap.put("Djibouti", new Location(41.66176, 43.3178524107, 10.9268785669, 12.6996385767));
		this.countryMap.put("Denmark", new Location(8.08997684086, 12.6900061378, 54.8000145534, 57.730016588));
		this.countryMap.put("Dominican Rep.", new Location(-71.9451120673, -68.3179432848, 17.598564358, 19.8849105901));
		this.countryMap.put("Algeria", new Location(-8.68439978681, 11.9995056495, 19.0573642034, 37.1183806422));
		this.countryMap.put("Ecuador", new Location(-80.9677654691, -75.2337227037, -4.95912851321, 1.3809237736));
		this.countryMap.put("Egypt", new Location(24.70007, 36.86623, 22.0, 31.58568));
		this.countryMap.put("Eritrea", new Location(36.3231889178, 43.0812260272, 12.4554157577, 17.9983074));
		this.countryMap.put("Spain", new Location(-9.39288367353, 3.03948408368, 35.946850084, 43.7483377142));
		this.countryMap.put("Estonia", new Location(23.3397953631, 28.1316992531, 57.4745283067, 59.6110903998));
		this.countryMap.put("Ethiopia", new Location(32.95418, 47.78942, 3.42206, 14.95943));
		this.countryMap.put("Finland", new Location(20.6455928891, 31.5160921567, 59.846373196, 70.1641930203));
		this.countryMap.put("Fiji", new Location(-180.0, 180.0, -18.28799, -16.0208822567));
		this.countryMap.put("Falkland Islands", new Location(-61.2, -57.75, -52.3, -51.1));
		this.countryMap.put("France", new Location(-54.5247541978, 9.56001631027, 2.05338918702, 51.1485061713));
		this.countryMap.put("Gabon", new Location(8.79799563969, 14.4254557634, -3.97882659263, 2.32675751384));
		this.countryMap.put("United Kingdom", new Location(-7.57216793459, 1.68153079591, 49.959999905, 58.6350001085));
		this.countryMap.put("Georgia", new Location(39.9550085793, 46.6379081561, 41.0644446885, 43.553104153));
		this.countryMap.put("Ghana", new Location(-3.24437008301, 1.0601216976, 4.71046214438, 11.0983409693));
		this.countryMap.put("Guinea", new Location(-15.1303112452, -7.83210038902, 7.3090373804, 12.5861829696));
		this.countryMap.put("Gambia", new Location(-16.8415246241, -13.8449633448, 13.1302841252, 13.8764918075));
		this.countryMap.put("Guinea Bissau", new Location(-16.6774519516, -13.7004760401, 11.0404116887, 12.6281700708));
		this.countryMap.put("Equatorial Guinea", new Location(9.3056132341, 11.285078973, 1.01011953369, 2.28386607504));
		this.countryMap.put("Greece", new Location(20.1500159034, 26.6041955909, 34.9199876979, 41.8269046087));
		this.countryMap.put("Greenland", new Location(-73.297, -12.20855, 60.03676, 83.64513));
		this.countryMap.put("Guatemala", new Location(-92.2292486234, -88.2250227526, 13.7353376327, 17.8193260767));
		this.countryMap.put("Guyana", new Location(-61.4103029039, -56.5393857489, 1.26808828369, 8.36703481692));
		this.countryMap.put("Honduras", new Location(-89.3533259753, -83.147219001, 12.9846857772, 16.0054057886));
		this.countryMap.put("Croatia", new Location(13.6569755388, 19.3904757016, 42.47999136, 46.5037509222));
		this.countryMap.put("Haiti", new Location(-74.4580336168, -71.6248732164, 18.0309927434, 19.9156839055));
		this.countryMap.put("Hungary", new Location(16.2022982113, 22.710531447, 45.7594811061, 48.6238540716));
		this.countryMap.put("Indonesia", new Location(95.2930261576, 141.03385176, -10.3599874813, 5.47982086834));
		this.countryMap.put("India", new Location(68.1766451354, 97.4025614766, 7.96553477623, 35.4940095078));
		this.countryMap.put("Ireland", new Location(-9.97708574059, -6.03298539878, 51.6693012559, 55.1316222195));
		this.countryMap.put("Iran", new Location(44.1092252948, 63.3166317076, 25.0782370061, 39.7130026312));
		this.countryMap.put("Iraq", new Location(38.7923405291, 48.5679712258, 29.0990251735, 37.3852635768));
		this.countryMap.put("Iceland", new Location(-24.3261840479, -13.609732225, 63.4963829617, 66.5267923041));
		this.countryMap.put("Israel", new Location(34.2654333839, 35.8363969256, 29.5013261988, 33.2774264593));
		this.countryMap.put("Italy", new Location(6.7499552751, 18.4802470232, 36.619987291, 47.1153931748));
		this.countryMap.put("Jamaica", new Location(-78.3377192858, -76.1996585761, 17.7011162379, 18.5242184514));
		this.countryMap.put("Jordan", new Location(34.9226025734, 39.1954683774, 29.1974946152, 33.3786864284));
		this.countryMap.put("Japan", new Location(129.408463169, 145.543137242, 31.0295791692, 45.5514834662));
		this.countryMap.put("Kazakhstan", new Location(46.4664457538, 87.3599703308, 40.6623245306, 55.3852501491));
		this.countryMap.put("Kenya", new Location(33.8935689697, 41.8550830926, -4.67677, 5.506));
		this.countryMap.put("Kyrgyzstan", new Location(69.464886916, 80.2599902689, 39.2794632025, 43.2983393418));
		this.countryMap.put("Cambodia", new Location(102.3480994, 107.614547968, 10.4865436874, 14.5705838078));
		this.countryMap.put("South Korea", new Location(126.117397903, 129.468304478, 34.3900458847, 38.6122429469));
		this.countryMap.put("Kuwait", new Location(46.5687134133, 48.4160941913, 28.5260627304, 30.0590699326));
		this.countryMap.put("Laos", new Location(100.115987583, 107.564525181, 13.88109101, 22.4647531194));
		this.countryMap.put("Lebanon", new Location(35.1260526873, 36.6117501157, 33.0890400254, 34.6449140488));
		this.countryMap.put("Liberia", new Location(-11.4387794662, -7.53971513511, 4.35575511313, 8.54105520267));
		this.countryMap.put("Libya", new Location(9.31941084152, 25.16482, 19.58047, 33.1369957545));
		this.countryMap.put("Sri Lanka", new Location(79.6951668639, 81.7879590189, 5.96836985923, 9.82407766361));
		this.countryMap.put("Lesotho", new Location(26.9992619158, 29.3251664568, -30.6451058896, -28.6475017229));
		this.countryMap.put("Lithuania", new Location(21.0558004086, 26.5882792498, 53.9057022162, 56.3725283881));
		this.countryMap.put("Luxembourg", new Location(5.67405195478, 6.24275109216, 49.4426671413, 50.1280516628));
		this.countryMap.put("Latvia", new Location(21.0558004086, 28.1767094256, 55.61510692, 57.9701569688));
		this.countryMap.put("Morocco", new Location(-17.0204284327, -1.12455115397, 21.4207341578, 35.7599881048));
		this.countryMap.put("Moldova", new Location(26.6193367856, 30.0246586443, 45.4882831895, 48.4671194525));
		this.countryMap.put("Madagascar", new Location(43.2541870461, 50.4765368996, -25.6014344215, -12.0405567359));
		this.countryMap.put("Mexico", new Location(-117.12776, -86.811982388, 14.5388286402, 32.72083));
		this.countryMap.put("Macedonia", new Location(20.46315, 22.9523771502, 40.8427269557, 42.3202595078));
		this.countryMap.put("Mali", new Location(-12.1707502914, 4.27020999514, 10.0963607854, 24.9745740829));
		this.countryMap.put("Myanmar", new Location(92.3032344909, 101.180005324, 9.93295990645, 28.335945136));
		this.countryMap.put("Montenegro", new Location(18.45, 20.3398, 41.87755, 43.52384));
		this.countryMap.put("Mongolia", new Location(87.7512642761, 119.772823928, 41.5974095729, 52.0473660345));
		this.countryMap.put("Mozambique", new Location(30.1794812355, 40.7754752948, -26.7421916643, -10.3170960425));
		this.countryMap.put("Mauritania", new Location(-17.0634232243, -4.92333736817, 14.6168342147, 27.3957441269));
		this.countryMap.put("Malawi", new Location(32.6881653175, 35.7719047381, -16.8012997372, -9.23059905359));
		this.countryMap.put("Malaysia", new Location(100.085756871, 119.181903925, 0.773131415201, 6.92805288332));
		this.countryMap.put("Namibia", new Location(11.7341988461, 25.0844433937, -29.045461928, -16.9413428687));
		this.countryMap.put("New Caledonia", new Location(164.029605748, 167.120011428, -22.3999760881, -20.1056458473));
		this.countryMap.put("Niger", new Location(0.295646396495, 15.9032466977, 11.6601671412, 23.4716684026));
		this.countryMap.put("Nigeria", new Location(2.69170169436, 14.5771777686, 4.24059418377, 13.8659239771));
		this.countryMap.put("Nicaragua", new Location(-87.6684934151, -83.147219001, 10.7268390975, 15.0162671981));
		this.countryMap.put("Netherlands", new Location(3.31497114423, 7.09205325687, 50.803721015, 53.5104033474));
		this.countryMap.put("Norway", new Location(4.99207807783, 31.29341841, 58.0788841824, 80.6571442736));
		this.countryMap.put("Nepal", new Location(80.0884245137, 88.1748043151, 26.3978980576, 30.4227169866));
		this.countryMap.put("New Zealand", new Location(166.509144322, 178.517093541, -46.641235447, -34.4506617165));
		this.countryMap.put("Oman", new Location(52.0000098, 59.8080603372, 16.6510511337, 26.3959343531));
		this.countryMap.put("Pakistan", new Location(60.8742484882, 77.8374507995, 23.6919650335, 37.1330309108));
		this.countryMap.put("Panama", new Location(-82.9657830472, -77.2425664944, 7.2205414901, 9.61161001224));
		this.countryMap.put("Peru", new Location(-81.4109425524, -68.6650797187, -18.3479753557, -0.0572054988649));
		this.countryMap.put("Philippines", new Location(117.17427453, 126.537423944, 5.58100332277, 18.5052273625));
		this.countryMap.put("Papua New Guinea", new Location(141.000210403, 156.019965448, -10.6524760881, -2.50000212973));
		this.countryMap.put("Poland", new Location(14.0745211117, 24.0299857927, 49.0273953314, 54.8515359564));
		this.countryMap.put("Puerto Rico", new Location(-67.2424275377, -65.5910037909, 17.946553453, 18.5206011011));
		this.countryMap.put("North Korea", new Location(124.265624628, 130.780007359, 37.669070543, 42.9853868678));
		this.countryMap.put("Portugal", new Location(-9.52657060387, -6.3890876937, 36.838268541, 42.280468655));
		this.countryMap.put("Paraguay", new Location(-62.6850571357, -54.2929595608, -27.5484990374, -19.3427466773));
		this.countryMap.put("Qatar", new Location(50.7439107603, 51.6067004738, 24.5563308782, 26.1145820175));
		this.countryMap.put("Romania", new Location(20.2201924985, 29.62654341, 43.6884447292, 48.2208812526));
		this.countryMap.put("Russia", new Location(-180.0, 180.0, 41.151416124, 81.2504));
		this.countryMap.put("Rwanda", new Location(29.0249263852, 30.8161348813, -2.91785776125, -1.13465911215));
		this.countryMap.put("Saudi Arabia", new Location(34.6323360532, 55.6666593769, 16.3478913436, 32.161008816));
		this.countryMap.put("Sudan", new Location(21.93681, 38.4100899595, 8.61972971293, 22.0));
		this.countryMap.put("South Sudan", new Location(23.8869795809, 35.2980071182, 3.50917, 12.2480077571));
		this.countryMap.put("Senegal", new Location(-17.6250426905, -11.4678991358, 12.332089952, 16.5982636581));
		this.countryMap.put("Solomon Islands", new Location(156.491357864, 162.398645868, -10.8263672828, -6.59933847415));
		this.countryMap.put("Sierra Leone", new Location(-13.2465502588, -10.2300935531, 6.78591685631, 10.0469839543));
		this.countryMap.put("El Salvador", new Location(-90.0955545723, -87.7235029772, 13.1490168319, 14.4241327987));
		this.countryMap.put("Somalia", new Location(40.98105, 51.13387, -1.68325, 12.02464));
		this.countryMap.put("Serbia", new Location(18.82982, 22.9860185076, 42.2452243971, 46.1717298447));
		this.countryMap.put("Suriname", new Location(-58.0446943834, -53.9580446031, 1.81766714112, 6.0252914494));
		this.countryMap.put("Slovakia", new Location(16.8799829444, 22.5581376482, 47.7584288601, 49.5715740017));
		this.countryMap.put("Slovenia", new Location(13.6981099789, 16.5648083839, 45.4523163926, 46.8523859727));
		this.countryMap.put("Sweden", new Location(11.0273686052, 23.9033785336, 55.3617373725, 69.1062472602));
		this.countryMap.put("Swaziland", new Location(30.6766085141, 32.0716654803, -27.2858794085, -25.660190525));
		this.countryMap.put("Syria", new Location(35.7007979673, 42.3495910988, 32.312937527, 37.2298725449));
		this.countryMap.put("Chad", new Location(13.5403935076, 23.88689, 7.42192454674, 23.40972));
		this.countryMap.put("Togo", new Location(-0.0497847151599, 1.86524051271, 5.92883738853, 11.0186817489));
		this.countryMap.put("Thailand", new Location(97.3758964376, 105.589038527, 5.69138418215, 20.4178496363));
		this.countryMap.put("Tajikistan", new Location(67.4422196796, 74.9800024759, 36.7381712916, 40.9602133245));
		this.countryMap.put("Turkmenistan", new Location(52.5024597512, 66.5461503437, 35.2706639674, 42.7515510117));
		this.countryMap.put("East Timor", new Location(124.968682489, 127.335928176, -9.39317310958, -8.27334482181));
		this.countryMap.put("Trinidad and Tobago", new Location(-61.95, -60.895, 10.0, 10.89));
		this.countryMap.put("Tunisia", new Location(7.52448164229, 11.4887874691, 30.3075560572, 37.3499944118));
		this.countryMap.put("Turkey", new Location(26.0433512713, 44.7939896991, 35.8215347357, 42.1414848903));
		this.countryMap.put("Taiwan", new Location(120.106188593, 121.951243931, 21.9705713974, 25.2954588893));
		this.countryMap.put("Tanzania", new Location(29.3399975929, 40.31659, -11.7209380022, -0.95));
		this.countryMap.put("Uganda", new Location(29.5794661801, 35.03599, -1.44332244223, 4.24988494736));
		this.countryMap.put("Ukraine", new Location(22.0856083513, 40.0807890155, 44.3614785833, 52.3350745713));
		this.countryMap.put("Uruguay", new Location(-58.4270741441, -53.209588996, -34.9526465797, -30.1096863746));
		this.countryMap.put("United States", new Location(-171.791110603, -66.96466, 18.91619, 71.3577635769));
		this.countryMap.put("Uzbekistan", new Location(55.9289172707, 73.055417108, 37.1449940049, 45.5868043076));
		this.countryMap.put("Venezuela", new Location(-73.3049515449, -59.7582848782, 0.724452215982, 12.1623070337));
		this.countryMap.put("Vietnam", new Location(102.170435826, 109.33526981, 8.59975962975, 23.3520633001));
		this.countryMap.put("Vanuatu", new Location(166.629136998, 167.844876744, -16.5978496233, -14.6264970842));
		this.countryMap.put("West Bank", new Location(34.9274084816, 35.5456653175, 31.3534353704, 32.5325106878));
		this.countryMap.put("Yemen", new Location(42.6048726743, 53.1085726255, 12.5859504257, 19.0000033635));
		this.countryMap.put("South Africa", new Location(16.3449768409, 32.830120477, -34.8191663551, -22.0913127581));
		this.countryMap.put("Zambia", new Location(21.887842645, 33.4856876971, -17.9612289364, -8.23825652429));
		this.countryMap.put("Zimbabwe", new Location(25.2642257016, 32.8498608742, -22.2716118303, -15.5077869605));
	}

	@Override
	public void detect(String text, LocationCollection locations) {
		text = text.toLowerCase();
		Iterator<Entry<String, Location>> it = countryMap.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, Location> entry = it.next();
			if (text.contains(entry.getKey().toLowerCase())) {
				Location l = entry.getValue();
				l.setProbability(0.9);
				LoggerFactory.getLogger(getClass()).debug("Parsed location " + l + " from DumbCountryDetector.");
				locations.add(l);
			}
		}
	}
	
}
