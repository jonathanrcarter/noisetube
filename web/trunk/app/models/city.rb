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
 
class City < ActiveRecord::Base
  has_many :city_names
  has_many :tracks
  has_many :urbanelements
  has_many :users, :through => :tracks, :uniq=>true
  has_many :measures,:through => :tracks 
  has_many :sms
  validates_uniqueness_of :name , :scope=> :country
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
  attr_accessible :name,:country

  named_scope :with_geodata , :select=>"distinct cities.*",
              :joins=>:tracks,
              :conditions=>"tracks.geolocated=true",
              :order=>"country, name"
 
  # Check the existance of a city's name using a geocoder
  def self.exist?(name)
    res=Geokit::Geocoders::MultiGeocoder.geocode("#{name}")
    return res.success
  end

  def contributors(limit)
      self.tracks.find(:all,
                       :select=>"user_id, max(created_at) as last",
                       :include=>:user,
                       :group=>"tracks.user_id",
                       :order=>"max(created_at)",
                       :limit=>limit)
  end
  
  # find city or build it if doesn't exist
  def self.find_or_build(name, country)
    # city=City.find(:first,:conditions=>{:name=>name, :country=>country})
     city_name = CityNames.find(:first, :joins=>"JOIN cities ON city_names.city_id = cities.id", :conditions=>"city_names.name=#{name} and cities.country=#{county}" )
     city = city_name.city unless city_name.nil?
     city=City.build_city("#{name}, #{country}") if city.nil?
     return city
  end

  # Create a city using external geocoder data (to find its lat,lng)
  def self.build_city(name)
    res=Geokit::Geocoders::MultiGeocoder.geocode("#{name}")
    if res.success
      
      city_name=(res.city.nil?) ? name : res.city
      city=City.find(:first, :conditions=>["name= ? and country = ?", city_name,res.country_code])
      # create new city
      if city.nil?
        city=City.new(:name=>city_name,:country=>res.country_code)
        lat,lng= res.ll.split(",")
        city.lat,city.lng=lat.to_f, lng.to_f
        city.save
      end
      return city
    else 
      raise "city #{name} not found by Geocoder"
    end 
  end  

  def users_recorded
    User.find(:joins=>:tracks, :conditions=>"tracks.city_id=#{self.id}")
  end  
  
  def users_recorded_count
    User.count(:joins=>:tracks, :conditions=>"tracks.city_id=#{self.id}")
  end
  
  def tags_count(context="tags")
    unless (context.nil?)
      context_sql= "and context='#{context}'"
    end
    Tagging.count(:joins=>"LEFT OUTER JOIN measures ON measures.id = taggings.taggable_id LEFT OUTER JOIN tracks ON tracks.id = measures.track_id", :conditions=> "city_id=#{self.id} #{context_sql}")
  end
  
  def tags(limit=100)
   #Tagging.find(:all, )
   Measure.tag_counts(:joins=>"LEFT OUTER JOIN tracks ON tracks.id = measures.track_id ", :conditions=> "tracks.city_id=#{self.id}", :order=>"count desc", :limit=>limit)
  end
  
end
