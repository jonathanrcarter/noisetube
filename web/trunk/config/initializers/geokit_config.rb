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
 
require 'geokit'

if defined? Geokit

	# These defaults are used in Geokit::Mappable.distance_to and in acts_as_mappable
	Geokit::default_units = :km
	Geokit::default_formula = :sphere

	# This is the timeout value in seconds to be used for calls to the geocoder web
	# services.  For no timeout at all, comment out the setting.  The timeout unit
	# is in seconds. 
	
Geokit::Geocoders::request_timeout = 3
	# These settings are used if web service calls must be routed through a proxy.
	# These setting can be nil if not needed, otherwise, addr and port must be 
	# filled in at a minimum.  If the proxy requires authentication, the username
	# and password can be provided as well.
	Geokit::Geocoders::proxy_addr = nil
	Geokit::Geocoders::proxy_port = nil
	Geokit::Geocoders::proxy_user = nil
	Geokit::Geocoders::proxy_pass = nil

	# This is your yahoo application key for the Yahoo Geocoder.
	# See http://developer.yahoo.com/faq/index.html#appid
	# and http://developer.yahoo.com/maps/rest/V1/geocode.html
	Geokit::Geocoders::yahoo = 'REPLACE_WITH_YOUR_YAHOO_KEY'
    
	# This is your Google Maps geocoder key. 
	# See http://www.google.com/apis/maps/signup.html
	# and http://www.google.com/apis/maps/documentation/#Geocoding_Examples
	Geokit::Geocoders::google = 'REPLACE_WITH_YOUR_GOOGLE_MAPS_KEY'
    
	# This is your username and password for geocoder.us.
	# To use the free service, the value can be set to nil or false.  For 
	# usage tied to an account, the value should be set to username:password.
	# See http://geocoder.us
	# and http://geocoder.us/user/signup
	Geokit::Geocoders::geocoder_us = false 

	# This is your authorization key for geocoder.ca.
	# To use the free service, the value can be set to nil or false.  For 
	# usage tied to an account, set the value to the key obtained from
	# Geocoder.ca.
	# See http://geocoder.ca
	# and http://geocoder.ca/?register=1
	Geokit::Geocoders::geocoder_ca = false

	# Uncomment to use a username with the Geonames geocoder
	#Geokit::Geocoders::geonames="REPLACE_WITH_YOUR_GEONAMES_USERNAME"

	# This is the order in which the geocoders are called in a failover scenario
	# If you only want to use a single geocoder, put a single symbol in the array.
	# Valid symbols are :google, :yahoo, :us, and :ca.
	# Be aware that there are Terms of Use restrictions on how you can use the 
	# various geocoders.  Make sure you read up on relevant Terms of Use for each
	# geocoder you are going to use.
	Geokit::Geocoders::provider_order = [:google,:us]

	# The IP provider order. Valid symbols are :ip,:geo_plugin.
	# As before, make sure you read up on relevant Terms of Use for each
	# Geokit::Geocoders::ip_provider_order = [:geo_plugin,:ip]

end
