class AddplaceId < ActiveRecord::Migration
  def self.up
    add_column :measures, :location, :string
    add_column :tracks, :sw_lat, :float
    add_column :tracks, :sw_lng, :float
    add_column :tracks, :ne_lat, :float
    add_column :tracks, :ne_lng, :float
  end

  def self.down
    remove_column :measures, :location
  end
end
