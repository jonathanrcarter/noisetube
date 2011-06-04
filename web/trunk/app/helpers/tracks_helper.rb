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
 
module TracksHelper

  Leq_label="%3C#{SoundLevel::MIN_DB}|"+Array.new(SoundLevel::NB_STEP){|i| "#{(i+1)*SoundLevel::STEP_DB+SoundLevel::MIN_DB if (i%2)==1 }"}.join('|')+"|%3E#{SoundLevel::MAX_DB}"
  Leq_size="260x140"

  def create_loudness_chart(leq_dist)
    graph= Gchart.bar(:data => leq_dist, :title=>"Leq(1s) Distribution in dB(A)", :size =>Leq_size,:axis_with_labels => ['x','y'] ,:axis_labels=>[Leq_label,"0|#{leq_dist.max}"], :bar_width_and_spacing=>"a", :custom=>"chxs=1,000000,12|0,000000,12&chts=000000,14&chf=bg,s,FFFFFFFF&chco=#{Measure::COLORS.join('|')}")
    graph.to_s
  end

  def create_tag_chart(tag_dist)
    tag_dist2={}
    tag_dist.each{ |tag,freq| 
      tag_dist2["#{tag} (#{freq})"]=freq
    }
    graph= Gchart.pie(:data =>tag_dist2.values, :title=>"Social Tagging", :size => '300x140', :labels =>tag_dist2.keys, :custom=>"chco=00AF33,4BB74C,EE2C2C,CC3232,33FF33,66FF66,9AFF9A,C1FFC1,CCFFCC&chts=000000,15&chf=bg,s,FFFFFFFF")
    
    return graph.to_s
  end


  def create_measure_circle(xml, loudness, xcoord, ycoord)
    xml.Style{
      xml.PolyStyle{
        xml.color(get_color_loudness(loudness.to_i));
        xml.colorMode("normal");
        xml.fill("true")
        xml.outline("false");
      }
    }
    xml.MultiGeometry{
      xml.Polygon{
        xml.extrude("true")
        xml.outerBoundaryIs{
          xml.LinearRing{
            xml.coordinates(generate_kmlcircle(3100+loudness.to_f(),xcoord.to_f(),ycoord.to_f(),15,24));
          }
        }
      }
    }
  end
  
  def get_next(index,list)
    if index<(list.size-1)
      return list[index+1]
    else
      return nil
    end
  end

  def generate_subfolders(frames,taged_frames,segments,time_offset)

    xml1 = Builder::XmlMarkup.new(:indent=>2)
    xml1.name("original measurements no label")
    xml1.visibility(1)
    
    xml2 = Builder::XmlMarkup.new(:indent=>2)
    xml2.name("original measurements with label")
    xml2.visibility(1)
    
    xml3 = Builder::XmlMarkup.new(:indent=>2)
    xml3.name("corrected measurements")
    xml3.visibility(1)
          
    xml4 = Builder::XmlMarkup.new(:indent=>2)
    xml4.name("segments by date")
    xml4.visibility(1)

    frames.each_with_index do |frame,i|
      next_frame=get_next(i,frames)
      generate_kml_of_original_measurements(i,xml1,frame,next_frame,time_offset)
      generate_kml_of_corrected_measurements(i,xml3,frame,segments,time_offset)
    end
    
    taged_frames.each_with_index do |frame,i|
      next_frame=get_next(i,taged_frames)
      generate_kml_of_labeled_measurements(i,xml2,frame,next_frame,segments,time_offset)
    end
    
    segments.values.each_with_index do |segment,i|
      generate_kml_of_segments(i,xml4,segment,time_offset)
    end
    
    #p xml1
    #p xml1.target!
    return xml1, xml2, xml3, xml4
  end

  def generate_kml_of_original_measurements(i,xml,frame,next_frame,time_offset)
    mpoint=frame.measure
    xml.Placemark{
      xml.name(i," - ",mpoint.user.login)
      xml.visibility(1)
      #xml.description("Track: ",mpoint.track.id,"\n",
      #  "&lt;h4&gt;Creator:&lt;/h4&gt;&lt;br/&gt; &lt;img src=\"http://www.noisetube.net//users/" , mpoint.user.id, ".jpg\" width=\"50\" height=\"50\" /&gt;\n",
      #  "#&lt;br/&gt;&lt;a href=\"http://www.noisetube.net//users/" , mpoint.user.id , "\"&gt;" , mpoint.user.login , "&lt;/a&gt;\n",
      #  "#&lt;p&gt;&lt;h4&gt;Date:&lt;/h4&gt;" , mpoint.made_at.strftime("%Y-%m-%d %H:%M:%S") , "&lt;/p&gt;\n",
      #  "#&lt;p&gt;&lt;h4&gt;Annotation related measures - Leq(1sec) in dB(A)&lt;/h4&gt;\n",
      #  "#&lt;img src=\"http://www.noisetube.net//taggings/" , mpoint.track.id , "/measures.png\" width=\"250\" height=\"140\" /&gt;\n",
      #  "#&lt;/p&gt;");
      xml.TimeSpan{
        xml.begin(mpoint.made_at.strftime("%Y-%m-%dT%H:%M:%S00"))
        xml.end((mpoint.made_at+3*time_offset).strftime("%Y-%m-%dT%H:%M:%S00"))
      }
      #xml.extrude("true")
      #xml.altitudeMode("relativeToGround")
      create_measure_circle(xml,mpoint.loudness,mpoint.geom.x,mpoint.geom.y);
    }

    xml.ScreenOverlay{
      xml.name(i," - ","distribution");
      xml.Icon{
        xml.href(create_loudness_chart(frame.loudness_stat));
      }
      xml.overlayXY(:x=>"0", :xunits=>:"fraction", :yunits=>:"fraction", :y=>"0.5")#x="0" xunits="fraction" yunits="fraction" y="0.5"
      xml.screenXY(:x=>"0", :xunits=>:"fraction", :yunits=>:"fraction", :y=>"0.5")#x="0" xunits="fraction" yunits="fraction" y="0.5"
      xml.TimeSpan{
        xml.begin(mpoint.made_at.strftime("%Y-%m-%dT%H:%M:%S00"))
        xml.end(next_frame.measure.made_at.strftime("%Y-%m-%dT%H:%M:%S00")) unless next_frame.nil?
      }
    }
  end

  def generate_kml_of_labeled_measurements(i,xml,frame,next_frame,segments,time_offset)
    mpoint=frame.measure
    for tag in mpoint.tags
      xml.Placemark{
        xml.name(tag.name,"(",mpoint.user.login,")")
        xml.visibility(1)
        #xml.description("Track: ",mpoint.track.id,"\n",
        #  "&lt;h4&gt;Creator:&lt;/h4&gt;&lt;br/&gt; &lt;img src=\"http://www.noisetube.net//users/" , mpoint.user.id, ".jpg\" width=\"50\" height=\"50\" /&gt;\n",
        #  "#&lt;br/&gt;&lt;a href=\"http://www.noisetube.net//users/" , mpoint.user.id , "\"&gt;" , mpoint.user.login , "&lt;/a&gt;\n",
        #  "#&lt;p&gt;&lt;h4&gt;Date:&lt;/h4&gt;" , mpoint.made_at.strftime("%Y-%m-%d %H:%M:%S") , "&lt;/p&gt;\n",
        #  "#&lt;p&gt;&lt;h4&gt;Annotation related measures - Leq(1sec) in dB(A)&lt;/h4&gt;\n",
        #  "#&lt;img src=\"http://www.noisetube.net//taggings/" , mpoint.track.id , "/measures.png\" width=\"250\" height=\"140\" /&gt;\n",
        #  "#&lt;/p&gt;");
        xml.TimeSpan{
          xml.begin(mpoint.made_at.strftime("%Y-%m-%dT%H:%M:%S00"))
          xml.end((segments[mpoint.segment_id].created_at+6*time_offset).strftime("%Y-%m-%dT%H:%M:%S00"))
        }
        #xml.extrude("true")
        #xml.altitudeMode("relativeToGround")
        xml.Point{
          xml.coordinates(mpoint.geom.x,",",mpoint.geom.y);
        }
      }
    end
    if mpoint.tags.size>0
      xml.ScreenOverlay{
        xml.name("sources of noise")
        xml.Icon{
          xml.href(create_tag_chart(frame.tags_stat));
        }
        xml.overlayXY(:x=>"0", :xunits=>:"fraction", :yunits=>:"fraction", :y=>"0.8")#x="0" xunits="fraction" yunits="fraction" y="0.8"
        xml.screenXY(:x=>"0", :xunits=>:"fraction", :yunits=>:"fraction", :y=>"0.8")#x="0" xunits="fraction" yunits="fraction" y="0.8"
        xml.TimeSpan{
          xml.begin(mpoint.made_at.strftime("%Y-%m-%dT%H:%M:%S00"))
          unless next_frame.nil?
            xml.end((next_frame.measure.made_at+time_offset).strftime("%Y-%m-%dT%H:%M:%S00"))
          end
        }
      }
    end
  end

  def generate_kml_of_corrected_measurements(i,xml,frame,segments,time_offset)
    mpoint=frame.measure
    xml.Placemark{
      xml.name(i,". corrected point - ",mpoint.loudness ,"db")
      xml.visibility(1)
      #xml.description("&lt;h4&gt;Creator:&lt;/h4&gt;&lt;br/&gt; &lt;img src=\"http://www.noisetube.net//users/" , mpoint.user.id, ".jpg\" width=\"50\" height=\"50\" /&gt;\n",
      #  "#&lt;br/&gt;&lt;a href=\"http://www.noisetube.net//users/" , mpoint.user.id , "\"&gt;" , mpoint.user.login , "&lt;/a&gt;\n",
      #  "#&lt;p&gt;&lt;h4&gt;Date:&lt;/h4&gt;" , mpoint.made_at.strftime("%Y-%m-%d %H:%M:%S") , "&lt;/p&gt;\n",
      #  "#&lt;p&gt;&lt;h4&gt;Annotation related measures - Leq(1sec) in dB(A)&lt;/h4&gt;\n",
      #  "#&lt;img src=\"http://www.noisetube.net//taggings/" , mpoint.track.id , "/measures.png\" width=\"250\" height=\"140\" /&gt;\n",
      #  "#&lt;/p&gt;");
      xml.TimeSpan{
        xml.begin((mpoint.made_at+3*time_offset).strftime("%Y-%m-%dT%H:%M:%S00"))
        xml.end((segments[mpoint.segment.id].created_at+6*time_offset).strftime("%Y-%m-%dT%H:%M:%S00"))
      }
      #xml.altitudeMode("relativeToGround")
      create_measure_circle(xml,mpoint.loudness,mpoint.corrected.x,mpoint.corrected.y);
    }
  end

  def generate_kml_of_segments(i,xml,segment,time_offset)

    xml.Placemark{
      if segment.segment.urban.nil?
        xml.name(i,". Seg with ",segment.loudness ," dB(A)")
      else
        xml.name(i,". Seg of {",segment.segment.urban.name,"} - ", segment.loudness ," dB(A)")
      end
      xml.description("Segment of the road {r[:name]}<br/> Gathering #{segment.loudness_count} measures<br/>",
        "Aggregated Loudness: #{segment.loudness} dB(A) <br/> Distribution: <img src=\"{r[:graph_url]}\"> <br/>",
        " density: ",segment.density)
      color=get_color_loudness(segment.loudness,"FF")
      xml.Style{
        xml.LineStyle{
          xml.color(color)
          xml.colorMode("normal")
          xml.width("8")
        }
        xml.PolyStyle{
          xml.color(color)
          xml.colorMode("normal")
          xml.outline(1)
          xml.fill(true)
        }
      }
      xml.MultiGeometry{
        xml.LineString{
          xml.extrude(true)
          xml.tessellate(true)
          xml.altitudeMode("relativeToGround")
          coord=""
          for point in segment.segment.geom
            coord=coord,"", point.x.to_s() , ",", point.y.to_s() ,",", segment.density.to_f*100,"\n"
          end
          xml.coordinates(coord)
        }
      }
      xml.TimeSpan{
        xml.begin((segment.created_at+6*time_offset+1).strftime("%Y-%m-%dT%H:%M:%S00"))
      }
    }
  end
end
