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
 
module ApiHelper

  GRAPH_LEQ_DIST_LABEL="%3C#{SoundLevel::MIN_DB}|"+Array.new(SoundLevel::NB_STEP){|i|
    "#{(i+1)*SoundLevel::STEP_DB+SoundLevel::MIN_DB if (i%2)==1 }"
  }.join('|')+"|%3E#{SoundLevel::MAX_DB}"

  def generate_tags_distribution_graph(labels,freq)
    graph= Gchart.pie(:data => freq,
                      :title=>"Social Tags",
                      :size => '300x140',
                      :labels => labels,
                      :custom=>"chf=bg,s,BBBBBBBB&chco=00AF33,4BB74C,EE2C2C,CC3232,33FF33,66FF66,9AFF9A,C1FFC1,CCFFCC&chts=000000,15")
                      #:custom=>"chf=bg,s,FFFFFFFF&chco=00AF33,4BB74C,EE2C2C,CC3232,33FF33,66FF66,9AFF9A,C1FFC1,CCFFCC&chts=000000,15")
    return graph
  end

  def  generate_leq_distribution_graph(dist, params)
    # build graph
    if params[:ge].nil?
      graph= Gchart.bar(:data => dist,
                        :size =>  params[:size]||"220x80",
                        :axis_with_labels => ['x', 'y'],
                        :axis_labels=>[GRAPH_LEQ_DIST_LABEL, "0|#{dist.max}"],
                        :bar_width_and_spacing=>"a",
                        :custom=>"chf=bg,s,EE000000&chco=#{SoundLevel::COLORS.join('|')}")
    else
      size=params[:size]
      graph= Gchart.bar(:data => dist,
                        :title=>"Leq(1s) Distribution in dB(A)",
                        :size => size,
                        :axis_with_labels => ['x', 'y'],
                        :axis_labels=>[GRAPH_LEQ_DIST_LABEL, "0|#{dist.max}"],
                        :bar_width_and_spacing=>"a",
                        :custom=>"chxs=1,000000,12|0,000000,12&chts=000000,14&chf=bg,s,FFFFFF77&chco=#{SoundLevel::COLORS.join('|')}")
    end
  end


end
