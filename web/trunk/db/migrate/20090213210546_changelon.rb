class Changelon < ActiveRecord::Migration
  def self.up
    rename_column :measures, :lon, :lng
  end

  def self.down
    rename_column :measures, :lng, :lon
  end
end
