class RenameStartedOn < ActiveRecord::Migration
  def self.up
    rename_column :tracks, :started_on, :starts_at
  end

  def self.down
    rename_column :tracks, :starts_at, :started_on
  end
end
