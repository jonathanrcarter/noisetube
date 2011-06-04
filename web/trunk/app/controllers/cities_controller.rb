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
 
require 'geo_ruby'

class CitiesController < ApplicationController

  caches_action :index, :layout=>false

  def index
    @cities = []
    City.with_geodata.each{|city|
      contributors=[]
      city.contributors(5).each{|track| contributors<<[track.user, track.last] }
      @cities<<{
              :city =>city,
              :tags=>city.tags(10),
              :contributors=>contributors,
              :has_map=>File.exist?("#{RAILS_ROOT}/public/kmz/city_#{city.id}.kmz")
      }
      @cities.sort!{|a,b| b[:city].measures.count<=>a[:city].measures.count}
      @best_cities=@cities[0..4]
    }
  end


  def show
    @city = City.find(params[:id])
    @onload="initialize()"
    @onunload="GUnload()"

    respond_to do |format|
      format.html {
              #send_file("#{RAILS_ROOT}/public/kmz/city_#{@city.id}.kmz", {:type=>"application/vnd.google-earth.kmz"})
      }
      format.kmz {
        send_file "#{RAILS_ROOT}/public/kmz/city_#{@city.id}.kmz"
      }
      format.rss {
        @tracks=@city.tracks.find(:all, :conditions=>"tracks.processed = true and (tracks.public is null or tracks.public = true) and tracks.geolocated=true", :order=>"tracks.created_at desc", :limit=>20 )
      }
      format.kml {
        as_kml
      }
    end
  end


  private

  def as_kml

    @baseurl=root_url
    @city_url=city_url

    if (params[:dl]=="1")
      # legend part
      kml_legend
    else

      @ranges=Measure.by_range(@city.measures)
      @users=@city.users
      @tags_measure={}

      Measure.tag_counts(:order=>"name DESC").each{ |tag|
        taggings=tag.taggings.find(:all, :include=>[:taggable, :tagger], :joins=>"LEFT OUTER JOIN measures ON taggings.taggable_id = measures.id LEFT OUTER JOIN tracks ON measures.track_id = tracks.id", :conditions=>"measures.loudness is not null and measures.geom is not null and tracks.city_id=#{@city.id} and taggings.context='tags'")
        unless taggings.empty?
          if @tags_measure[tag.name].nil?
            @tags_measure[tag.name]={:taggings=>[], :count=>0}
          end
          @tags_measure[tag.name][:taggings]+=taggings
          @tags_measure[tag.name][:count]+=taggings.size
        end
      }

      Sms.tag_counts(:order=>"name DESC").each{ |tag|
        taggings=tag.taggings.find(:all, :include=>[:taggable, :tagger], :joins=>"LEFT OUTER JOIN sms ON taggings.taggable_id = sms.id", :conditions=>"sms.geom is not null and sms.city_id=#{@city.id}")
        unless taggings.empty?
          if @tags_measure[tag.name].nil?
            @tags_measure[tag.name]={:taggings=>[], :count=>0}
          end
          @tags_measure[tag.name][:taggings]+=taggings
          @tags_measure[tag.name][:count]+=taggings.size
        end
      }

      render :template => "cities/map.kml.erb", :mimetype => :kml
    end
  end

  def kml_legend
    @box=params[:BBOX]
    @tag_type=params[:tagtype] || "tags"
    @telematin=(params[:telematin].nil?)? "" : "&telematin=1"
    render :template=>"cities/map_dynamic_legend.kml.erb", :mimetype => :kml
  end

  private
  
  

end