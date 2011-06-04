class Fixtrack < ActiveRecord::Migration
  def self.up
    rename_column :measures, :session_id, :track_id
  end

  def self.down
    rename_column :measures, :track_id, :session_id
  end
end
