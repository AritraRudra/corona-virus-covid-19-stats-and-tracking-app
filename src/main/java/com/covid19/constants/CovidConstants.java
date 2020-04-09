package com.covid19.constants;

public class CovidConstants {

    private static final String TIME_SERIES_URI = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/";

    public static final String CONFIRMED_INFECTED_URI = TIME_SERIES_URI + "time_series_covid19_confirmed_global.csv";

    public static final String CONFIRMED_DEATHS_URI = TIME_SERIES_URI + "time_series_covid19_deaths_global.csv";

    public static final String CONFIRMED_RECOVERED_URI = TIME_SERIES_URI + "time_series_covid19_recovered_global.csv";

    public static final String PROVINCE_STATE = "Province/State";

    public static final String COUNTRY_REGION = "Country/Region";

    public static final String LATITUDE = "Lat";

    public static final String LONGITUDE = "Long";

}
