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
 
require 'color'

module SoundLevel

  def self.compute_color_idx(loudness_value)
    idx=self.compute_loudness_index(loudness_value)
    ratio=1-idx.to_f/NB_STEP.to_f
    #ratio=ratio*2 if (ratio>0.8)
    
    #ratio=ratio*2 if (ratio<0.2)

    hue=(0.8)*ratio-0.4
    hue+=1 if hue<0
    brig=0.5
    brig=0.3 if ratio<0.4
    return Color::HSL::from_fraction(hue, 1.0, brig).html
  end

  # fix date
  def self.graph(measures, options={})
    duration=100
    data=[]
    unless (measures.nil?)
      start_ = measures[0].made_at
      end_ = measures[-1].made_at
      unless (end_.nil? || start_.nil?)
        duration=end_-start_
      end
      min = MIN_DB-10.0
      max= MAX_DB+10.0
      data=measures.map{|m|
      # min(max(loudness,MIN), MAX)
        ([[m.loudness.to_f, 30.0].max, max].min-min)/(max-min)*100
      }
    end
    Gchart.line(:data => data, :size => options[:size] || '250x140', :max_value=>100, :background => 'FFFFFF00', :custom=>"chco=0000FF&chxt=x,y,r&chxtc=2,-180&chxl=2:|low|moderate|high&chxp=2,12,38,62&chxs=2,0000dd,13,-1,t,FF0000&chxr=0,0,#{duration}|1,30,110")
  end

  def self.compute_distribution_labels
    labels=[]
    self.loudness_index_range(MIN_DB, MAX_DB, STEP_DB){|min, max, i|
      if min.nil?
        labels<<"<#{max}"
      elsif max.nil?
        labels<<">#{min}"
      else
        labels<<"[#{min},#{max}]"
      end
    }
    return labels
  end

  # get a color according to the loudness index
  def self.to_color(measure)
    idx=(measure.kind_of?(Measure)) ? measure.loudness_idx : self.compute_loudness_index(measure) #hack hack hack  (type check)
    return COLORS_HTML[idx]
  end


  def self.to_kmlcolor(measure, transparent="7F")
    color=to_color(measure)
    return transparent+color[5..6]+color[3..4]+color[1..2]
  end


  def self.loudness_index_range(min=MIN_DB, max=MAX_DB, step=STEP_DB)
    nb_step=(max-min)/step
    yield([nil, min, 0])
    (0..(nb_step-1)).each{|i|
      yield([min+(i*step), min+((i+1)*step), i+1])
    }
    yield([max, nil, nb_step+1])
  end


  def self.valid?(measure)
    (measure <= 130) && (measure >= 20)
  end


  def self.compute_decibel(measures)
    sum=0.0
    measures.each {|measure|
      if valid?(measure)
        sum+=1*10.0**(0.1*measure.to_f)
      end
    }    
    if (sum==0)
      -1
    else
    10.0*Math.log10(sum/measures.length.to_f)
    end
  end

  def self.compute_loudness_index(loudness_value)
    loudness_index_range(MIN_DB, MAX_DB, STEP_DB){|min, max, i|
      if (min.nil? || loudness_value >= min) && (max.nil? || loudness_value <= max)
        return i
      end
    }
    raise Exception.new("loudness not indexable: #{loudness_value}")

  end

  # Constants for NOISE
  MAX_DB=100
  MIN_DB=40
  STEP_DB=10
  NB_STEP=2+(MAX_DB-MIN_DB)/STEP_DB

  COLORS=Array.new(NB_STEP){|i|
    self.compute_color_idx(i*STEP_DB+MIN_DB)[1..6]
  }

  COLORS_HTML=Array.new(NB_STEP){|i|
    self.compute_color_idx(i*STEP_DB+MIN_DB)
  }
#def self.COLORS
#  self.compute_color_idx(i*STEP_DB+MIN_DB)[1..6]
#end
  
#def self.COLORS_HTML
#  colors=[]
#  NB_STEP.times{|i|
#    colors<<self.compute_color_idx(i*STEP_DB+MIN_DB)
#  }
#  return colors
#end
  
  DISTRIBUTION_LABELS=self.compute_distribution_labels
  

  COLOR_HTML=["0aff00", "33ff00", "5cff00", "85ff00", "adff00", "d6ff00", "ffff00", "ffd600", "ffad00", "ff8500", "ff5c00", "ff3300", "ff0a00", "ff001f", "ff0047", "ff0070", "ff0099"].join('|')
  #LABEL =["<40", "40-50", "50-60", "60-70", "70-80", "80-90", ">90"]
  #HUE_TABLE=[0.6, 0.55, 0.47, 0.3, 0.2, 0.17, 0.15, 0.1, 0, 0.95, 0.90, 0.85, 0.80, 0.78]
    
end
