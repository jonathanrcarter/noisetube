class ChangeTracksGeom < ActiveRecord::Migration
  def self.up
     remove_column :tracks, :sw_lat
     remove_column :tracks, :sw_lng
     remove_column :tracks, :ne_lat
     remove_column :tracks, :ne_lng 
     add_column :tracks, :geom, :polygon , :srid=>4326
     add_index :tracks, "geom", :spatial=>true
     add_index :overlays, "geom", :spatial=>true
  end

  def self.down
  end
end
