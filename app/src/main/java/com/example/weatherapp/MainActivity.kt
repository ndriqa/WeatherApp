package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import com.example.weatherapp.databinding.ActivityMainBinding
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener{
    private lateinit var binding: ActivityMainBinding
    private val model: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //fill up the cities list with the adapter
        model.getCities().observe(this, Observer<List<City>>{ cities ->
            // update UI
            val adapter: ArrayAdapter<String> =
                ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cities.toStringArray())

                //specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            //bind the adapter
            binding.citiesList.adapter = adapter
        })

        //set the selected item listener to this
        binding.citiesList.onItemSelectedListener = this

        //get and fill the weather data for the specified location
        model.getForecast().observe(this, Observer<List<ConsolidatedWeather>>{ weather ->
            // update UI
            val daysWeather = mutableListOf<View>()
            if (weather.isNotEmpty()){
                for (day in weather){
                    val wrapper = LinearLayout(this)
                    wrapper.orientation = LinearLayout.VERTICAL
                    wrapper.gravity = Gravity.CENTER
                    wrapper.setPadding(10, 10, 10, 10)

                    val dateTitle = TextView(this)
                    dateTitle.text = day.applicableDate?.replace('-', '/') ?: "null"

                    val weatherStateIcon = ImageView(this)
                    Picasso.get().load("https://www.metaweather.com/static/img/weather/png/64/${day.weatherStateAbbr}.png").into(weatherStateIcon, object: Callback {
                        override fun onSuccess(){
                            binding.temperatures.visibility = View.VISIBLE
                            binding.weatherLocationTitle.visibility = View.VISIBLE
                        }

                        override fun onError(e: Exception?) {

                        }
                    })

                    val theTemp = TextView(this)
                    val tempText = "${day.theTemp}°C"
                    theTemp.text = tempText
                    theTemp.textAlignment = LinearLayout.TEXT_ALIGNMENT_CENTER

                    wrapper.addView(dateTitle)
                    wrapper.addView(weatherStateIcon)
                    wrapper.addView(theTemp)

                    daysWeather.add(wrapper)
                }
                Log.d("images", daysWeather.toString())

                binding.forecastDays.removeAllViewsInLayout()
                for (v in daysWeather){
                    binding.forecastDays.addView(v)
                }

                Picasso.get().load("https://www.metaweather.com/static/img/weather/png/${weather[0].weatherStateAbbr}.png")
                    .into(binding.todaysWeatherIcon)

                val tempText = "${weather[0].theTemp}°C"
                val minTemp = "${weather[0].minTemp}°C"
                val maxTemp = "${weather[0].maxTemp}°C"

                binding.todaysWeatherTemperature.text = tempText
                binding.minimumTemperature.text = minTemp
                binding.maximumTemperature.text = maxTemp

                val cityText = "Weather in ${model.currentCity} today"
                binding.weatherLocationTitle.text = cityText
            }
        })

    }

    private fun List<City>.toStringArray(): ArrayList<String> {
        val list = ArrayList<String>(this.size)
        for (city in this){
            list.add(city.name)
        }
        return list
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        Glide.with(this).load(R.drawable.loading).into(binding.todaysWeatherIcon)
        binding.temperatures.visibility = View.GONE
        binding.weatherLocationTitle.visibility = View.GONE

        val selectedCity = parent?.getItemAtPosition(pos)
        if (selectedCity != null){
            model.setCurrentCity(selectedCity.toString())
        }
        Toast.makeText(applicationContext, "${model.currentCity}", Toast.LENGTH_SHORT).show()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(applicationContext, "Nothing selected", Toast.LENGTH_SHORT).show()
    }
}