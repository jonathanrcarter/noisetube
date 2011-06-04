class Add < ActiveRecord::Migration
  def self.up
    add_column :measures,:time_tagged, :boolean , :default=>false
    add_column :measures,:location_tagged, :boolean , :default=>false
    add_column :measures,:useractivity_tagged, :boolean , :default=>false
    add_column :measures,:loudness_tagged, :boolean , :default=>false
    add_column :measures,:weather_tagged, :boolean , :default=>false
    
  end

  def self.down
    remove_column :measures,:autotagged
  end
end
