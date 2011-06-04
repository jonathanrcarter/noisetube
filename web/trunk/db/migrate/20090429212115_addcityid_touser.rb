class AddcityidTouser < ActiveRecord::Migration
  def self.up
    add_column :users, :city_id, :integer
  end

  def self.down
  end
end
