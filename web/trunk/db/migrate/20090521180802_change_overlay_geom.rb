class ChangeOverlayGeom < ActiveRecord::Migration
  def self.up
    # change_column doesn't work
      remove_column :overlays, :geom
      add_column :overlays, :geom, :polygon , :srid=>4326
  end

  def self.down

  end
end
