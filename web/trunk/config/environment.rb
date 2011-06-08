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
 
# Be sure to restart your server when you modify this file

# Specifies gem version of Rails to use when vendor/rails is not present
RAILS_GEM_VERSION = '2.3.5' unless defined? RAILS_GEM_VERSION

# Bootstrap the Rails environment, frameworks, and default configuration
require File.join(File.dirname(__FILE__), 'boot')


Rails::Initializer.run do |config|
  # Settings in config/environments/* take precedence over those specified here.
  # Application configuration should go into files in config/initializers -- all
  # .rb files in that directory are automatically loaded.

  # Add additional load paths for your own custom dirs config.load_paths += %W(
  # #{RAILS_ROOT}/extras )

  # Specify gems that this application depends on and have them installed with
  # rake gems:install config.gem "bj" config.gem "hpricot", :version => '0.6',
  # :source => "http://code.whytheluckystiff.net" config.gem "sqlite3-ruby",
  # :lib => "sqlite3" config.gem "aws-s3", :lib => "aws/s3"

  # json 
  config.gem "json"
  
  # image of the track on a map 
  config.gem "rmagick", :lib=>"RMagick"
  
  # chart
  config.gem "googlecharts", :lib => "gchart" 
 # config.gem 'twitter-auth', :lib => 'twitter_auth'
  # xml
  config.gem "builder"
  
  #config.gem "newrelic_rpm"

  # view pagination
  config.gem 'mislav-will_paginate', :lib => 'will_paginate', :source => 'http://gems.github.com'

  # postgres
  config.gem "pg" , :lib=>false, :source => 'http://gems.github.com'

  # postgis extension
  config.gem "postgis_adapter", :lib => "postgis_adapter"

  # zip
  config.gem "rubyzip", :lib=>"zip/zipfilesystem", :source => 'http://gems.github.com'
  
  # ip based location 
  # config.gem "geoip_city"
  config.gem "geoip"

  config.gem "GeoRuby" , :lib=>"geo_ruby"

  # geocoder
  config.gem "geokit"
  
  # duration representation 
  config.gem "duration"

  #weather sensor
  config.gem "jdpace-weatherman", :lib=>"weather_man", :source=>"http://gems.github.com"
  config.gem "xml-simple"    , :lib=>"xmlsimple"

  # color tool
  config.gem "color-tools", :lib => "color"
  
  # for background process
  config.gem 'daemons'  
  config.gem 'chronic' #, :lib => false
  config.gem 'javan-whenever', :lib => false, :source => 'http://gems.github.com'
  
  # Only load the plugins named here, in the order given (default is
  # alphabetical). :all can be used as a placeholder for all plugins not
  # explicitly named config.plugins = [ :exception_notification,
  # :ssl_requirement, :all ]

=begin
  config.active_record.include_root_in_json = true
  config.active_record.store_full_sti_class = true
  config.active_support.use_standard_json_time_format = true
  config.active_support.escape_html_entities_in_json = false
=end



  # Skip frameworks you're not going to use. To use Rails without a database,
  # you must remove the Active Record framework. config.frameworks -= [
  # :active_record, :active_resource, :action_mailer ]

  # Activate observers that should always be running
  # config.active_record.observers = :cacher, :garbage_collector,
  # :forum_observer

  #config.active_record.observers = :user_observer
  config.active_record.observers = :track_observer
  
  # Set Time.zone default to the specified zone and make Active Record
  # auto-convert onfig.time_zone to this zone. Run "rake -D time" for a list of tasks for
  # finding time zone names.
  config.time_zone = 'UTC'

  #config.action_controller.page_cache_directory = "#{RAILS_ROOT}/public/cache/"

  # The default locale is :en and all translations from config/locales/*.rb,yml
  # are auto loaded. config.i18n.load_path += Dir[Rails.root.join('my',
  # 'locales', '*.{rb,yml}')] config.i18n.default_locale = :de
  #config.i18n.default_locale = :en
end

#comment this for rake gems:install
GeoRuby::SimpleFeatures::DEFAULT_SRID = 4326


#ENV['LD_LIBRARY_PATH']="/usr/local/lib"

# Recaptcha config
ENV['RECAPTCHA_PUBLIC_KEY'] = 'CHANGE_THIS'
ENV['RECAPTCHA_PRIVATE_KEY'] = 'CHANGE_THIS'

# mail config
ActionMailer::Base.smtp_settings = {
    :enable_starttls_auto => true,
    :address => 'smtp.gmail.com',
    :port => 587,
    :domain => 'noisetube.net',
    :authentication => :plain,
    :user_name => 'noisetubemail@gmail.com',
    :password => 'noisetubesony'
  }
ActionMailer::Base.default_content_type = "text/html"

if RAILS_ENV=="production"
    require 'geoip_city'
    G = GeoIPCity::Database.new('/usr/local/share/GeoIP/GeoLiteCity.dat')
  else
    require 'geoip'
    G = GeoIP.new("#{RAILS_ROOT}/public/GeoLiteCity.dat")
 end
 
 