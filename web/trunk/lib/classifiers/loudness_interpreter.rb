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
 
require 'classifiers/interpreter.rb'

class LoudnessInterpreter<Interpreter

  def initialize
    super
    @peak_idx=0
    @idx=0
    @memory_peak=[]
    @memory_longexp=[]
    @tags_type=[
          "loudness_value",
          "loudness_behavior"]
  end

  def update_memory(measure)
    # #update memory
    @memory_peak.delete_at(0) if (@memory_peak.size==5)
    @memory_peak<<measure
    @memory_longexp.delete(0) if (@memory_longexp.size==30)
    @memory_longexp<<measure
    puts "Excessing memory " if (@memory_peak.size>5)
  end

  def update_previous_measures(memory, tag)
    memory.each{|measure|
      tag(measure,[tag],"loudness")
    }
    memory=[]
  end

  def interpret(measure)
    # update_memory(measure)

    tags=[]

    if measure.loudness <50
      tags<<"value:quiet"
    elsif measure.loudness<75
      tags<<"value:annoying"
    elsif measure.loudness<85
      tags<<"value:noisy"
    else
      tags<<"value:risky"
    end
    
    
    # sudden peak detection
    if measure.peak==1
      tags<<"behavior:sudden peak"
      @peak_idx=@idx
      # update_previous_measures(@memory_peak,"sudden peak")
    else
      # and a 5 seconds after #if (@idx<(@peak_idx+5))
      #  tags<<"sudden peak"
      # #end
    end

    # long exposure
    if measure.longexposure==1
      tags<<"behavior:short-term risky exposure"
      # #update_previous_measures(@memory_longexp,"long high exposure")
    end

    tag(measure, tags,"loudness")

  end
end
