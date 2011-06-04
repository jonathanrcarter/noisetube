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
 
class TagsController < ApplicationController

  # /tags/time:tag_1+location:tag_2
  #
  caches_action :index, :layout=>false

  def index
    filter
  end
  
  def filter
    @baseurl=root_url
    
    @tags=[]    
    @tags+=params[:tags].split(",") unless params[:tags].nil?
    
    respond_to do |format|

      # RestFul: sematic representation
      format.html{
        
        @tags_clouds={}
        [ "tags", #human tags
          "time_day",
          "time_week",
          "user_mobility",
          "user_sensor",
          "user_calibrated",
          "time_season",
          "location_type",
          "location_city",
          "location_district",
          "location_street",
          "loudness_value",
          "loudness_behavior",
          "weather_general",
          "weather_temperature",
          "weather_wind"
        ].each{ |semantic|

          tags=semantic_perspective(@tags,"#{semantic}")
          # sort by name
          @tags_clouds[semantic]=tags.sort_by{| tag|  tag.name }
        }
        render :template=>"tags/filter.html.erb"
      }

      # geographical representation
      format.kml{
        
        @measures=geographical_perspective(params)
        cond,joins=Measure.search_options(params)
        @tags_measure={}
        taggings=Tagging.find(:all, :joins=>"INNER JOIN measures ON taggings.taggable_id = measures.id INNER JOIN tags ON tags.id = taggings.tag_id", :conditions=>"measures.geom is not null and taggings.context='tags' and #{cond} ", :order=>"tags.name")

        unless taggings.empty?

          taggings.each{|tagging|
            name=tagging.tag.name
            if @tags_measure[name].nil?
              @tags_measure[name]={:taggings=>[], :count=>0}
            end
            @tags_measure[name][:taggings]<<tagging
            @tags_measure[name][:count]+=1
          }
        end
        render :template=>"tags/filter.kml.erb"
      }
               
      # #@ranges=Measure.by_range(measures) #render :template =>
      # "cities/map.kml.erb", :mimetype => :kml

  end
end

def legend
  @baseurl=root_url
  @box=params[:BBOX]
  @tags_type=params[:type_tags] || "tags"
  @tags=[]
  @tags=params[:exclude_tags].split(",") unless params[:exclude_tags].blank?
  respond_to do |format|
    format.kml{
    }
  end
    
end

  
private

def geographical_count(tags)
  unless (tags.empty?)
    cond1=[]
    tags.each { |tag|
      context,name=tag.split(":")
      cond1<<"( taggings_tags.name = E'#{name}' and taggings.context='#{context}')"
    }
  end
  return Tagging.find_by_sql("SELECT * FROM taggings LEFT OUTER JOIN measures ON measures.id=taggings.taggable_id LEFT OUTER JOIN tags taggings_tags ON taggings_tags.id = taggings.tag_id WHERE measures.geom is not null and #{cond1.join(" OR ")} GROUP BY taggings.taggable_id HAVING COUNT(taggings.taggable_id) =#{cond1.size}")
  # #return Measure.find(:all, :conditions=>"measures.geom is not null and
  # measures.id IN (SELECT taggings.taggable_id FROM taggings LEFT OUTER JOIN
  # tags taggings_tags ON taggings_tags.id = taggings.tag_id WHERE
  # #{cond1.join(" OR ")} GROUP BY taggings.taggable_id HAVING
  # COUNT(taggings.taggable_id)
  # =#{cond1.size}) ")
end

def geographical_perspective(param)
  cond,joins=Measure.search_options(param)
  return Measure.find(:all, :conditions=>"measures.geom is not null and #{cond}")
end


def semantic_perspective(tags, general_context,limit=30)
  
  unless (tags.empty?)
    cond1=[]
    cond2=[]
    names=[]
    tags.each { |tag|
      context,name=tag.split(":")
      name=name.gsub(/\\/, '\&\&').gsub(/'/, "''")
      cond1<<"( tags.name like E'#{name}' and taggings.context='#{context}')"
      # remove the selected tags of the tag cloud 
      cond2<< " (not tags.name like E'#{name}' )"
      names<<name
    }
    cond="taggable_id IN (SELECT taggings.taggable_id FROM taggings LEFT OUTER JOIN tags ON tags.id = taggings.tag_id WHERE #{cond1.join(" OR ")} GROUP BY taggings.taggable_id HAVING COUNT(taggings.taggable_id) =#{cond1.size}) "

    # error
    cond+=" and #{cond2.join(" and ")}" unless cond2.empty?
      
  end
  unless cond.nil?
    Measure.tag_counts_on(general_context, {:conditions=>cond, :order=>"count desc", :limit=>limit})
  else
    Measure.tag_counts_on(general_context, {:order=>"count desc", :limit=>limit})
  end
end

  
end