class Addtimezone < ActiveRecord::Migration
  def self.up
    change_column :measures, :created_at , "timestamp with time zone" 
  end

  def self.down
  end
end
