class Adddistrict < ActiveRecord::Migration
  def self.up
    add_column :segment, :district_id, :integer
  end

  def self.down
  end
end
