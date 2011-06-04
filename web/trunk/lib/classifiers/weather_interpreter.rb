# --------------------------------------------------------------------------------
#  NoiseTube Web application
#  
#  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
#  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2011
# --------------------------------------------------------------------------------
#  This library is free software; you can redistribute it and/or modify it under
#  the terms of the GNU Lesser General Public License, version 2.1, as published
#  by the Free Software Foundation.
#  
#  This library is distributed in the hope that it will be useful, but WITHOUT
#  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
#  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
#  details.
#  
#  You should have received a copy of the GNU Lesser General Public License along
#  with this library; if not, write to:
#    Free Software Foundation, Inc.,
#    51 Franklin Street, Fifth Floor,
#    Boston, MA  02110-1301, USA.
#  
#  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
#  NoiseTube project source code repository: http://code.google.com/p/noisetube
# --------------------------------------------------------------------------------
#  More information:
#   - NoiseTube project website: http://www.noisetube.net
#   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
#   - VUB BrusSense team: http://www.brussense.be
# --------------------------------------------------------------------------------
 
require 'classifiers/interpreter.rb'
require 'weather_man'

WeatherMan.partner_id = 'CHANGE_THIS'
WeatherMan.license_key = 'CHANGE_THIS'

class WeatherInterpreter<Interpreter

  @temp=@feel_like=@wind_speed=nil
  
  # #values="Clear", "Cloudy", "Fog", "Haze", "Light rain", "Mostly Cloudy",
  # "Overcast", "Partly Cloudy", "Rain", "Rain Showers", "Showers",
  # "Thunderstormv", "Chance of Showers", "Chance of Snow", "Chance of Storm",
  # "Mostly Sunny", "Partly Sunny", "Scattered Showers", "Sunny" #keys="images/weather/chance_of_rain.gif","images/weather/sunny.gif","images/weather/mostly_sunny.gif","images/weather/partly_cloudy.gif","images/weather/mostly_cloudy.gif","images/weather/chance_of_storm.gif","images/weather/rain.gif","images/weather/chance_of_rain.gif","images/weather/chance_of_snow.gif","images/weather/cloudy.gif","images/weather/mist.gif","images/weather/storm.gif","images/weather/thunderstorm.gif","images/weather/chance_of_tstorm.gif","images/weather/sleet.gif","images/weather/snow.gif","images/weather/icy.gif","images/weather/dust.gif","images/weather/fog.gif","images/weather/smoke.gif","images/weather/haze.gif","images/weather/flurries.gif"
  @@fakes=["Tokyo",
    "New york",
    "Sao Paulo, Brazil",
    "Mexico City, Mexico",
    "Mumbai, India",
    "Jakarta, Indonesia",
    "Los Angeles, United States",
    "Moscow, Russia",
    "Shanghai, China",
    "Lagos, Nigeria ",
    "Paris, France",
    "Buenos Aires, Argentina",
    "Beijing, China",
    "San Franscisco",
    "Istanbul",
    "Yokohama",
    "Cape Town",
    "Madrid",
    "Singapore"
  ]


  def initialize
    super
    @tags_type=[
          "weather_general",
          "weather_temperature",
          "weather_wind"]
  end
  
  def interpret(measure)
    measure.temperature=@temp
    tags=[]
    # feeling
    tags<<"general:#{@feels_like}"

    # temperature
    if @temp<0
      tags<<"temperature:freezing"
    elsif @temp<10
      tags<<"temperature:cold"
    elsif @temp<20
      tags<<"temperature:moderate"
    elsif @temp<30
      tags<<"temperature:hot"
    else
      tags<<"temperature:very hot"
    end

    if (@wind_speed=="calm")
      tags<<"wind:calm"
    else
      @wind_speed=@wind_speed.to_i
      if @wind_speed<1
        tags<<"wind:calm"
      elsif   @wind_speed<5
        tags<<"wind:light air"
      elsif   @wind_speed<20
        tags<<"wind:breeze"
      elsif @wind_speed<50
        tags<<"wind:gale"
      else
        tags<<"wind:storm"
      end
    end
    tag(measure, tags,"weather")
  end
  

  def _process(track)

    # FOR FAKE MODE locations = WeatherMan.search(track.city.name)
    # #city=@@fakes[rand(@@fakes.size)]

    if track.city.nil?
      #logger.warn("no city for the track #{track.id}")
      return
    end
    
    city=track.city.name
    locations = WeatherMan.search(city)
    
    if locations.empty?
      # #logger.warn("no weather information for city  #{city}")
      return
    end
    
    # Test if it the same day
    # date=Date.parse(track.starts_at.localtime.strftime('%Y/%m/%d')) if
    # (Date.today-date>0) return # #end

    
    # #day=rand(3)

    day=0
    
    weather = locations[0].fetch(:days => day, :unit => 'm')

    if (weather.nil? || weather.current_conditions.nil?)
      # #logger.warn("no current condition for city #{city}")
      return
    end

    @temp = weather.current_conditions.temperature.to_i
    @feels_like = weather.current_conditions.description
    @wind_speed = weather.current_conditions.wind.speed  
  
    #WeatherInterpreter.superclass.process(track)
    
    self.class.superclass.instance_method("_process").bind(self).call(track)

    # puts "day #{day}, city #{city} #{temp} #{feels_like},#{wind_speed}, tags:
    # #{tags}"
  end
  
end
