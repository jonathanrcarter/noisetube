class AddTimestampswithtz < ActiveRecord::Migration
  def self.up
    add_column :measures, :measured_on, "timestamp with time zone"
    add_column :tracks, :started_on, "timestamp with time zone"
  end

  def self.down
    remove_column :measures, :measured_on
    remove_column :tracks, :started_on
  end
end
