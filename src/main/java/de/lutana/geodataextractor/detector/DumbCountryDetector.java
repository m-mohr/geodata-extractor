package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Just a dumb country detector for the captions.
 * 
 * Using this to test the framework until we have something useful implemented.
 * 
 * @author Matthias Mohr
 */
public class DumbCountryDetector implements TextDetector {
	
	private final HashMap<String, Location> countryMap;
	
	public DumbCountryDetector() {
		this.countryMap = new HashMap<>();
		this.countryMap.put("Afghanistan", new Location(60.5284298033, 29.318572496, 75.1580277851, 38.4862816432));
		this.countryMap.put("Angola", new Location(11.6400960629, -17.9306364885, 24.0799052263, -4.43802336998));
		this.countryMap.put("Albania", new Location(19.3044861183, 39.624997667, 21.0200403175, 42.6882473822));
		this.countryMap.put("United Arab Emirates", new Location(51.5795186705, 22.4969475367, 56.3968473651, 26.055464179));
		this.countryMap.put("Argentina", new Location(-73.4154357571, -55.25, -53.628348965, -21.8323104794));
		this.countryMap.put("Armenia", new Location(43.5827458026, 38.7412014837, 46.5057198423, 41.2481285671));
		this.countryMap.put("Antarctica", new Location(-180.0, -90.0, 180.0, -63.2706604895));
		this.countryMap.put("French Southern and Antarctic Lands", new Location(68.72, -49.775, 70.56, -48.625));
		this.countryMap.put("Australia", new Location(113.338953078, -43.6345972634, 153.569469029, -10.6681857235));
		this.countryMap.put("Austria", new Location(9.47996951665, 46.4318173285, 16.9796667823, 49.0390742051));
		this.countryMap.put("Azerbaijan", new Location(44.7939896991, 38.2703775091, 50.3928210793, 41.8606751572));
		this.countryMap.put("Burundi", new Location(29.0249263852, -4.49998341229, 30.752262811, -2.34848683025));
		this.countryMap.put("Belgium", new Location(2.51357303225, 49.5294835476, 6.15665815596, 51.4750237087));
		this.countryMap.put("Benin", new Location(0.772335646171, 6.14215770103, 3.79711225751, 12.2356358912));
		this.countryMap.put("Burkina Faso", new Location(-5.47056494793, 9.61083486576, 2.17710778159, 15.1161577418));
		this.countryMap.put("Bangladesh", new Location(88.0844222351, 20.670883287, 92.6727209818, 26.4465255803));
		this.countryMap.put("Bulgaria", new Location(22.3805257504, 41.2344859889, 28.5580814959, 44.2349230007));
		this.countryMap.put("Bahamas", new Location(-78.98, 23.71, -77.0, 27.04));
		this.countryMap.put("Bosnia and Herzegovina", new Location(15.7500260759, 42.65, 19.59976, 45.2337767604));
		this.countryMap.put("Belarus", new Location(23.1994938494, 51.3195034857, 32.6936430193, 56.1691299506));
		this.countryMap.put("Belize", new Location(-89.2291216703, 15.8869375676, -88.1068129138, 18.4999822047));
		this.countryMap.put("Bolivia", new Location(-69.5904237535, -22.8729187965, -57.4983711412, -9.76198780685));
		this.countryMap.put("Brazil", new Location(-73.9872354804, -33.7683777809, -34.7299934555, 5.24448639569));
		this.countryMap.put("Brunei", new Location(114.204016555, 4.007636827, 115.450710484, 5.44772980389));
		this.countryMap.put("Bhutan", new Location(88.8142484883, 26.7194029811, 92.1037117859, 28.2964385035));
		this.countryMap.put("Botswana", new Location(19.8954577979, -26.8285429827, 29.4321883481, -17.6618156877));
		this.countryMap.put("Central African Republic", new Location(14.4594071794, 2.2676396753, 27.3742261085, 11.1423951278));
		this.countryMap.put("Canada", new Location(-140.99778, 41.6751050889, -52.6480987209, 83.23324));
		this.countryMap.put("Switzerland", new Location(6.02260949059, 45.7769477403, 10.4427014502, 47.8308275417));
		this.countryMap.put("Chile", new Location(-75.6443953112, -55.61183, -66.95992, -17.5800118954));
		this.countryMap.put("China", new Location(73.6753792663, 18.197700914, 135.026311477, 53.4588044297));
		this.countryMap.put("Ivory Coast", new Location(-8.60288021487, 4.33828847902, -2.56218950033, 10.5240607772));
		this.countryMap.put("Cameroon", new Location(8.48881554529, 1.72767263428, 16.0128524106, 12.8593962671));
		this.countryMap.put("Congo (Kinshasa)", new Location(12.1823368669, -13.2572266578, 31.1741492042, 5.25608775474));
		this.countryMap.put("Congo (Brazzaville)", new Location(11.0937728207, -5.03798674888, 18.4530652198, 3.72819651938));
		this.countryMap.put("Colombia", new Location(-78.9909352282, -4.29818694419, -66.8763258531, 12.4373031682));
		this.countryMap.put("Costa Rica", new Location(-85.94172543, 8.22502798099, -82.5461962552, 11.2171192489));
		this.countryMap.put("Cuba", new Location(-84.9749110583, 19.8554808619, -74.1780248685, 23.1886107447));
		this.countryMap.put("Cyprus", new Location(32.2566671079, 34.5718694118, 34.0048808123, 35.1731247015));
		this.countryMap.put("Czech Rep.", new Location(12.2401111182, 48.5553052842, 18.8531441586, 51.1172677679));
		this.countryMap.put("Germany", new Location(5.98865807458, 47.3024876979, 15.0169958839, 54.983104153));
		this.countryMap.put("Djibouti", new Location(41.66176, 10.9268785669, 43.3178524107, 12.6996385767));
		this.countryMap.put("Denmark", new Location(8.08997684086, 54.8000145534, 12.6900061378, 57.730016588));
		this.countryMap.put("Dominican Rep.", new Location(-71.9451120673, 17.598564358, -68.3179432848, 19.8849105901));
		this.countryMap.put("Algeria", new Location(-8.68439978681, 19.0573642034, 11.9995056495, 37.1183806422));
		this.countryMap.put("Ecuador", new Location(-80.9677654691, -4.95912851321, -75.2337227037, 1.3809237736));
		this.countryMap.put("Egypt", new Location(24.70007, 22.0, 36.86623, 31.58568));
		this.countryMap.put("Eritrea", new Location(36.3231889178, 12.4554157577, 43.0812260272, 17.9983074));
		this.countryMap.put("Spain", new Location(-9.39288367353, 35.946850084, 3.03948408368, 43.7483377142));
		this.countryMap.put("Estonia", new Location(23.3397953631, 57.4745283067, 28.1316992531, 59.6110903998));
		this.countryMap.put("Ethiopia", new Location(32.95418, 3.42206, 47.78942, 14.95943));
		this.countryMap.put("Finland", new Location(20.6455928891, 59.846373196, 31.5160921567, 70.1641930203));
		this.countryMap.put("Fiji", new Location(-180.0, -18.28799, 180.0, -16.0208822567));
		this.countryMap.put("Falkland Islands", new Location(-61.2, -52.3, -57.75, -51.1));
		this.countryMap.put("France", new Location(-54.5247541978, 2.05338918702, 9.56001631027, 51.1485061713));
		this.countryMap.put("Gabon", new Location(8.79799563969, -3.97882659263, 14.4254557634, 2.32675751384));
		this.countryMap.put("United Kingdom", new Location(-7.57216793459, 49.959999905, 1.68153079591, 58.6350001085));
		this.countryMap.put("Georgia", new Location(39.9550085793, 41.0644446885, 46.6379081561, 43.553104153));
		this.countryMap.put("Ghana", new Location(-3.24437008301, 4.71046214438, 1.0601216976, 11.0983409693));
		this.countryMap.put("Guinea", new Location(-15.1303112452, 7.3090373804, -7.83210038902, 12.5861829696));
		this.countryMap.put("Gambia", new Location(-16.8415246241, 13.1302841252, -13.8449633448, 13.8764918075));
		this.countryMap.put("Guinea Bissau", new Location(-16.6774519516, 11.0404116887, -13.7004760401, 12.6281700708));
		this.countryMap.put("Equatorial Guinea", new Location(9.3056132341, 1.01011953369, 11.285078973, 2.28386607504));
		this.countryMap.put("Greece", new Location(20.1500159034, 34.9199876979, 26.6041955909, 41.8269046087));
		this.countryMap.put("Greenland", new Location(-73.297, 60.03676, -12.20855, 83.64513));
		this.countryMap.put("Guatemala", new Location(-92.2292486234, 13.7353376327, -88.2250227526, 17.8193260767));
		this.countryMap.put("Guyana", new Location(-61.4103029039, 1.26808828369, -56.5393857489, 8.36703481692));
		this.countryMap.put("Honduras", new Location(-89.3533259753, 12.9846857772, -83.147219001, 16.0054057886));
		this.countryMap.put("Croatia", new Location(13.6569755388, 42.47999136, 19.3904757016, 46.5037509222));
		this.countryMap.put("Haiti", new Location(-74.4580336168, 18.0309927434, -71.6248732164, 19.9156839055));
		this.countryMap.put("Hungary", new Location(16.2022982113, 45.7594811061, 22.710531447, 48.6238540716));
		this.countryMap.put("Indonesia", new Location(95.2930261576, -10.3599874813, 141.03385176, 5.47982086834));
		this.countryMap.put("India", new Location(68.1766451354, 7.96553477623, 97.4025614766, 35.4940095078));
		this.countryMap.put("Ireland", new Location(-9.97708574059, 51.6693012559, -6.03298539878, 55.1316222195));
		this.countryMap.put("Iran", new Location(44.1092252948, 25.0782370061, 63.3166317076, 39.7130026312));
		this.countryMap.put("Iraq", new Location(38.7923405291, 29.0990251735, 48.5679712258, 37.3852635768));
		this.countryMap.put("Iceland", new Location(-24.3261840479, 63.4963829617, -13.609732225, 66.5267923041));
		this.countryMap.put("Israel", new Location(34.2654333839, 29.5013261988, 35.8363969256, 33.2774264593));
		this.countryMap.put("Italy", new Location(6.7499552751, 36.619987291, 18.4802470232, 47.1153931748));
		this.countryMap.put("Jamaica", new Location(-78.3377192858, 17.7011162379, -76.1996585761, 18.5242184514));
		this.countryMap.put("Jordan", new Location(34.9226025734, 29.1974946152, 39.1954683774, 33.3786864284));
		this.countryMap.put("Japan", new Location(129.408463169, 31.0295791692, 145.543137242, 45.5514834662));
		this.countryMap.put("Kazakhstan", new Location(46.4664457538, 40.6623245306, 87.3599703308, 55.3852501491));
		this.countryMap.put("Kenya", new Location(33.8935689697, -4.67677, 41.8550830926, 5.506));
		this.countryMap.put("Kyrgyzstan", new Location(69.464886916, 39.2794632025, 80.2599902689, 43.2983393418));
		this.countryMap.put("Cambodia", new Location(102.3480994, 10.4865436874, 107.614547968, 14.5705838078));
		this.countryMap.put("South Korea", new Location(126.117397903, 34.3900458847, 129.468304478, 38.6122429469));
		this.countryMap.put("Kuwait", new Location(46.5687134133, 28.5260627304, 48.4160941913, 30.0590699326));
		this.countryMap.put("Laos", new Location(100.115987583, 13.88109101, 107.564525181, 22.4647531194));
		this.countryMap.put("Lebanon", new Location(35.1260526873, 33.0890400254, 36.6117501157, 34.6449140488));
		this.countryMap.put("Liberia", new Location(-11.4387794662, 4.35575511313, -7.53971513511, 8.54105520267));
		this.countryMap.put("Libya", new Location(9.31941084152, 19.58047, 25.16482, 33.1369957545));
		this.countryMap.put("Sri Lanka", new Location(79.6951668639, 5.96836985923, 81.7879590189, 9.82407766361));
		this.countryMap.put("Lesotho", new Location(26.9992619158, -30.6451058896, 29.3251664568, -28.6475017229));
		this.countryMap.put("Lithuania", new Location(21.0558004086, 53.9057022162, 26.5882792498, 56.3725283881));
		this.countryMap.put("Luxembourg", new Location(5.67405195478, 49.4426671413, 6.24275109216, 50.1280516628));
		this.countryMap.put("Latvia", new Location(21.0558004086, 55.61510692, 28.1767094256, 57.9701569688));
		this.countryMap.put("Morocco", new Location(-17.0204284327, 21.4207341578, -1.12455115397, 35.7599881048));
		this.countryMap.put("Moldova", new Location(26.6193367856, 45.4882831895, 30.0246586443, 48.4671194525));
		this.countryMap.put("Madagascar", new Location(43.2541870461, -25.6014344215, 50.4765368996, -12.0405567359));
		this.countryMap.put("Mexico", new Location(-117.12776, 14.5388286402, -86.811982388, 32.72083));
		this.countryMap.put("Macedonia", new Location(20.46315, 40.8427269557, 22.9523771502, 42.3202595078));
		this.countryMap.put("Mali", new Location(-12.1707502914, 10.0963607854, 4.27020999514, 24.9745740829));
		this.countryMap.put("Myanmar", new Location(92.3032344909, 9.93295990645, 101.180005324, 28.335945136));
		this.countryMap.put("Montenegro", new Location(18.45, 41.87755, 20.3398, 43.52384));
		this.countryMap.put("Mongolia", new Location(87.7512642761, 41.5974095729, 119.772823928, 52.0473660345));
		this.countryMap.put("Mozambique", new Location(30.1794812355, -26.7421916643, 40.7754752948, -10.3170960425));
		this.countryMap.put("Mauritania", new Location(-17.0634232243, 14.6168342147, -4.92333736817, 27.3957441269));
		this.countryMap.put("Malawi", new Location(32.6881653175, -16.8012997372, 35.7719047381, -9.23059905359));
		this.countryMap.put("Malaysia", new Location(100.085756871, 0.773131415201, 119.181903925, 6.92805288332));
		this.countryMap.put("Namibia", new Location(11.7341988461, -29.045461928, 25.0844433937, -16.9413428687));
		this.countryMap.put("New Caledonia", new Location(164.029605748, -22.3999760881, 167.120011428, -20.1056458473));
		this.countryMap.put("Niger", new Location(0.295646396495, 11.6601671412, 15.9032466977, 23.4716684026));
		this.countryMap.put("Nigeria", new Location(2.69170169436, 4.24059418377, 14.5771777686, 13.8659239771));
		this.countryMap.put("Nicaragua", new Location(-87.6684934151, 10.7268390975, -83.147219001, 15.0162671981));
		this.countryMap.put("Netherlands", new Location(3.31497114423, 50.803721015, 7.09205325687, 53.5104033474));
		this.countryMap.put("Norway", new Location(4.99207807783, 58.0788841824, 31.29341841, 80.6571442736));
		this.countryMap.put("Nepal", new Location(80.0884245137, 26.3978980576, 88.1748043151, 30.4227169866));
		this.countryMap.put("New Zealand", new Location(166.509144322, -46.641235447, 178.517093541, -34.4506617165));
		this.countryMap.put("Oman", new Location(52.0000098, 16.6510511337, 59.8080603372, 26.3959343531));
		this.countryMap.put("Pakistan", new Location(60.8742484882, 23.6919650335, 77.8374507995, 37.1330309108));
		this.countryMap.put("Panama", new Location(-82.9657830472, 7.2205414901, -77.2425664944, 9.61161001224));
		this.countryMap.put("Peru", new Location(-81.4109425524, -18.3479753557, -68.6650797187, -0.0572054988649));
		this.countryMap.put("Philippines", new Location(117.17427453, 5.58100332277, 126.537423944, 18.5052273625));
		this.countryMap.put("Papua New Guinea", new Location(141.000210403, -10.6524760881, 156.019965448, -2.50000212973));
		this.countryMap.put("Poland", new Location(14.0745211117, 49.0273953314, 24.0299857927, 54.8515359564));
		this.countryMap.put("Puerto Rico", new Location(-67.2424275377, 17.946553453, -65.5910037909, 18.5206011011));
		this.countryMap.put("North Korea", new Location(124.265624628, 37.669070543, 130.780007359, 42.9853868678));
		this.countryMap.put("Portugal", new Location(-9.52657060387, 36.838268541, -6.3890876937, 42.280468655));
		this.countryMap.put("Paraguay", new Location(-62.6850571357, -27.5484990374, -54.2929595608, -19.3427466773));
		this.countryMap.put("Qatar", new Location(50.7439107603, 24.5563308782, 51.6067004738, 26.1145820175));
		this.countryMap.put("Romania", new Location(20.2201924985, 43.6884447292, 29.62654341, 48.2208812526));
		this.countryMap.put("Russia", new Location(-180.0, 41.151416124, 180.0, 81.2504));
		this.countryMap.put("Rwanda", new Location(29.0249263852, -2.91785776125, 30.8161348813, -1.13465911215));
		this.countryMap.put("Saudi Arabia", new Location(34.6323360532, 16.3478913436, 55.6666593769, 32.161008816));
		this.countryMap.put("Sudan", new Location(21.93681, 8.61972971293, 38.4100899595, 22.0));
		this.countryMap.put("South Sudan", new Location(23.8869795809, 3.50917, 35.2980071182, 12.2480077571));
		this.countryMap.put("Senegal", new Location(-17.6250426905, 12.332089952, -11.4678991358, 16.5982636581));
		this.countryMap.put("Solomon Islands", new Location(156.491357864, -10.8263672828, 162.398645868, -6.59933847415));
		this.countryMap.put("Sierra Leone", new Location(-13.2465502588, 6.78591685631, -10.2300935531, 10.0469839543));
		this.countryMap.put("El Salvador", new Location(-90.0955545723, 13.1490168319, -87.7235029772, 14.4241327987));
		this.countryMap.put("Somalia", new Location(40.98105, -1.68325, 51.13387, 12.02464));
		this.countryMap.put("Serbia", new Location(18.82982, 42.2452243971, 22.9860185076, 46.1717298447));
		this.countryMap.put("Suriname", new Location(-58.0446943834, 1.81766714112, -53.9580446031, 6.0252914494));
		this.countryMap.put("Slovakia", new Location(16.8799829444, 47.7584288601, 22.5581376482, 49.5715740017));
		this.countryMap.put("Slovenia", new Location(13.6981099789, 45.4523163926, 16.5648083839, 46.8523859727));
		this.countryMap.put("Sweden", new Location(11.0273686052, 55.3617373725, 23.9033785336, 69.1062472602));
		this.countryMap.put("Swaziland", new Location(30.6766085141, -27.2858794085, 32.0716654803, -25.660190525));
		this.countryMap.put("Syria", new Location(35.7007979673, 32.312937527, 42.3495910988, 37.2298725449));
		this.countryMap.put("Chad", new Location(13.5403935076, 7.42192454674, 23.88689, 23.40972));
		this.countryMap.put("Togo", new Location(-0.0497847151599, 5.92883738853, 1.86524051271, 11.0186817489));
		this.countryMap.put("Thailand", new Location(97.3758964376, 5.69138418215, 105.589038527, 20.4178496363));
		this.countryMap.put("Tajikistan", new Location(67.4422196796, 36.7381712916, 74.9800024759, 40.9602133245));
		this.countryMap.put("Turkmenistan", new Location(52.5024597512, 35.2706639674, 66.5461503437, 42.7515510117));
		this.countryMap.put("East Timor", new Location(124.968682489, -9.39317310958, 127.335928176, -8.27334482181));
		this.countryMap.put("Trinidad and Tobago", new Location(-61.95, 10.0, -60.895, 10.89));
		this.countryMap.put("Tunisia", new Location(7.52448164229, 30.3075560572, 11.4887874691, 37.3499944118));
		this.countryMap.put("Turkey", new Location(26.0433512713, 35.8215347357, 44.7939896991, 42.1414848903));
		this.countryMap.put("Taiwan", new Location(120.106188593, 21.9705713974, 121.951243931, 25.2954588893));
		this.countryMap.put("Tanzania", new Location(29.3399975929, -11.7209380022, 40.31659, -0.95));
		this.countryMap.put("Uganda", new Location(29.5794661801, -1.44332244223, 35.03599, 4.24988494736));
		this.countryMap.put("Ukraine", new Location(22.0856083513, 44.3614785833, 40.0807890155, 52.3350745713));
		this.countryMap.put("Uruguay", new Location(-58.4270741441, -34.9526465797, -53.209588996, -30.1096863746));
		this.countryMap.put("United States", new Location(-171.791110603, 18.91619, -66.96466, 71.3577635769));
		this.countryMap.put("Uzbekistan", new Location(55.9289172707, 37.1449940049, 73.055417108, 45.5868043076));
		this.countryMap.put("Venezuela", new Location(-73.3049515449, 0.724452215982, -59.7582848782, 12.1623070337));
		this.countryMap.put("Vietnam", new Location(102.170435826, 8.59975962975, 109.33526981, 23.3520633001));
		this.countryMap.put("Vanuatu", new Location(166.629136998, -16.5978496233, 167.844876744, -14.6264970842));
		this.countryMap.put("West Bank", new Location(34.9274084816, 31.3534353704, 35.5456653175, 32.5325106878));
		this.countryMap.put("Yemen", new Location(42.6048726743, 12.5859504257, 53.1085726255, 19.0000033635));
		this.countryMap.put("South Africa", new Location(16.3449768409, -34.8191663551, 32.830120477, -22.0913127581));
		this.countryMap.put("Zambia", new Location(21.887842645, -17.9612289364, 33.4856876971, -8.23825652429));
		this.countryMap.put("Zimbabwe", new Location(25.2642257016, -22.2716118303, 32.8498608742, -15.5077869605));
	}

	@Override
	public void detect(String text, LocationCollection locations) {
		text = text.toLowerCase();
		Iterator<Entry<String, Location>> it = countryMap.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, Location> entry = it.next();
			if (text.contains(entry.getKey().toLowerCase())) {
				locations.add(entry.getValue());
			}
		}
	}
	
}
