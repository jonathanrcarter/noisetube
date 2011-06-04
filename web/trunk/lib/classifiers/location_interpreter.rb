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

class LocationInterpreter<Interpreter

  def initialize
    super
    @i=11
    @city=nil
    @address=nil
    @last_geom=nil
    @first=true
    @tags_type=[
          "location_type",
          "location_city",
          "location_district",
          "location_street"]
    
  end

  def _process(track)
    @first=true
    self.class.superclass.instance_method("_process").bind(self).call(track)
  end
  
  def get_location(measure)
      
    # refresh tags #p "#{measure.geom.lat},#{measure.geom.lng}"

    res=Geokit::Geocoders::GoogleGeocoder.reverse_geocode "#{measure.geom.lat},#{measure.geom.lng}"

    unless (res.city.nil?)

      @last_geom=measure.geom
      @city=res.city.downcase
      @zip=res.zip
      if (res.street_address.nil?)
        @address=nil
        puts "address not found in #{$last_location}"
      else
        @address=res.street_address.gsub(/(\d+(\s|$))/,"").downcase
      end
    else
      # puts " reverse geocoding failed  for #{measure.geom}"
    end
  end

  def interpret(measure)
    tags=[]

    if (measure.geom.nil?)
      tags<<"type:indoor"
    else
      tags<<"type:street"
    end

    if (@i>20||@first) && !measure.geom.nil? && measure.geom!=@last_geom
      get_location(measure)
      @i=0
      @first=false
    end

    tags<<"street:#{@address}" unless (@address.nil?)
    tags<<"city:#{@city}"  unless (@city.nil?)
    tags<<"district:#{@zip}" unless (@zip.nil?)
    
    tag(measure, tags,"location")

    @i+=1
  end
  
end