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
 
class LocalizeSMSException < Exception

end

class Sms < ActiveRecord::Base
  acts_as_taggable_on :tags

  belongs_to :usersms
  belongs_to :city

  #validates_presence_of   :city
  validates_presence_of   :text
  validates_presence_of   :usersms

  attr_accessible :usersms, :text
  # Receiving SMS text formatted as: tag1, tag2, ..., tagn, L: 100 Spear st, San
  # Francisco, CA tag1, tag2, ..., tagn, L: lat,lng
  #
  def self.create(msg,from,to=nil)

    usersms=Usersms.find_or_create_by_phone(from)
    sms=Sms.new(:usersms=>usersms, :text=>msg)

    # parse location
    begin
      loc=parse_location(msg,"Paris France")
      lat,lng=loc.ll.split(",")
      city=City.find_or_build(loc.city, loc.country_code)
      sms.city=city
      sms.geom=Point.from_x_y(lng,lat)
    rescue LocalizeSMSException
      
    end
    
    sms.save!
    
    # parse tags
    parse_tags(msg).each{|tag|
      sms.usersms.tag(sms, :with=>tag, :on=>:tags)
    }

    return sms
  end

  def self.parse_tags(text)
    if  text=~ /(.*)(L|l)\:/
      return $1.split(",").map{|tag|
        tag.strip!
        tag unless tag.empty?
      }.compact
    end
    return nil
  end
  
  # parse the location
  def self.parse_location(text,location_suffix)
    # get only address
     if text=~ /(L|l)\:(.*)/
      address=$2.strip
     end

    if address.nil? || address.empty?
      raise LocalizeSMSException.new("no address in sms")
    end

    res=Geokit::Geocoders::GoogleGeocoder.geocode("#{address}, #{location_suffix}")
    if  (res.nil? || res.ll.nil? || res.street_address.nil?)
      # ERROR: address not localized
      raise LocalizeSMSException.new("address #{address} not localized")
    else
      p res
      return res
    end
  end
 
end
