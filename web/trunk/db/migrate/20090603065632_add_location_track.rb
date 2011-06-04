class AddLocationTrack < ActiveRecord::Migration
  def self.up
    add_column :tracks, :location, :text
    add_column :users, :measures_size, :integer
    add_column :users, :tags_size, :integer
    add_column :users, :daily_graph, :text
    add_column :cities, :measures_size, :integer
    add_column :cities, :tags_size, :integer
    add_column :cities, :daily_graph, :text

  end

  def self.down
  end
end
