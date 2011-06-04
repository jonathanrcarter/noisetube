class LastActivity < ActiveRecord::Migration
  def self.up
    add_column :users, :last_activity_at, :timestamp
    add_column :cities, :last_activity_at, :timestamp
  end

  def self.down
    remove_column :users, :last_activity_at
    remove_column :cities, :last_activity_at
  end
end
