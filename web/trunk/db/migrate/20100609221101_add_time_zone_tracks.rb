class AddTimeZoneTracks < ActiveRecord::Migration
  def self.up
    change_column :tracks, :created_at, "timestamp with time zone"
    change_column :tracks, :ends_at, "timestamp with time zone"
  end

  def self.down
    change_column :tracks, :created_at, :datetime
    change_column :tracks, :ends_at, :datetime
  end
end
