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
 
namespace :noisetube do
  require 'net/http'
  require 'zip/zipfilesystem'

  desc "computing unprocessed tracks"
  task(:process_tracks=>:environment) do

    puts "START TASK noisetube:process_tracks (#{Time.now})"
    
    id=ENV['id'] || nil
    force=ENV["force"] || false
    send_email=!force
    unvalidating_cache=false

    if id.nil?
      if force
        puts "Forced (re)processing of all tracks"
        tracks=Track.find(:all, :order=>"created_at asc") #ends running tracks!
      else
        #end/delete idle tracks:
       
        max_idle_time_seconds = 86400 #= 1 day (= 60*60*24)
        running_tracks = Track.find(:all, :conditions=>"ends_at IS NULL", :order=>"starts_at asc")
        puts "There are #{running_tracks.size} running tracks"
        puts "Ending idle tracks / Deleting of empty idle tracks"
        running_tracks.each { |t|
          last_measurement = t.measures.find(:last, :order=>"made_at")

          if not last_measurement.nil?
            if (Time.now - last_measurement.made_at > max_idle_time_seconds) #is last added measurement older than one day?
              puts "Track #{t.id} is idle (but not empty) --> ending"
              t.ends_at = last_measurement.made_at #yes --> end track
              t.save
            end
          else #empty track (no measurements (yet?))
            if (Time.now - t.starts_at > max_idle_time_seconds)
              #track started over one day ago, but still no measurements --> delete it
              puts "Track #{t.id} is idle and empty --> deleting"
              t.destroy
            end
          end
        }

        #process unprocessed ended tracks:
        puts "Processing ended unprocessed tracks"
        tracks = Track.find(:all, :conditions=>"processed=false and ends_at IS NOT NULL", :order=>"created_at asc")
      end

      size = tracks.size
      
      puts "Processing #{size} tracks..."
      
      unvalidating_cache=true if size>0
      tracks.each_with_index{|track, i|
        track.process(send_email)
      }
    else
      Track.find(id).process(send_email)
      unvalidating_cache=true
    end

    if (unvalidating_cache)
      # invalidate caches related to tracks
      store = ActionController::Base.cache_store
      store.delete_matched("/users/index")
      store.delete_matched("/tags/index")
      store.delete_matched("/tags/filter")
    end
    
    puts "END TASK noisetube:process_tracks (#{Time.now})"
    puts #empty line
  end

  def complicated_generation(url, path)
    uri=URI.parse(url)
    http = Net::HTTP.new(uri.host, uri.port)
    http.open_timeout = 10
    http.read_timeout = 1000
    http.start do |http|
      puts "fetching #{url}"
      response=http.request_get(uri.path)
      puts "generating kmz"
      KML::generate_kmz_file(response.body, path)
    end
  end

  desc "generate cities kmz"
  task(:generate_maps => :environment) do

    puts "START TASK noisetube:generate_maps (#{Time.now})"

    force=ENV["force"] || false

    City.all.each{|city|
      puts "---\nCity: #{city.name} #{city.country}"

      if (city.tracks.count(:conditions=>"geolocated=true")>0)

        path="#{RAILS_ROOT}/public/kmz/city_#{city.id}.kmz"

        # no yet generated?
        to_generate=!File.exists?(path)

        # city data updated?
        if to_generate
          puts "Existing kmz file... #{to_generate}"
          puts "Generating map for the first time"
        else
          last_generated=File.open(path).ctime
          puts "Existing kmz file... #{to_generate} (#{last_generated.to_s})"
          last_updated = city.last_activity_at || city.created_at
          puts "City created at: #{city.created_at}"
          puts "City updated at: #{city.updated_at}"          

          to_generate=(last_updated.nil?) || (last_updated>last_generated)

        end

        if to_generate || force
          puts "Updating map..."
          url="http://www.noisetube.net/cities/#{city.id}.kml"
          complicated_generation(url, path)
          track=city.tracks.find(:last, :conditions=>"processed=true")
          if ((!track.nil?) && (!force))
            UserMailer.deliver_update_city(track)
          end
        end

      else
        puts "No geolocated measurement yet."
      end
    }

    puts "END TASK noisetube:generate_maps (#{Time.now})"
    puts #empty line
  end


  desc "generate dynamic animation kmz"
  task(:generate_animated_maps => :environment) do

    puts "START TASK noisetube:generate_animated_maps (#{Time.now})"

    # configure 
    root_url=(RAILS_ENV=='production')? "http://noisetube.net" :"http://localhost:3000"
    track=Track.find(ENV["id"])
    path="#{RAILS_ROOT}/public/tours/tour#{track.id}.kmz"
    url="#{root_url}/users/#{track.user.id}/tracks/#{track.id}/animated_map.kml"

    # process
    complicated_generation(url, path)

    puts "END TASK noisetube:generate_animated_maps (#{Time.now})"
    puts #empty line
  end

end



