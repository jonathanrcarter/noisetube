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
 
# Methods added to this helper will be available to all templates in the
# application.
module ApplicationHelper

  
  def soundlevel_exposure_profile(measures)

    dist=Array.new(SoundLevel::NB_STEP){Array.new(4, 0)}

    # compute dist
    measures.each { |m|
      time_index=Measure.time_index(m)
      if dist[m.loudness_index][time_index].nil?
        dist[m.loudness_index][time_index]=0
      end
      dist[m.loudness_index][time_index]+=1
    }

    label=["night", "morning", "afternoon", "evening"]

    # for each data series
    sum=Array.new(4, 0)
    dist.each_with_index{|h, i|
      h.each_with_index{|e, j|
        sum[j]+=dist[i][j]
      }
    }
    
    max=sum.max.to_f
    dist.each_with_index{|h, i|
      h.each_with_index{|e, j|
        dist[i][j]=dist[i][j]*100.0/max if dist[i][j]>0
      }
    }

    return Gchart.bar(:data => dist,
                      :size => '250x80',
                      :axis_with_labels => ['x', 'y'],
                      :axis_labels=>[label, "0|#{max}"],
                      :bar_colors => SoundLevel::COLORS,
                      :bar_width_and_spacing=>"a",
                      :encoding => 'text')
  end
  
  

  
  def get_location(measure)
    if measure.corrected.nil?
      measure.geom
    else
      measure.corrected
    end
  end

  def generate_kml_measurement(xml, point)
    geom=point.geom
    xml.Placemark{
      xml.ExtendedData{
        xml.Data(:name=>"decibel"){
          xml.value(point.loudness)
        }
        xml.Data(:name=>"decibel"){
          xml.value(point.loudness)
        }
        xml.Data(:name=>"geo-corrected"){
          xml.value(point.corrected.nil?)
        }
      }
      xml.styleUrl("#db_index_#{point.loudness_index}")
      xml.Point{
        xml.coordinates("#{geom.lng},#{geom.lat},0")
      }
      xml.TimeStamp{
        xml.when(point.made_at.to_s)
      }
    }
  end

  def style_header(xml, ballon_text)
    colors_table=SoundLevel::COLORS_HTML #if COLORS_HTML is not CONSTANT BUT a FUnction
    SoundLevel.loudness_index_range{ |min, max, idx|
      color=colors_table[idx]
      color="ff"+color[5..6]+color[3..4]+color[1..2]
      #color="ff"+color[1..7]
      xml.Style(:id=>"db_index_#{idx}") {
        xml.IconStyle{
          xml.color(color)
          xml.scale("1")
          xml.Icon{
            xml.href("http://www.google.com/mapfiles/kml/shapes/dot.png")
          }
        }
        xml.BalloonStyle{
          xml.text { |x| x<<ballon_text}
        }
      }
    }
  end

  def get_icon_style(measure)
  end

  def to_georss(xml, tracks)
    tracks.each { |t|
      unless t.ends_at.nil?
        xml.item do
          xml.title "Track started at #{t.starts_at} by #{t.user.login}"
          desc= "<br/><b>Measurement </b> #{duration(t)},  #{(t.city.name unless t.city.nil?)} (gps: #{t.geolocated})<br/> <img src=\"#{t.graph_image_url}\" />"
          desc+="<br/>Tags: </b>#{t.tags.join(",")}"
          desc+= "<br/>Created by <a href=\"/users/#{t.user.id}\"><img src=\"/users/#{t.user.id}.jpg\" width=50 title=\"#{t.user.login}\" /></a>"
          xml.description desc
          xml.author("#{t.user.login})")
          xml.pubDate(t.ends_at.strftime("%a, %d %b %Y %H:%M:%S %z"))
          if (t.geolocated and not t.geom.nil?) #nil check just to sure
            xml << t.geom.georss_simple_representation({:geom_attr=>""})
          end
        end
      end
    }
  end
end
