class RenameMeasuredOn < ActiveRecord::Migration
  def self.up
    rename_column :measures, :measured_on, :made_at
  end

  def self.down
    rename_column :measures, :made_at, :measured_on
  end
end

