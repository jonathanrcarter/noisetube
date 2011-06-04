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
 
require 'sound_level.rb'
namespace :noisetube do

  namespace :db do

    desc "delete track (e.g id=233)"
    task(:delete_track=>:environment) do
      id=ENV['id']
      unless id.nil?
        Track.find(id).destroy
      end
    end

    desc "delete user (e.g. id=123 or id=username)"
    task(:delete_user=>:environment) do
      id=ENV['id']
      unless id.nil?
        if id[:id] =~ /^\d*$/
          User.find(id).destroy
        else
          User.find_by_login(id).destroy
        end
      end
    end

    desc "reindexing tags"
    task(:track_tagging=> :environment) do
      from=ENV["from"]||"0"
      m=Track.find(:all, :conditions=>"tracks.id>=#{from}", :order=>"tracks.id")
#      Interpreter.interpreters=[
#              TimeInterpreter.new,
#              LocationInterpreter.new,
#              ActivityInterpreter,
#              SensorTypeInterpreter,
#              WeatherInterpreter.new,
#              LoudnessInterpreter.new
#      ]
      size=m.size
      m.each_with_index{ |track, i|
        size_measure=track.measures.count
        puts "\n--------------\nTrack #{track.id } (#{i}/#{size}) reindexing #{size_measure} measures (geo:#{track.geolocated})"
        track.indexation
      }
    end

    desc "reindexing loudness+time (index)"
    task(:track_indexing=> :environment) do
      Track.all.each{ |track|
        puts " track #{track.id } reindexing..."
        track.process_indexing
      }
    end

    desc "reindexing geo_corrected attribute"
    task(:track_geocorrection=> :environment) do
      tracks=Track.find(:all, :conditions=>"geolocated=true", :order=>"tracks.id")
      tracks.each_with_index{|track, i|
        puts "track #{track.id } (#{i}/#{tracks.size}) correcting #{track.measures.count} measures (geo:#{track.geolocated})"
        r=GeoCorrector::correct_track(track)
        puts "=> #{r} "
      }
    end

    desc "cutting the road in segments"
    task(:generating_urban_segments=>:environment) do
      GeoFeatures::urban_element_segmentation
    end



    desc "clean tracks"
    task(:clean_tracks=>:environment) do
      # Measure.add_random_tag
      Track.find(:all, :order=>"ends_at asc").each {|track|
        change=false
        if (track.measures.size<3)
          puts "Track #{track.id} fixed. too short => deleted"
          track.destroy
        else

          if (track.user.nil?)
            track.user=User.first
            puts "Track #{track.id} Fixed. track with no user #{track.user.login}"
            change=true
          end

          if (track.city.nil?)
            track.city=track.user.city
            track.city=track.measures.first.user.city unless track.city.nil?
            track.city=City.first
            puts "Track #{track.id} Fixed. track with no city #{track.city}"
            change=true
          end

          if (track.public.nil?)
            track.public=true
            puts "Track #{track.id} Fixed. making public track by default"
            change=true
          end

          if (track.ends_at.nil?)
            puts "Track #{track.id} fixed (no ends_at attribute)"
            track.ends_at = track.measures.find(:last, :order=>"made_at").made_at
            change=true
          end

          track.save if change
        end
      }
    end







    desc "clean measurements"
    task(:clean_measurements=>:environment) do

      r=Measure.destroy_all("loudness is null or loudness<=0")
      puts "Measurements fixed. measurement with bad loudness deleted #{r}"

      m=Measure.find(:all, :conditions=>"loudness_index is null or loudness_index>#{SoundLevel::NB_STEP}")
      puts "update measure with loudness_index null  #{m.size}"
      m.each{|e|
        e.loudness_index=SoundLevel.compute_loudness_index(e.loudness)
        e.save
      }

      m=Measure.find(:all, :conditions=>"geom is null and lat is not null")
      puts "update measure with geom null  #{m.size}"
      m.each{|e|
        e.geom=Point.from_x_y(e.lng, e.lat)
        e.save
      }

      ghost_tracks={}
      ghostMeasures = Measure.find(:all, :conditions=>"track_id is null")
      puts "Create track for #{m.size} measures without a track"
      ghostMeasures.each{|m|
        gTrack = ghost_tracks[m.user]
        if gTrack.nil?
          gTrack = Track.new(:user=>m.user)
          gTrack.client=nil
          gTrack.client_version=nil
          gTrack.device="ghost"
          gTrack.save
          ghost_tracks[m.user] = gTrack
          puts "Ghost track created for user #{m.user.id}"
        end
        m.track = gTrack
        gTrack.starts_at = m.made_at if gTrack.starts_at.nil? or m.made_at < gTrack.starts_at
        gTrack.ends_at = m.made_at if gTrack.ends_at.nil? or m.made_at > gTrack.ends_at
        gTrack.save
        m.save
      }
    end








    desc "clean the cities"
    task(:clean_cities=>:environment) do

      City.all.each { |city|
        track=city.tracks.find(:last, :conditions=>"processed=true", :order=>"ends_at")
        unless track.nil?
          if (city.last_activity_at.nil? || city.last_activity_at!=track.ends_at)
            puts "City #{city.id} Fixed. last activity at (#{city.last_activity_at} to #{track.ends_at}"
            city.last_activity_at=track.ends_at
            city.save
          end
        end
      }
    end







    desc "clean the tags"
    task(:clean_tags=>:environment) do

      puts "check duplicated tags"
      ActiveRecord::Base.connection().execute("DELETE from taggings WHERE taggings.id IN (select taggings.id FROM taggings left join measures on measures.id=taggings.taggable_id where measures.id is null)");
      results=ActiveRecord::Base.connection().execute("Select t.id as id, t.name as name, t2.id as id_ref  from tags as t , tags as t2 where t.id!=t2.id and t.name=t2.name order by t.name")
      puts "#{results.num_tuples} found"
      ids={}
      results.each{| res |
        ids[res["name"]]=[] if ids[res["name"]].nil?
        ids[res["name"]]<< res["id_ref"].to_i unless ids[res["name"]].include?(res["id_ref"].to_i)
      }
      p ids
      ids.each{|key, ids|
        ref_id=ids[0]
        other_ids=ids[1..-1]
        puts "change #{other_ids} to #{ref_id}"
        Tagging.update_all("tag_id= #{ref_id}", "tag_id in (#{other_ids.join(",")})")
        other_ids.each{|id|
          Tag.find_by_id(id).destroy
        }
      }

      # not valid tags
      ["variation:high",
       "exposure:high",
       "suddent peak",
       "[suggestions]",
       "null"].each { |tag|
        item=Tag.find_by_name(tag)
        unless item.nil?
          puts "Tag #{tag} Fixed. delete not valid tag"
          item.destroy
        end
      }

      # tagging
#    puts "fixing tagging (fixing user)"
      #no_tagger=Tagging.find(:all, :joins=>:taggers, :conditions=>"users.id is null")
      #orphan
      results=Tagging.find_by_sql("select taggings.*
      from users right join taggings on users.id=taggings.tagger_id
      where users.id is null")
      puts "orphan tagging found: #{results.size}" if results.size>0
      results.each { |tagging|
        tagging.tagger_id=tagging.taggable.user_id
        tagging.save
      }
    end

    desc "clean the users"
    task(:clean_users=>:environment) do

      User.all.each{|user|
        change=false


        #user.email=user.email.replace('@','@')
        # check if user has a phonemodel
        if (user.phonemodel.nil?)
          user.phonemodel="unknown"
          change=true
          puts "User #{user.id} Fixed. phonemodel null to unknown"
        end

        # check if the user has a city
        if (user.city.nil? || (!user.valid_location?))
          if user.valid_location?
            city=City.build_city(user.location)
            puts "User #{user.id} Fixed. city not found to #{city}"
          else
            city=City.first
            puts "User #{user.id} Fixed. city not found: first city #{city}"
          end          
          user.city=city
          user.location="#{city.name}, #{city.country}"
          change=true

        end

        # bug fix
        if user.email=="admin@noisetube.net"
          user.email=user.email.gsub("admin", "admin#{user.id}")
          change=true
        end

        # fixing updated_at attribute
        track=user.tracks.find(:last, :conditions=>"processed=true", :order=>"ends_at")
        unless track.nil?
          if (user.last_activity_at.nil? || track.ends_at!=user.last_activity_at)
            puts "User #{user.id} Fixed. last_activity_at attribute (#{user.last_activity_at} to #{track.ends_at})"
            user.last_activity_at=track.ends_at
            change=true
          end
        end

        if change
          p user.errors unless (user.save)
        end

      }
    end

    desc "clean all the db"
    # tasks order is important
    task(:clean_all=>[
            "noisetube:db:clean_tags",
            "noisetube:db:clean_measurements",
            "noisetube:db:clean_tracks",
            "noisetube:db:clean_users",
            "noisetube:db:clean_cities"]) do
    end
  end
end