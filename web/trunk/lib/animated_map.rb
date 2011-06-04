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
 

module AnimatedMap

  # time before displaying the corrected values
  Time_offset=60*10

  # copy a hash ...
  def self.deepcopy_hash(obj)
    newobject={}
    obj.each do |key, value|
      newobject[key]=value;
    end
    return newobject;
  end


  class Frame
    attr_accessor :measure, :loudness_stat, :tags_stat

    def initialize(measure, last_frame=nil)
      @measure=measure
      

      if last_frame.nil?
        @loudness_stat=Array.new(Measure::NB_STEP,0)
        @tags_stat={}
      else
        @loudness_stat=last_frame.loudness_stat.clone
        @tags_stat=AnimatedMap::deepcopy_hash(last_frame.tags_stat)
      end

      for t in measure.tags
        add_tag(t)
      end
      # update state
      @loudness_stat[Measure.loudness_index(measure.loudness)]+=1
    end

    def add_tag(t)
      if @tags_stat[t.name].nil?
        @tags_stat[t.name]=1
      else
        @tags_stat[t.name]+=1
      end
    end

  end


  class SegmentDetails
    attr_accessor :segment, :loudness, :density, :created_at, :loudness_count

    def initialize(measure)
      @segment = measure.segment
      @loudness = 0
      @created_at = measure.made_at
      @loudness_count=0.0
      #add_measure(measure)
    end

    def add_measure(m)
      @created_at = m.made_at if @created_at < m.made_at
      @loudness+=10.0**(0.1*m.loudness);
      @loudness_count+=1;
    end
    
    def finalize
      @loudness=10.0*Math.log10(@loudness/@loudness_count)
      surface=@segment.geom.points[0].ellipsoidal_distance(@segment.geom.points[1])
      @density=@loudness_count/surface
    end

  end

  def self.generate_track(trackId)

    segments={};
    frames=[]
    taged_frames=[]
    last_frame=nil

    measures=Measure.find(:all, :conditions =>"geom IS NOT NULL AND  segment_id IS NOT NULL AND  corrected IS NOT NULL AND   measures.track_id=#{trackId}", :order=>"made_at");
   

    for m in measures

      # compute frame
      frame= Frame.new(m, last_frame)
      frames<<frame
      last_frame=frame
      
      if(frame.measure.tags.size>0)
        taged_frames<<frame
      end

      # compute segment
      segments[m.segment_id]= SegmentDetails.new(m) unless segments.include?(m.segment_id)
      segments[m.segment_id].add_measure(m)
      
    end

    segments.each_value{|seg| seg.finalize}

    puts "measures size: #{measures.size}"
    puts "segments size: #{segments.size}"
    puts "frames size: #{frames.size}"

    return frames,taged_frames,segments
  end
end