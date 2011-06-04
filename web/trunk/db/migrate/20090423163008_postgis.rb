class Postgis < ActiveRecord::Migration
  def self.up
    add_column :measures, :geom, :point, :with_z => false, :null => true, :srid => 4326
    add_index :measures, "geom", :spatial=>true

  end

  def self.down
  end
end
