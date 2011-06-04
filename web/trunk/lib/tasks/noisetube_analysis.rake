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
 
class Array
  def comb(n = size)
    if size < n or n < 0
    elsif n == 0
      yield([])
    else
      self[1..-1].comb(n) do |x|
	yield(x)
      end
      self[1..-1].comb(n - 1) do |x|
	yield([first] + x)
      end
    end
  end
end

namespace :noisetube do

  namespace :analysis do

    $mapping=[]
    def get_index(key)
      idx=$mapping.index(key)
      if idx.nil?
        $mapping<<key
        idx=$mapping.size-1
      end
        idx
    end

    desc "export data"
    task(:cooccurrance_network => :environment) do
      # for a subset
      max=1000
      matrix=Array.new(max,Array.new(max,0))

      m=Measure.tagged_with("paris", :on => :location_city, :joins=>"taggings")
      puts "size: #{m.size}"

      m.each_with_index { |measure,i|
         tags=[]
          measure.taggings.each { |tagging|
           tags<<tagging.context+":"+tagging.tag.name
          }
         puts "measure: #{i}" if (i%100)==0
          tags.comb(2){|pair|
            x=get_index(pair[0])
            y=get_index(pair[1])
            matrix[x][y]+=1;
          }
        }

      puts "build reduced matrix for #{$mapping.size}"

      reduced_matrix=Array.new($mapping.size,Array.new($mapping.size,0))
      for i in 0..($mapping.size-1)
        for j in 0..i($mapping.size-1)
          reduced_matrix[i][j]=matrix[i][j]
        end
      end
      p reduced_matrix
        #indexing
        #[tag1,tag2,tag3,tag4]
        #for each pair tag 1 tag2
    end

    desc "export data"
    task(:export_data => :environment) do
      database.export_tags_matrix
    end


  def export_tags_matrix(cvs_filename)
    m=Measure.all
    column=[ "tags", #human tags
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
             "weather_wind"]
    CSV.open(cvs_filename, 'w') do |writer|
      writer << [m.size, column.size]
      column.each { |c|
        tag=Measure.tag_counts_on(c).sort{|t| t.freq}
        writer << [c, "FLOAT"]
      }
      m.each { |mea|
        tags
        #reset
        mea.taggings.each { |tag|
          tags[tag.context]=true
        }
      }
      writer.close
    end
  end
  end
end

