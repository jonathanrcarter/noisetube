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
 
class Interpreter

  # collectors of all the active interpreters
  @@interpreters=[]
  @tags_type=[]

  def initialize
    @inserts=[]
    @tags_type=["__"]
    @batch=true

    "taggings.context!='tags' and taggings.context=''"
  end

  def self.interpreters=(array)
    @@interpreters=array
  end

  def self.interpreters
    return @@interpreters
  end

  def pre_process(track)

    puts "\n\nClassifier #{self.class.name} (batch:#{@batch})"
    puts '---- preprocessing ---'

    cond=(@tags_type.empty?)? "" : "and (#{@tags_type.map{|e| "taggings.context='#{e}'"}.join(" or ")})"
    Tagging.transaction do
      list=Tagging.find(:all, :joins=>"LEFT OUTER JOIN measures ON  measures.id=taggings.taggable_id",
                        :conditions=>"measures.track_id=#{track.id} and taggings.context!='tags' #{cond}")
      puts "#{list.size} tag assignments will be removed"
      list.each {|tagging|
        tagging.destroy
      }
    end
    @inserts=[] if @batch
  end

  def post_process(track)
    puts '---- postprocessing ---'
    batch if @batch
  end

  def process(track)
    pre_process(track)
    _process(track)
    post_process(track)
  end

  def _process(track)
    Measure.transaction {
      track.measures.find(:all, :order=>"made_at").each{|measure|
        interpret(measure)
      }
    }
  end

  def add_to_batch(measure, tag_name, semantic)

    tag=Tag.find_or_create_by_name(tag_name)
    @inserts.push("('#{DateTime.now.to_formatted_s(:db)}','User','Measure',#{measure.id}, #{tag.id}, #{measure.user_id}, '#{semantic}')")
  end

  # #mass insert
  def batch
    unless @inserts.empty?
      sql = "INSERT INTO Taggings (created_at, tagger_type, taggable_type, taggable_id, tag_id, tagger_id, context) VALUES #{@inserts.join(", ")}"
      puts "BATCH: #{@inserts.size} inserted elements"
      ActiveRecord::Base.connection().execute(sql);
    else
      puts "no inserted element"
    end

  end

  def tag(measure, tags, base)
    tags.each{|tag|
      context, name=tag.split(":")
      semantic="#{base}_#{context}" unless context.nil?
      tag_name=name.downcase.strip  unless name.nil?

      if @batch
        add_to_batch(measure, tag_name, semantic) unless tag_name.nil?
      else
        measure.user.tag(measure, :with=>tag_name, :on=>"#{semantic}") unless tag_name.nil?
      end
    }
  end
end