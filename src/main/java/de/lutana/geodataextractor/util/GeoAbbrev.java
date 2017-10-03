package de.lutana.geodataextractor.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GeoAbbrev {

	private static Map<String, String> iso3ToIso2Map;
	private static Map<String, String> iso2ToIso3Map;
	private static Map<String, String> iocMap;
	private static Map<String, String> australiaStatesMap;
	private static Map<String, String> usStatesMap;

	public boolean existsIso2Code(String iso2CountryCode) {
		initIsoMaps();
		return iso2ToIso3Map.containsKey(iso2CountryCode.toUpperCase());
	}

	public boolean existsIso3Code(String iso3CountryCode) {
		initIsoMaps();
		return iso3ToIso2Map.containsKey(iso3CountryCode.toUpperCase());
	}
	
	/**
	 * Converts ISO 3166-1 alpha-2 codes to their ISO 3166-1 alpha-3 equivalents.
	 * 
	 * @param iso2CountryCode
	 * @return 
	 */
	public String convertIso2CodeToIso3(String iso2CountryCode) {
		initIsoMaps();
		return iso2ToIso3Map.get(iso2CountryCode.toUpperCase());
	}

	/**
	 * Converts ISO 3166-1 alpha-3 codes to their ISO 3166-1 alpha-2 equivalents.
	 * 
	 * @param iso3CountryCode
	 * @return 
	 */
	public String convertIso3CodeToIso2(String iso3CountryCode) {
		initIsoMaps();
		return iso3ToIso2Map.get(iso3CountryCode.toUpperCase());
	}
	
	/**
	 * Country codes of the International Olympic Committee.
	 * 
	 * They are sometimes used for Sports, e.g. GER for Germany is widely used and
	 * probably more common than DEU from ISO3 codes.
	 * 
	 * @param code
	 * @return 
	 */
	public String convertIocCodeToIso2(String code) {
		initIocMap();
		return iocMap.get(code.toUpperCase());
	}
	
	/**
	 * Converts the Australian State Abbreviations to their English State names.
	 * 
	 * @param code
	 * @return 
	 */
	public String convertAustraliaStateToName(String code) {
		initAustraliaStatesMap();
		return australiaStatesMap.get(code.toUpperCase());
	}
	
	/**
	 * Converts the US State Abbreviations to their English State names.
	 * 
	 * @param code
	 * @return 
	 */
	public String convertUsStateToName(String code) {
		initUsStatesMap();
		return usStatesMap.get(code.toUpperCase());
	}
	
	private static void initIsoMaps() {
		if (iso2ToIso3Map == null || iso3ToIso2Map == null) {
			String[] countries = Locale.getISOCountries();
			iso2ToIso3Map = new HashMap<>(countries.length);
			iso3ToIso2Map = new HashMap<>(countries.length);
			for (String iso2 : countries) {
				Locale locale = new Locale("", iso2);
				iso2ToIso3Map.put(iso2.toUpperCase(), locale.getISO3Country().toUpperCase());
				iso3ToIso2Map.put(locale.getISO3Country().toUpperCase(), iso2.toUpperCase());
			}
		}
	}
	
	private static void initUsStatesMap() {
		if (usStatesMap != null) {
			return;
		}
		usStatesMap = new HashMap<>();
		usStatesMap.put("AL", "ALABAMA");
		usStatesMap.put("AK", "ALASKA");
		usStatesMap.put("AZ", "ARIZONA");
		usStatesMap.put("AR", "ARKANSAS");
		usStatesMap.put("CA", "CALIFORNIA");
		usStatesMap.put("CO", "COLORADO");
		usStatesMap.put("CT", "CONNECTICUT");
		usStatesMap.put("DE", "DELAWARE");
		usStatesMap.put("FL", "FLORIDA");
		usStatesMap.put("GA", "GEORGIA");
		usStatesMap.put("HI", "HAWAII");
		usStatesMap.put("ID", "IDAHO");
		usStatesMap.put("IL", "ILLINOIS");
		usStatesMap.put("IN", "INDIANA");
		usStatesMap.put("IA", "IOWA");
		usStatesMap.put("KS", "KANSAS");
		usStatesMap.put("KY", "KENTUCKY");
		usStatesMap.put("LA", "LOUISIANA");
		usStatesMap.put("ME", "MAINE");
		usStatesMap.put("MD", "MARYLAND");
		usStatesMap.put("MA", "MASSACHUSETTS");
		usStatesMap.put("MI", "MICHIGAN");
		usStatesMap.put("MN", "MINNESOTA");
		usStatesMap.put("MS", "MISSISSIPPI");
		usStatesMap.put("MO", "MISSOURI");
		usStatesMap.put("MT", "MONTANA");
		usStatesMap.put("NE", "NEBRASKA");
		usStatesMap.put("NV", "NEVADA");
		usStatesMap.put("NH", "NEW HAMPSHIRE");
		usStatesMap.put("NJ", "NEW JERSEY");
		usStatesMap.put("NM", "NEW MEXICO");
		usStatesMap.put("NY", "NEW YORK");
		usStatesMap.put("NC", "NORTH CAROLINA");
		usStatesMap.put("ND", "NORTH DAKOTA");
		usStatesMap.put("OH", "OHIO");
		usStatesMap.put("OK", "OKLAHOMA");
		usStatesMap.put("OR", "OREGON");
		usStatesMap.put("PA", "PENNSYLVANIA");
		usStatesMap.put("RI", "RHODE ISLAND");
		usStatesMap.put("SC", "SOUTH CAROLINA");
		usStatesMap.put("SD", "SOUTH DAKOTA");
		usStatesMap.put("TN", "TENNESSEE");
		usStatesMap.put("TX", "TEXAS");
		usStatesMap.put("UT", "UTAH");
		usStatesMap.put("VT", "VERMONT");
		usStatesMap.put("VA", "VIRGINIA");
		usStatesMap.put("WA", "WASHINGTON");
		usStatesMap.put("WV", "WEST VIRGINIA");
		usStatesMap.put("WI", "WISCONSIN");
		usStatesMap.put("WY", "WYOMING");
	}
	
	
	private static void initAustraliaStatesMap() {
		if (australiaStatesMap != null) {
			return;
		}
		australiaStatesMap = new HashMap<>();
		// Only the major states, ignoring small islands etc.
		australiaStatesMap.put("NSW", "New South Wales");
		australiaStatesMap.put("QLD", "Queensland");
		australiaStatesMap.put("SA", "South Australia");
		australiaStatesMap.put("TAS", "Tasmania");
		australiaStatesMap.put("VIC", "Victoria");
		australiaStatesMap.put("WA", "Western Australia");
		australiaStatesMap.put("ACT", "Australian Capital Territory");
		australiaStatesMap.put("NT", "Northern Territory");
	}
	
	private static void initIocMap() {
		if (iocMap != null) {
			return;
		}
		iocMap = new HashMap<>();
		iocMap.put("AFG", "AF");
		iocMap.put("ALB", "AL");
		iocMap.put("ALG", "DZ");
		iocMap.put("ASA", "AS");
		iocMap.put("AND", "AD");
		iocMap.put("ANG", "AO");
		iocMap.put("ANT", "AG");
		iocMap.put("ARG", "AR");
		iocMap.put("ARM", "AM");
		iocMap.put("ARU", "AW");
		iocMap.put("AUS", "AU");
		iocMap.put("AUT", "AT");
		iocMap.put("AZE", "AZ");
		iocMap.put("BAH", "BS");
		iocMap.put("BRN", "BH");
		iocMap.put("BAN", "BD");
		iocMap.put("BAR", "BB");
		iocMap.put("BLR", "BY");
		iocMap.put("BEL", "BE");
		iocMap.put("BIZ", "BZ");
		iocMap.put("BEN", "BJ");
		iocMap.put("BER", "BM");
		iocMap.put("BHU", "BT");
		iocMap.put("BOL", "BO");
		iocMap.put("BIH", "BA");
		iocMap.put("BOT", "BW");
		iocMap.put("BRA", "BR");
		iocMap.put("IVB", "VG");
		iocMap.put("BRU", "BN");
		iocMap.put("BUL", "BG");
		iocMap.put("BUR", "BF");
		iocMap.put("BDI", "BI");
		iocMap.put("CAM", "KH");
		iocMap.put("CMR", "CM");
		iocMap.put("CAN", "CA");
		iocMap.put("CPV", "CV");
		iocMap.put("CAY", "KY");
		iocMap.put("CAF", "CF");
		iocMap.put("CHA", "TD");
		iocMap.put("CHI", "CL");
		iocMap.put("CHN", "CN");
		iocMap.put("COL", "CO");
		iocMap.put("COM", "KM");
		iocMap.put("CGO", "CG");
		iocMap.put("COK", "CK");
		iocMap.put("CRC", "CR");
		iocMap.put("CIV", "CI");
		iocMap.put("CRO", "HR");
		iocMap.put("CUB", "CU");
		iocMap.put("CYP", "CY");
		iocMap.put("CZE", "CZ");
		iocMap.put("COD", "CD");
		iocMap.put("PRK", "KP");
		iocMap.put("DEN", "DK");
		iocMap.put("DJI", "DJ");
		iocMap.put("DMA", "DM");
		iocMap.put("DOM", "DO");
		iocMap.put("ECU", "EC");
		iocMap.put("EGY", "EG");
		iocMap.put("ESA", "SV");
		iocMap.put("GEQ", "GQ");
		iocMap.put("ERI", "ER");
		iocMap.put("EST", "EE");
		iocMap.put("ETH", "ET");
		iocMap.put("FRO", "FO");
		iocMap.put("FIJ", "FJ");
		iocMap.put("FIN", "FI");
		iocMap.put("FRA", "FR");
		iocMap.put("GAB", "GA");
		iocMap.put("GAM", "GM");
		iocMap.put("GEO", "GE");
		iocMap.put("GER", "DE");
		iocMap.put("GHA", "GH");
		iocMap.put("GRE", "GR");
		iocMap.put("GRN", "GD");
		iocMap.put("GUM", "GU");
		iocMap.put("GUA", "GT");
		iocMap.put("GUI", "GN");
		iocMap.put("GBS", "GW");
		iocMap.put("GUY", "GY");
		iocMap.put("HAI", "HT");
		iocMap.put("HON", "HN");
		iocMap.put("HKG", "HK");
		iocMap.put("HUN", "HU");
		iocMap.put("ISL", "IS");
		iocMap.put("IND", "IN");
		iocMap.put("INA", "ID");
		iocMap.put("IRI", "IR");
		iocMap.put("IRQ", "IQ");
		iocMap.put("IRL", "IE");
		iocMap.put("ISR", "IL");
		iocMap.put("ITA", "IT");
		iocMap.put("JAM", "JM");
		iocMap.put("JPN", "JP");
		iocMap.put("JOR", "JO");
		iocMap.put("KAZ", "KZ");
		iocMap.put("KEN", "KE");
		iocMap.put("KIR", "KI");
		iocMap.put("KUW", "KW");
		iocMap.put("KGZ", "KG");
		iocMap.put("LAO", "LA");
		iocMap.put("LAT", "LV");
		iocMap.put("LIB", "LB");
		iocMap.put("LES", "LS");
		iocMap.put("LBR", "LR");
		iocMap.put("LBA", "LY");
		iocMap.put("LIE", "LI");
		iocMap.put("LTU", "LT");
		iocMap.put("LUX", "LU");
		iocMap.put("MKD", "MK");
		iocMap.put("MAD", "MG");
		iocMap.put("MAW", "MW");
		iocMap.put("MAS", "MY");
		iocMap.put("MDV", "MV");
		iocMap.put("MLI", "ML");
		iocMap.put("MLT", "MT");
		iocMap.put("MHL", "MH");
		iocMap.put("MTN", "MR");
		iocMap.put("MRI", "MU");
		iocMap.put("MEX", "MX");
		iocMap.put("FSM", "FM");
		iocMap.put("MDA", "MD");
		iocMap.put("MON", "MC");
		iocMap.put("MGL", "MN");
		iocMap.put("MNE", "ME");
		iocMap.put("MAR", "MA");
		iocMap.put("MOZ", "MZ");
		iocMap.put("MYA", "MM");
		iocMap.put("NAM", "NA");
		iocMap.put("NRU", "NR");
		iocMap.put("NEP", "NP");
		iocMap.put("NED", "NL");
		iocMap.put("AHO", "AN");
		iocMap.put("NZL", "NZ");
		iocMap.put("NCA", "NI");
		iocMap.put("NIG", "NE");
		iocMap.put("NGR", "NG");
		iocMap.put("NOR", "NO");
		iocMap.put("OMA", "OM");
		iocMap.put("PAK", "PK");
		iocMap.put("PLW", "PW");
		iocMap.put("PLE", "PS");
		iocMap.put("PAN", "PA");
		iocMap.put("PNG", "PG");
		iocMap.put("PAR", "PY");
		iocMap.put("PER", "PE");
		iocMap.put("PHI", "PH");
		iocMap.put("POL", "PL");
		iocMap.put("POR", "PT");
		iocMap.put("PUR", "PR");
		iocMap.put("QAT", "QA");
		iocMap.put("KOR", "KR");
		iocMap.put("ROU", "RO");
		iocMap.put("RUS", "RU");
		iocMap.put("RWA", "RW");
		iocMap.put("SKN", "KN");
		iocMap.put("VIN", "VC");
		iocMap.put("SAM", "WS");
		iocMap.put("SMR", "SM");
		iocMap.put("STP", "ST");
		iocMap.put("KSA", "SA");
		iocMap.put("SEN", "SN");
		iocMap.put("SRB", "RS");
		iocMap.put("SEY", "SC");
		iocMap.put("SLE", "SL");
		iocMap.put("SIN", "SG");
		iocMap.put("SVK", "SK");
		iocMap.put("SLO", "SI");
		iocMap.put("SOL", "SB");
		iocMap.put("SOM", "SO");
		iocMap.put("RSA", "ZA");
		iocMap.put("SSD", "SS");
		iocMap.put("MLT", "MT");
		iocMap.put("ESP", "ES");
		iocMap.put("SRI", "LK");
		iocMap.put("LCA", "LC");
		iocMap.put("SUD", "SD");
		iocMap.put("SUR", "SR");
		iocMap.put("SWZ", "SZ");
		iocMap.put("SWE", "SE");
		iocMap.put("SUI", "CH");
		iocMap.put("SYR", "SY");
		iocMap.put("TPE", "TW");
		iocMap.put("TJK", "TJ");
		iocMap.put("THA", "TH");
		iocMap.put("TLS", "TL");
		iocMap.put("TOG", "TG");
		iocMap.put("TGA", "TO");
		iocMap.put("TRI", "TT");
		iocMap.put("TUN", "TN");
		iocMap.put("TUR", "TR");
		iocMap.put("TKM", "TM");
		iocMap.put("TUV", "TV");
		iocMap.put("UGA", "UG");
		iocMap.put("UKR", "UA");
		iocMap.put("UAE", "AE");
		iocMap.put("GBR", "GB");
		iocMap.put("TAN", "TZ");
		iocMap.put("USA", "US");
		iocMap.put("ISV", "VI");
		iocMap.put("URU", "UY");
		iocMap.put("UZB", "UZ");
		iocMap.put("VAN", "VU");
		iocMap.put("VEN", "VE");
		iocMap.put("VIE", "VN");
		iocMap.put("YEM", "YE");
		iocMap.put("ZAM", "ZM");
		iocMap.put("ZIM", "ZW");
	}
	
}
