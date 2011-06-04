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
 
class Api < ActiveRecord::Base

  def self.distribution_leq(params)
    dist=Array.new(SoundLevel::NB_STEP, 0)
    #p dist
    # querying
    cond, joins=Measure.search_options(params)
    d=Measure.count(:all, :joins=>joins, :group=>:loudness_index, :conditions=>cond, :order=>"loudness_index")
    #p d
    # rendering
    unless d.nil?
      d.collect{|i, freq| dist[i]=freq if i<(SoundLevel::NB_STEP-1) }   #in case there are a loudness_index issue
    end

    return dist
  end

  def self.distribution_contributors(measures, freq_sort=false)
    dist={}

    # #Tag.group by count  group by

    # get the results ms=_search
    measures.each { |m|
      dist[m.user.login]=0 if dist[m.user.login].nil?
      dist[m.user.login]+=1
    }

    # sort by freq
    if freq_sort
      dist=dist.sort { |l, r| r[1]<=>l[1] }
      # sort by name
    else
      dist=dist.sort
    end
    dist
  end

  def self.distribution_tags(params)

    # querying
    cond, joins=Measure.search_options(params)
    #we have to remove tags and taggings
    joins.gsub!("INNER JOIN taggings ON taggings.taggable_id = measures.id", "")
    joins.gsub!("INNER JOIN tags ON tags.id = taggings.tag_id", "")
    context="tags"
    context=params[:tags_type] unless params[:tags_type].blank?
    tags=Measure.tag_counts_on(context, {:joins=>joins, :conditions=>cond, :order=>"count desc", :limit=>10})

    # filter the tags
    excluded_tags=[]
    excluded_tags+=params[:tags].split(",") unless params[:tags].blank?
    tags=tags.select{|tag| !excluded_tags.include?("#{context}:"+tag.name)}

    # rendering
    labels=[]
    freq=[]
    tags.each_with_index{|tag, i|
      labels << "#{tag.name.capitalize}(#{tag.count})"
      freq << tag.count
    }

    return labels, freq
  end
    
end
