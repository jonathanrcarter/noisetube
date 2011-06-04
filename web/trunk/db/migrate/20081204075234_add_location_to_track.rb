class AddLocationToTrack < ActiveRecord::Migration
  def self.up
    add_column :tracks, :lat,:float
    add_column :tracks, :lon,:float
  end

  def self.down
    remove_column :tracks, :lat
    remove_column :tracks, :lon
  end
end
