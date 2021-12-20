package com.example.weatherapp

import android.media.Image
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class MainActivityViewModel: ViewModel() {
    val citiesList = mutableListOf<City>()

    var currentCity: City? = null
    val forecastData = mutableListOf<ConsolidatedWeather>()

    private val cities: MutableLiveData<List<City>> by lazy {
        MutableLiveData<List<City>>().also {
            loadCities()
        }
    }

    private val fullForecast: MutableLiveData<List<ConsolidatedWeather>> by lazy {
        MutableLiveData<List<ConsolidatedWeather>>().also {
            loadForecastData()
        }
    }

    fun getCities(): LiveData<List<City>> {
        return cities
    }

    private fun loadCities() {
        // Do an asynchronous operation to fetch users.
        GlobalScope.launch {
            val c1 = City("New York", 2459115)
            val c2 = City("Tokyo", 1118370)

            citiesList.add(c1)
            citiesList.add(c2)

            currentCity = c1

            cities.postValue(citiesList)
        }
    }

    private fun loadForecastData(){
        GlobalScope.launch {
            val apiResponse = URL("https://www.metaweather.com/api/location/${currentCity?.id}/").readText()
            val json = JSONObject(apiResponse)

            val forecast = json.getJSONArray("consolidated_weather")

            forecastData.clear()

            for (f in 0 until forecast.length()){
                val day = forecast.getJSONObject(f)

                val dayData = ConsolidatedWeather(
                    id = day.getLong("id"),
                    applicableDate = day.get("applicable_date") as String,
                    weatherStateName = day.getString("weather_state_name"),
                    weatherStateAbbr = day.getString("weather_state_abbr"),
                    windSpeed = day.getDouble("wind_speed"),
                    windDirection = day.getDouble("wind_direction"),
                    windDirectionCompass = day.getString("wind_direction_compass"),
                    minTemp = day.getInt("min_temp"),
                    maxTemp = day.getInt("max_temp"),
                    theTemp = day.getInt("the_temp"),
                    airPressure = day.getDouble("air_pressure"),
                    humidity = day.getDouble("humidity"),
                    visibility = day.getDouble("visibility"),
                    predictability = day.getInt("predictability")
                )

                forecastData.add(dayData)
            }

            fullForecast.postValue(forecastData)
        }
    }

    fun getForecast(): LiveData<List<ConsolidatedWeather>> {
        return fullForecast
    }

    fun setCurrentCity(name: String): Boolean{
        val city = citiesList.filter { it.name.contains(name) }
        return if(city.isNotEmpty()) {
            currentCity = city[0]
            loadForecastData()
            true
        } else {
            false
        }
    }
}

data class ConsolidatedWeather(
    val id: Long?,
    val applicableDate: String?,
    val weatherStateName: String?,
    val weatherStateAbbr: String?,
    val windSpeed: Double?,
    val windDirection: Double?,
    val windDirectionCompass: String?,
    val minTemp: Int?,
    val maxTemp: Int?,
    val theTemp: Int?,
    val airPressure: Double?,
    val humidity: Double?,
    val visibility: Double?,
    val predictability: Int?
){
    lateinit var icon: Image

    override fun toString(): String {
        return weatherStateAbbr ?: "null"
    }
}

data class City(val name: String, val id: Int){
    override fun toString(): String {
        return name
    }
}